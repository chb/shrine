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
import net.shrine.util.Try
import akka.dispatch.ExecutionContexts
import akka.dispatch.Future
import akka.dispatch.Await
import akka.dispatch.ExecutionContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import net.shrine.util.Success
import net.shrine.util.Failure
import net.shrine.protocol.HasQueryResults
import net.shrine.adapter.Obfuscator.obfuscateResults

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

    def countRequest(localResultId: Long) = ReadInstanceResultsRequest(req.projectId, req.waitTimeMs, req.authn, localResultId)

    def breakdownRequest(localResultId: Long) = ReadResultRequest(req.projectId, req.waitTimeMs, req.authn, localResultId.toString)

    StoredQueries.retrieve(dao, queryId) match {
      case None => errorResponse
      case Some(shrineQueryResult: ShrineQueryResult) => {
        if (shrineQueryResult.isDone) shrineQueryResult.toQueryResults(doObfuscation).map(toResponse(queryId, _)).getOrElse(errorResponse)
        else {
          implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

          val futureCountAttempts = Future.sequence(for {
            count <- shrineQueryResult.count.toSeq
          } yield Future {
            Try(delegateCountAdapter.process(identity, countRequest(count.localId)))
          })

          val futureBreakdownAttempts = Future.sequence(for {
            Breakdown(_, localResultId, resultType, data) <- shrineQueryResult.breakdowns
          } yield Future {
            Try(delegateBreakdownAdapter.process(identity, breakdownRequest(localResultId)))
          })

          val futureResponses = for {
            countResponseAttempts: Seq[Try[ReadInstanceResultsResponse]] <- futureCountAttempts
            breakdownResponseAttempts: Seq[Try[ReadResultResponse]] <- futureBreakdownAttempts
          } yield {
            (countResponseAttempts, breakdownResponseAttempts)
          }

          import akka.util.duration._

          val (countResponseAttempts, breakdownResponseAttempts) = Await.result(futureResponses, req.waitTimeMs.milliseconds)

          val responseAttempt = for {
            countResponses: Seq[ReadInstanceResultsResponse] <- Try.sequence(countResponseAttempts)
            countResponse: ReadInstanceResultsResponse <- Try(countResponses.head)
            breakdownResponses: Seq[ReadResultResponse] <- Try.sequence(breakdownResponseAttempts)
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

          responseAttempt match {
            case Success(response) => {
              /*val rawResults = response.results
              val obfuscatedResults = obfuscateResults(response.results)
              
              dao.transactional.storeResults(authn, masterId, networkQueryId, queryDefinition, rawQueryResults, obfuscatedQueryResults, breakdownFailures, mergedBreakdowns, obfuscatedBreakdowns)*/
              
              response
            }
            case Failure(e) => ErrorResponse("Couldn't retrieve query with id '" + queryId + "' from the CRC: exception message follows: " + e.getMessage + " stack trace: " + e.getStackTrace)
          }
        }
      }
    }
  }
  
  private def obfuscateResults(results: Seq[QueryResult]): Seq[QueryResult] = {
    import net.shrine.adapter.Obfuscator.obfuscate

    if(doObfuscation) results.map(obfuscate) else results
  }

  private final class DelegateAdapter[Req <: ShrineRequest, Rsp <: ShrineResponse](unmarshal: NodeSeq => Rsp) extends CrcAdapter[Req, Rsp](crcUrl, httpClient, hiveCredentials) {
    def process(identity: Identity, req: Req): Rsp = processRequest(identity, BroadcastMessage(req)).asInstanceOf[Rsp]

    override protected def parseShrineResponse(xml: NodeSeq): ShrineResponse = unmarshal(xml)
  }

  private lazy val delegateCountAdapter = new DelegateAdapter[ReadInstanceResultsRequest, ReadInstanceResultsResponse](ReadInstanceResultsResponse.fromI2b2)

  private lazy val delegateBreakdownAdapter = new DelegateAdapter[ReadResultRequest, ReadResultResponse](ReadResultResponse.fromI2b2)
}
