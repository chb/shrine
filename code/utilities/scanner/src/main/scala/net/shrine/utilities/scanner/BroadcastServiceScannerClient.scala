package net.shrine.utilities.scanner

import net.shrine.broadcaster.BroadcastService
import net.shrine.util.Loggable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import net.shrine.protocol.RunQueryRequest
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.AuthenticationInfo
import ScannerClient.toQueryDef
import ScannerClient.errorTermResult
import net.shrine.aggregation.Aggregators
import net.shrine.protocol.ReadQueryResultRequest
import net.shrine.protocol.ReadQueryResultResponse
import net.shrine.aggregation.ReadQueryResultAggregator
import net.shrine.utilities.scanner.components.HasSingleThreadExecutionContextComponent
import net.shrine.protocol.AggregatedRunQueryResponse
import net.shrine.protocol.AggregatedReadQueryResultResponse
import net.shrine.utilities.scanner.components.HasBroadcastServiceComponent
import net.shrine.utilities.scanner.components.HasExecutionContextComponent


/**
 * @author clint
 * @date Mar 14, 2013
 */
trait BroadcastServiceScannerClient extends ScannerClient with Loggable { 
  self: HasExecutionContextComponent with HasBroadcastServiceComponent =>
  
  val projectId: String
  
  val authn: AuthenticationInfo
  
  private val waitTimeMs = 10000
  
  //Don't ask for an aggregated (summed) result, since we'll get at most one result back in any case 
  private val runQueryAggregatorSource = Aggregators.forRunQueryRequest(false) _
  
  override def query(term: String): Future[TermResult] = {
    import Scanner.QueryDefaults._

    info(s"Querying for '$term'")
    
    val request = RunQueryRequest(projectId, waitTimeMs, authn, -1L, topicId, outputTypes, toQueryDef(term))
    
    val futureResponse = broadcastService.sendAndAggregate(request, runQueryAggregatorSource(request), false)
    
    def toTermResult(runQueryResponse: AggregatedRunQueryResponse): TermResult = {
      val termResultOption = for {
        shrineQueryResult <- runQueryResponse.results.headOption
      } yield TermResult(runQueryResponse.queryId, term, shrineQueryResult.statusType, shrineQueryResult.setSize)
      
      //TODO: Is this the right query id to use here?
      termResultOption.getOrElse(errorTermResult(runQueryResponse.queryId, term))
    }
    
    futureResponse.collect { case resp: AggregatedRunQueryResponse => resp }.map(toTermResult)
  }
  
  override def retrieveResults(termResult: TermResult): Future[TermResult] = {
    info(s"Retrieving results for previously-incomplete query for '${termResult.term}'")
    
    val request = ReadQueryResultRequest(projectId, waitTimeMs, authn, termResult.networkQueryId)
    
    val futureResponse = broadcastService.sendAndAggregate(request, new ReadQueryResultAggregator(termResult.networkQueryId, false), false)
    
    def toTermResult(readQueryResultResponse: AggregatedReadQueryResultResponse): TermResult = {
      val termResultOption = for {
        shrineQueryResult <- readQueryResultResponse.results.headOption
      } yield TermResult(termResult.networkQueryId, termResult.term, shrineQueryResult.statusType, shrineQueryResult.setSize)
      
      //TODO: Is this the right query id to use here?
      termResultOption.getOrElse(errorTermResult(termResult.networkQueryId, termResult.term))
    }
    
    futureResponse.collect { case resp: AggregatedReadQueryResultResponse => resp }.map(toTermResult)
  }
}