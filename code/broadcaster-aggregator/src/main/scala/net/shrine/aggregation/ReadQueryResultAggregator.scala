package net.shrine.aggregation

import net.shrine.aggregation.BasicAggregator.Valid
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.ReadQueryResultResponse
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.ResultOutputType._
import QueryResult.StatusType.Finished

/**
 * @author clint
 * @date Nov 6, 2012
 */
//TODO: TEST!!!
final class ReadQueryResultAggregator(shrineNetworkQueryId: Long, showAggregation: Boolean) extends 
    StoredResultsAggregator[ReadQueryResultResponse](
        shrineNetworkQueryId: Long, 
        showAggregation: Boolean,
        Some("No results available"), 
        Some("No results available")) {
  
  protected override def consolidateQueryResults(queryResultsFromAllValidResponses: Seq[(SpinResultEntry, Seq[QueryResult])]): Seq[QueryResult] = {
    queryResultsFromAllValidResponses.flatMap { case (_, resultsFromOneResponse) => resultsFromOneResponse.headOption }
  }
  
  protected override def makeAggregatedResult(queryResults: Seq[QueryResult]): Option[QueryResult] = {
    val totalSize = queryResults.map(_.setSize).sum
          
    queryResults.headOption.map(_.copy(instanceId = shrineNetworkQueryId, resultType = Some(PATIENT_COUNT_XML), setSize = totalSize, description = Some("Aggregated Count"), statusType = Finished.name, statusMessage = None))
  }
}
