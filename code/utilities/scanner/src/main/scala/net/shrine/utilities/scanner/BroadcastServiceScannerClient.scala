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


/**
 * @author clint
 * @date Mar 14, 2013
 */
final case class BroadcastServiceScannerClient(
    val projectId: String,
    val authn: AuthenticationInfo,
    val broadcastService: BroadcastService) extends ScannerClient with Loggable {
  
  //private val peerGroupToQuery = DefaultPeerGroups.LOCAL.name
  
  private val waitTimeMs = 10000
  
  //Don't ask for an aggregated (summed) result, since we'll get at most one result back in any case 
  private val runQueryAggregatorSource = Aggregators.forRunQueryRequest(false) _
  
  import ExecutionContext.Implicits.global
  
  override def query(term: String): Future[TermResult] = {
    import Scanner.QueryDefaults._

    info(s"Querying for '$term'")
    
    val request = RunQueryRequest(projectId, waitTimeMs, authn, -1L, topicId, outputTypes, toQueryDef(term))
    
    val futureResponse = broadcastService.sendAndAggregate(request, runQueryAggregatorSource(request), false)
    
    def toTermResult(runQueryResponse: RunQueryResponse): TermResult = {
      val termResultOption = for {
        shrineQueryResult <- runQueryResponse.results.headOption
      } yield TermResult(runQueryResponse.queryId, term, shrineQueryResult.statusType, shrineQueryResult.setSize)
      
      //TODO: Is this the right query id to use here?
      termResultOption.getOrElse(errorTermResult(runQueryResponse.queryId, term))
    }
    
    futureResponse.collect { case resp: RunQueryResponse => resp }.map(toTermResult)
  }
  
  override def retrieveResults(termResult: TermResult): Future[TermResult] = {
    info(s"Retrieving results for previously-incomplete query for '${termResult.term}'")
    
    val request = ReadQueryResultRequest(projectId, waitTimeMs, authn, termResult.networkQueryId)
    
    val futureResponse = broadcastService.sendAndAggregate(request, new ReadQueryResultAggregator(termResult.networkQueryId, false), false)
    
    def toTermResult(readQueryResultResponse: ReadQueryResultResponse): TermResult = {
      val termResultOption = for {
        shrineQueryResult <- readQueryResultResponse.results.headOption
      } yield TermResult(termResult.networkQueryId, termResult.term, shrineQueryResult.statusType, shrineQueryResult.setSize)
      
      //TODO: Is this the right query id to use here?
      termResultOption.getOrElse(errorTermResult(termResult.networkQueryId, termResult.term))
    }
    
    futureResponse.collect { case resp: ReadQueryResultResponse => resp }.map(toTermResult)
  }
}