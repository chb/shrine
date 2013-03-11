package net.shrine.service

import net.shrine.protocol._
import net.shrine.authorization.QueryAuthorizationService
import org.spin.tools.crypto.signature.Identity
import org.spin.tools.config.{ EndpointType, EndpointConfig }
import org.apache.log4j.MDC
import net.shrine.filters.LogFilter
import java.lang.String
import org.spin.tools.crypto.PKCryptor
import net.shrine.broadcaster.dao.AuditDao
import net.shrine.broadcaster.dao.model.AuditEntry
import org.spin.message.{ AckNack, Failure, Response, Result, ResultSet, QueryInfo }
import net.shrine.aggregation._
import org.apache.log4j.Logger
import org.spin.tools.crypto.Envelope
import org.spin.identity.IdentityService
import org.spin.client.AgentException
import org.spin.client.SpinClient
import java.net.MalformedURLException
import org.spin.tools.NetworkTime
import net.shrine.util.Util
import scala.util.Try
import net.shrine.util.Loggable
import net.shrine.aggregation.ReadQueryResultAggregator
import scala.concurrent.Future
import scala.concurrent.Await
import org.spin.client.Credentials

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
object ShrineService {
  private[service] def toCredentials(authn: AuthenticationInfo): Credentials = Credentials(authn.domain, authn.username, authn.credential.value)
}

class ShrineService(
    auditDao: AuditDao,
    authorizationService: QueryAuthorizationService,
    broadcasterPeerGroupToQuery: Option[String],
    includeAggregateResult: Boolean,
    spinClient: SpinClient) extends ShrineRequestHandler with Loggable {

  import ShrineService._
  
  private[service] def determinePeergroupFallingBackTo(projectId: String): String = {
    broadcasterPeerGroupToQuery.getOrElse(projectId)
  }

  private[service] def broadcastMessage(message: BroadcastMessage): Future[ResultSet] = {
    val queryType = message.request.requestType.name
    
    val credentials = toCredentials(message.request.authn)
    
    val peerGroupToQuery = determinePeergroupFallingBackTo(message.request.projectId)
    
    Util.time("Broadcasting via Spin")(debug(_)) {
      spinClient.query(queryType, message, peerGroupToQuery, credentials)
    }
  }

  private def waitForResults(futureResults: Future[ResultSet]): ResultSet = {
    val delta = 100L
    
    Util.time("Waiting for Spin results")(debug(_)) {
      import scala.concurrent.duration._
      
      Await.result(futureResults, (spinClient.config.maxWaitTime + delta).milliseconds)
    }
  }

  private[service] def aggregate(spinResults: ResultSet, aggregator: Aggregator): ShrineResponse = {

    def toDescription(response: Response): String = Option(response).map(_.getDescription).getOrElse("Unknown")

    import scala.collection.JavaConverters._

    val (results, failures, nullResponses) = {
      val (results, nullResults) = spinResults.getResults.asScala.partition(_ != null)

      val (failures, nullFailures) = spinResults.getFailures.asScala.partition(_ != null)

      (results, failures, nullResults ++ nullFailures)
    }

    if (!failures.isEmpty) {
      warn("Received " + failures.size + " failures. descriptions:")

      failures.map("  " + _.getDescription).foreach(this.warn(_))
    }

    if (!nullResponses.isEmpty) {
      error("Received " + nullResponses.size + " null results.  Got non-null results from " + (results.size + failures.size) + " nodes: " + (results ++ failures).map(toDescription))
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

  protected def executeRequest(request: ShrineRequest, aggregator: Aggregator): ShrineResponse = {
    executeRequest(BroadcastMessage(request), aggregator)
  }
  
  protected def executeRequest(message: BroadcastMessage, aggregator: Aggregator): ShrineResponse = {
    val resultSet = waitForResults(broadcastMessage(message))
    
    val result = Util.time("Aggregating")(debug(_)) {
      aggregate(resultSet, aggregator)
    }

    debug("Aggregated into a " + result.getClass.getName)
    
    result
  }

  private def auditTransactionally[T](request: RunQueryRequest)(body: => T): T = {
    auditDao.inTransaction {
      try { body } finally {
        auditDao.addAuditEntry(
          request.projectId,
          request.authn.domain,
          request.authn.username,
          request.queryDefinition.toI2b2String, //TODO: Use i2b2 format Still?
          request.topicId)
      }
    }
  }

  override def runQuery(request: RunQueryRequest): ShrineResponse = {
    auditTransactionally(request) {

      authorizationService.authorizeRunQueryRequest(request)

      val reqWithQueryIdAssigned = request.withNetworkQueryId(BroadcastMessage.Ids.next)

      val message = BroadcastMessage(reqWithQueryIdAssigned.networkQueryId, reqWithQueryIdAssigned)

      val aggregator = new RunQueryAggregator(
        message.requestId,
        reqWithQueryIdAssigned.authn.username,
        reqWithQueryIdAssigned.projectId,
        reqWithQueryIdAssigned.queryDefinition,
        includeAggregateResult)

      executeRequest(message, aggregator)
    }
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

  override def readPreviousQueries(request: ReadPreviousQueriesRequest) = executeRequest(request, new ReadPreviousQueriesAggregator(request.userId, request.projectId))

  override def renameQuery(request: RenameQueryRequest) = executeRequest(request, new RenameQueryAggregator)

  override def deleteQuery(request: DeleteQueryRequest) = executeRequest(request, new DeleteQueryAggregator)

  override def readApprovedQueryTopics(request: ReadApprovedQueryTopicsRequest) = authorizationService.readApprovedEntries(request)

  override def readQueryResult(request: ReadQueryResultRequest): ShrineResponse = executeRequest(request, new ReadQueryResultAggregator(request.queryId, includeAggregateResult))
}