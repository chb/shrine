package net.shrine.service

import net.shrine.protocol._
import net.shrine.authorization.QueryAuthorizationService
import org.spin.tools.crypto.signature.Identity
import net.shrine.config.ShrineConfig
import org.spin.tools.config.{ EndpointType, EndpointConfig }
import org.apache.log4j.MDC
import net.shrine.filters.LogFilter
import java.lang.String
import org.spin.tools.crypto.PKCryptor
import net.shrine.broadcaster.dao.AuditDAO
import net.shrine.broadcaster.dao.hibernate.AuditEntry
import org.springframework.transaction.annotation.Transactional
import org.spin.message.{ AckNack, Failure, Response, Result, ResultSet, QueryInfo }
import net.shrine.aggregation._
import org.apache.log4j.Logger
import org.spin.tools.crypto.Envelope
import org.spin.identity.IdentityService
import org.spin.client.AgentException
import org.spin.client.SpinAgent
import org.spin.client.TimeoutException
import java.net.MalformedURLException
import org.spin.tools.NetworkTime
import net.shrine.util.Util
import net.shrine.util.Try
import net.shrine.util.Loggable

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
  private val spinClient: SpinAgent) extends ShrineRequestHandler with Loggable {

  import ShrineService._

  private lazy val aggregatorEndpointConfig = new EndpointConfig(EndpointType.SOAP, shrineConfig.getAggregatorEndpoint);

  protected def generateIdentity(authn: AuthenticationInfo): Identity = identityService.certify(authn.domain, authn.username, authn.credential.value)

  private[service] def determinePeergroup(projectId: String): String = {
    Option(shrineConfig.getBroadcasterPeerGroupToQuery).getOrElse(projectId)
  }

  private[service] def broadcastMessage(message: BroadcastMessage, queryInfo: QueryInfo): AckNack = {
    val ackNack = spinClient.send(queryInfo, message, BroadcastMessage.serializer)

    if (ackNack.isError) {
      throw new AgentException("Error encountered during query.")
    }

    ackNack
  }

  private def getSpinResults(queryID: String, identity: Identity): ResultSet = {
    try {
      spinClient.receive(queryID, identity)
    } catch {
      case e: TimeoutException => spinClient.getResult(queryID, identity)
    }
  }

  private[service] def aggregate(queryId: String, identity: Identity, aggregator: Aggregator): ShrineResponse = {

    def toDescription(response: Response): String = Option(response).map(_.getDescription).getOrElse("Unknown")

    val spinResults = {
      val start = System.currentTimeMillis

      val fromSpin = getSpinResults(queryId, identity)

      val elapsed = System.currentTimeMillis - start

      debug("Polling Spin for results took " + elapsed + "ms")

      fromSpin
    }

    import scala.collection.JavaConverters._

    val (results, failures, nullResponses) = {
      val (results, nullResults) = spinResults.getResults.asScala.partition(_ != null)

      val (failures, nullFailures) = spinResults.getFailures.asScala.partition(_ != null)

      (results, failures, nullResults ++ nullFailures)
    }

    if (!failures.isEmpty) {
      log.warn("Received " + failures.size + " failures. descriptions:")

      failures.map("  " + _.getDescription).foreach(log.warn)
    }

    if (!nullResponses.isEmpty) {
      log.error("Received " + nullResponses.size + " null results.  Got non-null results from " + (results.size + failures.size) + " nodes: " + (results ++ failures).map(toDescription))
    }

    def decrypt(envelope: Envelope) = {
      if (envelope.isEncrypted) (new PKCryptor).decrypt(envelope)
      else envelope.getData
    }

    def toHostName(url: String): Option[String] = Try(new java.net.URL(url).getHost).toOption

    val spinResultEntries = results.map(result => new SpinResultEntry(decrypt(result.getPayload), result))

    //TODO: Make something better here, using the failing node's human-readable name.  
    //Using the failing node's hostname is the best we can do for now.
    val errorResponses = for {
      failure <- failures
      hostname <- toHostName(failure.getOriginUrl)
    } yield ErrorResponse(hostname)

    aggregator.aggregate(spinResultEntries.toSeq, errorResponses.toSeq)
  }

  protected def executeRequest(identity: Identity, message: BroadcastMessage, aggregator: Aggregator): ShrineResponse = {
    val queryInfo = new QueryInfo(determinePeergroup(message.request.projectId), identity, message.request.requestType.name, aggregatorEndpointConfig)

    val ackNack = broadcastMessage(message, queryInfo)

    val start = System.currentTimeMillis

    val result = aggregate(ackNack.getQueryId, identity, aggregator)

    val elapsed = System.currentTimeMillis - start

    debug("Aggregating into a " + result.getClass.getName + " took: " + elapsed + "ms")

    result
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
  override def runQuery(request: RunQueryRequest): ShrineResponse = {
    authorizationService.authorizeRunQueryRequest(request)

    val identity = generateIdentity(request.authn)

    auditRunQueryRequest(identity, request)

    val message = BroadcastMessage(request)

    //TODO: What if masterId and instanceId are None?
    val aggregator = new RunQueryAggregator(
      message.masterId.get,
      request.authn.username,
      request.projectId,
      request.queryDefinition,
      message.instanceId.get,
      shrineConfig.isIncludeAggregateResult)

    executeRequest(identity, message, aggregator)
  }

  override def readQueryDefinition(request: ReadQueryDefinitionRequest) = executeRequest(request, new ReadQueryDefinitionAggregator)

  override def readPdo(request: ReadPdoRequest) = executeRequest(request, new ReadPdoResponseAggregator)

  override def readInstanceResults(request: ReadInstanceResultsRequest) = executeRequest(request, new ReadInstanceResultsAggregator(request.shrineNetworkQueryId, false))

  override def readQueryInstances(request: ReadQueryInstancesRequest) = {
    val now = Util.now
    val networkQueryId = request.queryId
    val username = request.authn.username
    val groupId = request.projectId

    //NB: Return a dummy response, with a dummy QueryInstance containing the network (Shrine) id of the query we'd like
    //to get "instances" for.  This allows the legacy web client to formulate a request for query results that Shrine
    //can understand, while meeting the conversational requirements of the legacy web client.
    val instance = QueryInstance(networkQueryId.toString, networkQueryId.toString, username, groupId, now, now)

    ReadQueryInstancesResponse(networkQueryId, username, groupId, Seq(instance))
  }

  override def readPreviousQueries(request: ReadPreviousQueriesRequest) = executeRequest(request, new ReadPreviousQueriesAggregator(request.authn.username, request.projectId))

  override def renameQuery(request: RenameQueryRequest) = executeRequest(request, new RenameQueryAggregator)

  override def deleteQuery(request: DeleteQueryRequest) = executeRequest(request, new DeleteQueryAggregator)

  override def readApprovedQueryTopics(request: ReadApprovedQueryTopicsRequest) = authorizationService.readApprovedEntries(request)

  override def readResult(request: ReadResultRequest): ShrineResponse = sys.error("TODO")
}

object ShrineService {
  private val log = Logger.getLogger(classOf[ShrineService])
}