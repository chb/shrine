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
import net.shrine.aggregation.Aggregator
import net.shrine.aggregation.SpinResultEntry
import net.shrine.aggregation.RunQueryAggregator
import net.shrine.aggregation.ReadQueryDefinitionAggregator
import net.shrine.aggregation.ReadPdoResponseAggregator
import net.shrine.aggregation.ReadInstanceResultsAggregator
import net.shrine.aggregation.ReadPreviousQueriesAggregator
import net.shrine.aggregation.RenameQueryAggregator
import net.shrine.aggregation.DeleteQueryAggregator
import org.spin.tools.config.DefaultPeerGroups
import net.shrine.broadcaster.BroadcastService
import scala.concurrent.duration.Duration
import net.shrine.aggregation.Aggregators
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

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
    auditDao: AuditDao,
    authorizationService: QueryAuthorizationService,
    includeAggregateResult: Boolean,
    broadcastService: BroadcastService,
    queryTimeout: Duration) extends ShrineRequestHandler with Loggable {

  import broadcastService.sendAndAggregate
  
  override def runQuery(request: RunQueryRequest, shouldBroadcast: Boolean): ShrineResponse = {
    afterAuditingAndAuthorizing(request) {
      waitFor(sendAndAggregate(request, runQueryAggregatorFor(request), shouldBroadcast))
    }
  }

  override def readQueryDefinition(request: ReadQueryDefinitionRequest, shouldBroadcast: Boolean) = waitFor(sendAndAggregate(request, new ReadQueryDefinitionAggregator, shouldBroadcast))

  override def readPdo(request: ReadPdoRequest, shouldBroadcast: Boolean) = waitFor(sendAndAggregate(request, new ReadPdoResponseAggregator, shouldBroadcast))

  override def readInstanceResults(request: ReadInstanceResultsRequest, shouldBroadcast: Boolean) = waitFor(sendAndAggregate(request, new ReadInstanceResultsAggregator(request.shrineNetworkQueryId, false), shouldBroadcast))

  override def readQueryInstances(request: ReadQueryInstancesRequest, shouldBroadcast: Boolean) = {
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

  override def readPreviousQueries(request: ReadPreviousQueriesRequest, shouldBroadcast: Boolean) = waitFor(sendAndAggregate(request, new ReadPreviousQueriesAggregator(request.userId, request.projectId), shouldBroadcast))

  override def renameQuery(request: RenameQueryRequest, shouldBroadcast: Boolean) = waitFor(sendAndAggregate(request, new RenameQueryAggregator, shouldBroadcast))

  override def deleteQuery(request: DeleteQueryRequest, shouldBroadcast: Boolean) = waitFor(sendAndAggregate(request, new DeleteQueryAggregator, shouldBroadcast))

  override def readApprovedQueryTopics(request: ReadApprovedQueryTopicsRequest, shouldBroadcast: Boolean) = authorizationService.readApprovedEntries(request)

  override def readQueryResult(request: ReadQueryResultRequest, shouldBroadcast: Boolean): ShrineResponse = waitFor(sendAndAggregate(request, new ReadQueryResultAggregator(request.queryId, includeAggregateResult), shouldBroadcast))
  
  private def waitFor(futureResponse: Future[ShrineResponse]): ShrineResponse = {
    Util.time("Waiting for aggregated results")(debug(_)) {
      Await.result(futureResponse, queryTimeout)
    }
  }
  
  private[service] val runQueryAggregatorFor: RunQueryRequest => RunQueryAggregator = Aggregators.forRunQueryRequest(includeAggregateResult) _
  
  private[service] def afterAuditingAndAuthorizing[T](request: RunQueryRequest)(body: => T): T = {
    auditTransactionally(request) {
      authorizationService.authorizeRunQueryRequest(request)
      
      body
    }
  }
  
  private[service] def auditTransactionally[T](request: RunQueryRequest)(body: => T): T = {
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
}