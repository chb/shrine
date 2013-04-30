package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity
import net.shrine.adapter.dao.AdapterDao
import net.shrine.config.HiveCredentials
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.ReadQueryResultRequest
import net.shrine.protocol.ReadQueryResultResponse
import net.shrine.serialization.XmlMarshaller
import net.shrine.protocol.ShrineRequest
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.QueryResult
import net.shrine.config.HiveCredentials
import net.shrine.util.HttpClient
import net.shrine.protocol.ReadInstanceResultsRequest
import net.shrine.protocol.ReadInstanceResultsResponse
import scala.xml.NodeSeq
import net.shrine.adapter.dao.model.ShrineQueryResult
import net.shrine.protocol.ReadResultRequest
import net.shrine.protocol.ReadResultResponse
import net.shrine.adapter.dao.model.Breakdown
import net.shrine.protocol.ResultOutputType
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import net.shrine.protocol.HasQueryResults
import net.shrine.adapter.Obfuscator.obfuscateResults
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.AuthenticationInfo
import net.shrine.serialization.I2b2Unmarshaller
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Await
import net.shrine.util.Util

/**
 * @author clint
 * @date Nov 2, 2012
 *
 */
abstract class AbstractReadQueryResultAdapter[Req <: ShrineRequest, Rsp <: ShrineResponse with HasQueryResults](
    crcUrl: String,
    httpClient: HttpClient,
    hiveCredentials: HiveCredentials,
    dao: AdapterDao,
    doObfuscation: Boolean,
    getQueryId: Req => Long,
    toResponse: (Long, QueryResult) => Rsp) extends Adapter {

  //TODO: Use scala.concurrent.ExecutionContext.Implicits.global instead?
  private lazy val executorService = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors + 1)

  override def destroy() {
    try {
      executorService.shutdown()

      executorService.awaitTermination(5, TimeUnit.SECONDS)
    } finally {
      executorService.shutdownNow()

      super.destroy()
    }
  }

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val req = message.request.asInstanceOf[Req]

    val queryId = getQueryId(req)

    def errorResponse = ErrorResponse("Query with id '" + queryId + "' not found")

    StoredQueries.retrieve(dao, queryId) match {
      case None => errorResponse
      case Some(shrineQueryResult) => {
        if (shrineQueryResult.isDone) { shrineQueryResult.toQueryResults(doObfuscation).map(toResponse(queryId, _)).getOrElse(errorResponse) }
        else {
          //NB: If the requested query was not finished executing on the i2b2 side when Shrine recorded it, attempt to
          //retrieve it and all its sub-components (breakdown results, if any) in parallel.  Asking for the results in
          //parallel is quite possibly too clever, but feels subjectively faster than asking for them serially.
          //TODO: Review this.
          
          //Make requests for results in parallel
          val futureResponses = scatter(identity, req, shrineQueryResult)

          //Gather all the results (block until they're all returned)
          val (responseAttempt, breakdownResponseAttempts) = gather(queryId, futureResponses, req.waitTimeMs)

          responseAttempt match {
            //If we successfully received the parent response (the one with query type PATIENT_COUNT_XML), re-store it along
            //with any retrieved breakdowns before returning it.
            case Success(response) => {
              storeResultIfNecessary(shrineQueryResult, response, req.authn, queryId, getFailedBreakdownTypes(breakdownResponseAttempts))

              response
            }
            case Failure(e) => ErrorResponse(s"Couldn't retrieve query with id '$queryId' from the CRC: exception message follows: ${ e.getMessage } stack trace: ${ e.getStackTrace }")
          }
        }
      }
    }
  }

  private def scatter(identity: Identity, req: Req, shrineQueryResult: ShrineQueryResult): Future[(Option[Try[ReadInstanceResultsResponse]], Seq[Try[ReadResultResponse]])] = {

    def countRequest(localResultId: Long) = ReadInstanceResultsRequest(req.projectId, req.waitTimeMs, req.authn, localResultId)

    def breakdownRequest(localResultId: Long) = ReadResultRequest(req.projectId, req.waitTimeMs, req.authn, localResultId.toString)

    implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

    val futureCountAttempts = Future.sequence(for {
      count <- shrineQueryResult.count.toSeq
    } yield Future {
      Try(delegateCountAdapter.process(identity, countRequest(count.localId)))
    }).map(_.headOption)

    val futureBreakdownAttempts = Future.sequence(for {
      Breakdown(_, localResultId, resultType, data) <- shrineQueryResult.breakdowns
    } yield Future {
      Try(delegateBreakdownAdapter.process(identity, breakdownRequest(localResultId)))
    })

    for {
      countResponseAttempts: Option[Try[ReadInstanceResultsResponse]] <- futureCountAttempts
      breakdownResponseAttempts: Seq[Try[ReadResultResponse]] <- futureBreakdownAttempts
    } yield {
      (countResponseAttempts, breakdownResponseAttempts)
    }
  }

  private def gather(queryId: Long, futureResponses: Future[(Option[Try[ReadInstanceResultsResponse]], Seq[Try[ReadResultResponse]])], waitTimeMs: Long): (Try[Rsp], Seq[Try[ReadResultResponse]]) = {
    import scala.concurrent.duration._
    
    val (countResponseAttempts, breakdownResponseAttempts) = Await.result(futureResponses, waitTimeMs.milliseconds)

    import Util.Tries.sequence
    import Util.Tries.Implicits._
    
    val responseAttempt = for {
      countResponses: Option[ReadInstanceResultsResponse] <- sequence(countResponseAttempts)
      countResponse: ReadInstanceResultsResponse <- countResponses
      breakdownResponses: Seq[ReadResultResponse] <- sequence(breakdownResponseAttempts)
    } yield {
      val localCountResultId = countResponse.shrineNetworkQueryId

      val countQueryResult = countResponse.singleNodeResult

      val breakdownsByType = (for {
        response <- breakdownResponses
        resultType <- response.metadata.resultType
      } yield resultType -> response.data).toMap

      val queryResultToReturn = countQueryResult.withBreakdowns(breakdownsByType)

      toResponse(queryId, queryResultToReturn)
    }
    
    (responseAttempt, breakdownResponseAttempts)
  }

  private def getFailedBreakdownTypes(attempts: Seq[Try[ReadResultResponse]]): Set[ResultOutputType] = {
    val successfulBreakdownTypes = attempts.collect { case Success(ReadResultResponse(_, metadata, _)) => metadata.resultType }.flatten

    ResultOutputType.breakdownTypes.toSet -- successfulBreakdownTypes
  }

  private def storeResultIfNecessary(shrineQueryResult: ShrineQueryResult, response: Rsp, authn: AuthenticationInfo, queryId: Long, failedBreakdownTypes: Set[ResultOutputType]) {
    val responseIsDone = response.results.forall(_.statusType.isDone)

    if (responseIsDone) {
      storeResult(shrineQueryResult, response, authn, queryId, failedBreakdownTypes)
    }
  }

  private def storeResult(shrineQueryResult: ShrineQueryResult, response: Rsp, authn: AuthenticationInfo, queryId: Long, failedBreakdownTypes: Set[ResultOutputType]) {
    val rawResults = response.results
    val obfuscatedResults = obfuscateResults(doObfuscation)(response.results)

    for {
      shrineQuery <- dao.findQueryByNetworkId(queryId)
      queryResult <- rawResults.headOption
      obfuscatedQueryResult <- obfuscatedResults.headOption
    } {
      val queryDefinition = QueryDefinition(shrineQuery.name, shrineQuery.queryExpr)

      dao.inTransaction {
        dao.deleteQuery(queryId)

        dao.storeResults(authn, shrineQueryResult.localId, queryId, queryDefinition, rawResults, obfuscatedResults, failedBreakdownTypes.toSeq, queryResult.breakdowns, obfuscatedQueryResult.breakdowns)
      }
    }
  }

  private final class DelegateAdapter[Req <: ShrineRequest, Rsp <: ShrineResponse](unmarshaller: I2b2Unmarshaller[Rsp])extends CrcAdapter[Req, Rsp](crcUrl, httpClient, hiveCredentials) {
    def process(identity: Identity, req: Req): Rsp = processRequest(identity, BroadcastMessage(req)).asInstanceOf[Rsp]

    override protected def parseShrineResponse(xml: NodeSeq): ShrineResponse = unmarshaller.fromI2b2(xml)
  }

  private lazy val delegateCountAdapter = new DelegateAdapter[ReadInstanceResultsRequest, ReadInstanceResultsResponse](ReadInstanceResultsResponse)

  private lazy val delegateBreakdownAdapter = new DelegateAdapter[ReadResultRequest, ReadResultResponse](ReadResultResponse)
}
