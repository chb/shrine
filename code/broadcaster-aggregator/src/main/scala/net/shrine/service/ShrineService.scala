package net.shrine.service

import net.shrine.protocol._
import net.shrine.authorization.QueryAuthorizationService
import org.spin.tools.crypto.signature.Identity
import net.shrine.config.ShrineConfig
import org.spin.tools.config.{EndpointType, EndpointConfig}
import org.apache.log4j.MDC
import net.shrine.filters.LogFilter
import java.lang.String
import org.spin.tools.crypto.PKCryptor
import scala.collection.JavaConversions._
import net.shrine.broadcaster.dao.AuditDAO
import net.shrine.broadcaster.dao.hibernate.AuditEntry
import org.springframework.transaction.annotation.Transactional
import org.spin.message.{AckNack, Failure, Response, Result, ResultSet, QueryInfo}
import net.shrine.aggregation._
import org.apache.log4j.Logger
import org.spin.tools.crypto.Envelope
import org.spin.identity.IdentityService
import org.spin.client.AgentException
import org.spin.client.SpinAgent
import org.spin.client.TimeoutException
import java.net.MalformedURLException

/**
 * @author Bill Simons
 * @date 3/23/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ShrineService(
    private val auditDao: AuditDAO,
    private val authorizationService: QueryAuthorizationService,
    private val identityService: IdentityService,
    private val shrineConfig: ShrineConfig,
    private val spinClient: SpinAgent) extends ShrineRequestHandler {

  import ShrineService._
  
  private lazy val aggregatorEndpointConfig = new EndpointConfig(EndpointType.SOAP, shrineConfig.getAggregatorEndpoint);

  protected def generateIdentity(authn: AuthenticationInfo): Identity = identityService.certify(authn.domain, authn.username, authn.credential.value)

  private[service] def determinePeergroup(projectId: String): String = {
    if(shrineConfig.getBroadcasterPeerGroupToQuery == null) {
      projectId
    }
    else {
      shrineConfig.getBroadcasterPeerGroupToQuery
    }
  }

  private[service] def broadcastMessage(message: BroadcastMessage, queryInfo: QueryInfo): AckNack = {
    val ackNack = spinClient.send(queryInfo, message, BroadcastMessage.serializer)

    if(ackNack.isError()) {
      throw new AgentException("Error encountered during query.")
    }

    ackNack
  }

  private def getSpinResults(queryID: String, identity: Identity): ResultSet = {
    try {
      spinClient.receive(queryID, identity)
    }
    catch {
      case e: TimeoutException => spinClient.getResult(queryID, identity)
    }
  }

  private[service] def aggregate(queryId: String, identity: Identity, aggregator: Aggregator) = {
    
    def toDescription(response: Response): String = Option(response).map(_.getDescription).getOrElse("Unknown")
    
    val spinResults = getSpinResults(queryId, identity)
    
    val (results, failures, nullResponses) = {
      val (results, nullResults) = spinResults.getResults.toSeq.partition(_ != null)
      
      val (failures, nullFailures) = spinResults.getFailures.toSeq.partition(_ != null)
      
      (results, failures, nullResults ++ nullFailures)
    }

    if(!failures.isEmpty) {
      log.warn("Received " + failures.size + " failures. descriptions:")
      
      failures.map("  " + _.getDescription).foreach(log.warn)
    }
    
    if(!nullResponses.isEmpty) {
      log.error("Received " + nullResponses.size + " null results.  Got non-null results from " + (results.size + failures.size) + " nodes: " + (results ++ failures).map(toDescription))
    }
    
    def decrypt(envelope: Envelope) = {
      if(envelope.isEncrypted) {
        (new PKCryptor).decrypt(envelope) 
      } else {
        envelope.getData
      }
    }
    
    def toHostName(url: String): Option[String] = {
      try {
        Option((new java.net.URL(url)).getHost)
      } catch {
        case e: MalformedURLException => None
      }
    }
    
    val spinResultEntries = results.map(result => new SpinResultEntry(decrypt(result.getPayload), result))

    //TODO: Make something better here, using the failing node's human-readable name.  
    //Using the failing node's hostname is the best we can do for now.
    val errorResponses = for {
      failure <- failures
      hostname <- toHostName(failure.getOriginUrl)
    } yield ErrorResponse(hostname)

    aggregator.aggregate(spinResultEntries, errorResponses)
  }

  protected def executeRequest(identity: Identity, message: BroadcastMessage, aggregator: Aggregator): ShrineResponse = {
    val queryInfo = new QueryInfo(determinePeergroup(message.request.projectId), identity, message.request.requestType.name, null.asInstanceOf[EndpointConfig])
    
    val ackNack = broadcastMessage(message, queryInfo)
    
    aggregate(ackNack.getQueryId, identity, aggregator)
  }

  protected def executeRequest(request: ShrineRequest, aggregator: Aggregator): ShrineResponse = {
    executeRequest(generateIdentity(request.authn), new BroadcastMessage(logId, request), aggregator)
  }

  private def logId: Long = MDC.get(LogFilter.GRID).asInstanceOf[Long]

  private def auditRunQueryRequest(identity: Identity, request: RunQueryRequest) {
    auditDao.addAuditEntry(AuditEntry(
      request.projectId,
      identity.getDomain,
      identity.getUsername,
      request.queryDefinition.toI2b2.toString, //TODO: Use i2b2 format Still?
      request.topicId))
  }

  @Transactional
  def runQuery(request: RunQueryRequest) = {
    authorizationService.authorizeRunQueryRequest(request)
    val identity = generateIdentity(request.authn)

    auditRunQueryRequest(identity, request)
    val message = BroadcastMessage(request)
    val aggregator = new RunQueryAggregator(message.masterId.get, request.authn.username, request.projectId,
      request.queryDefinition, message.instanceId.get, shrineConfig.isIncludeAggregateResult)
    executeRequest(identity, message, aggregator)
  }

  def readQueryDefinition(request: ReadQueryDefinitionRequest) = executeRequest(request, new ReadQueryDefinitionAggregator())

  def readPdo(request: ReadPdoRequest) = executeRequest(request, new ReadPdoResponseAggregator())

  def readInstanceResults(request: ReadInstanceResultsRequest) = executeRequest(request, new ReadInstanceResultsAggregator(request.instanceId, false))

  def readQueryInstances(request: ReadQueryInstancesRequest) = executeRequest(request, new ReadQueryInstancesAggregator(request.queryId, request.authn.username, request.projectId))

  def readPreviousQueries(request: ReadPreviousQueriesRequest) = executeRequest(request, new ReadPreviousQueriesAggregator(request.authn.username, request.projectId))

  def renameQuery(request: RenameQueryRequest) = executeRequest(request, new RenameQueryAggregator())

  def deleteQuery(request: DeleteQueryRequest) = executeRequest(request, new DeleteQueryAggregator())

  def readApprovedQueryTopics(request: ReadApprovedQueryTopicsRequest) = authorizationService.readApprovedEntries(request)
}

object ShrineService {
  private val log = Logger.getLogger(classOf[ShrineService])
}