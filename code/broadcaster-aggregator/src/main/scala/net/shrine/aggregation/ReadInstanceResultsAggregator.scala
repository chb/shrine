package net.shrine.aggregation

import scala.Option.option2Iterable

import org.spin.message.Result

import net.shrine.protocol.ReadInstanceResultsResponse
import net.shrine.protocol.AggregatedReadInstanceResultsResponse
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.ResultOutputType.PATIENTSET
import net.shrine.protocol.ResultOutputType.PATIENT_COUNT_XML

/**
 * @author Bill Simons
 * @author Clint Gilbert
 * @date 6/13/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadInstanceResultsAggregator(shrineNetworkQueryId: Long, showAggregation: Boolean) extends 
    StoredResultsAggregator[ReadInstanceResultsResponse, AggregatedReadInstanceResultsResponse](
        shrineNetworkQueryId, 
        showAggregation,
        Some("No results available"), 
        Some("No results available")) {

  import ResultOutputType._
  
  private val setType = Some(PATIENT_COUNT_XML)
  private val statusType = QueryResult.StatusType.Finished
  private val allowedSetTypes = ResultOutputType.values.toSet

  protected override def consolidateQueryResults(queryResultsFromAllValidResponses: Seq[(SpinResultEntry, Seq[QueryResult])]): Seq[QueryResult] = {
    queryResultsFromAllValidResponses.flatMap { case (spinResult, resultsFromOneResponse) =>
      for {
        firstResult <- resultsFromOneResponse.headOption
        newResult = firstResult.withResultType(PATIENT_COUNT_XML) //Eh?
      } yield {
        transformResult(newResult, spinResult.spinResultMetadata)
      }
    }
  }
  
  protected override def makeAggregatedResult(queryResults: Seq[QueryResult]): Option[QueryResult] = {
    val totalSize = queryResults.map(_.setSize).sum

    queryResults.headOption.map(_.copy(instanceId = shrineNetworkQueryId, resultType = setType, setSize = totalSize, description = Some("Aggregated Count"), statusType = statusType, statusMessage = None))
  }
  
  /**
   * Default implementation only replaces the description with the spinResult description; Subclasses can override
   * and do something more interesting.
   */
  protected[aggregation] def transformResult(queryResult: QueryResult, spinResult: Result): QueryResult = queryResult.withDescription(spinResult.getDescription)
}