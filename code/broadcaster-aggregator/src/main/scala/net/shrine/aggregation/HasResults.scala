package net.shrine.aggregation

import net.shrine.protocol.QueryResult
import net.shrine.protocol.ReadQueryResultResponse
import net.shrine.protocol.ReadInstanceResultsResponse

/**
 * @author clint
 * @date Nov 9, 2012
 */
trait HasResults[R] {
  def resultsFrom(response: R): Seq[QueryResult]
  
  def makeResponse(shrineNetworkQueryId: Long, queryResults: Seq[QueryResult]): R
}

object HasResults {
  implicit val readQueryResultResponseHasResults: HasResults[ReadQueryResultResponse] = new HasResults[ReadQueryResultResponse] {
    override def resultsFrom(response: ReadQueryResultResponse) = response.results
    
    override def makeResponse(shrineNetworkQueryId: Long, queryResults: Seq[QueryResult]) = ReadQueryResultResponse(shrineNetworkQueryId, queryResults)
  }
  
  implicit val readInstanceResultsResponseHasResults: HasResults[ReadInstanceResultsResponse] = new HasResults[ReadInstanceResultsResponse] {
    override def resultsFrom(response: ReadInstanceResultsResponse) = response.results
    
    override def makeResponse(shrineNetworkQueryId: Long, queryResults: Seq[QueryResult]) = ReadInstanceResultsResponse(shrineNetworkQueryId, queryResults)
  }
}