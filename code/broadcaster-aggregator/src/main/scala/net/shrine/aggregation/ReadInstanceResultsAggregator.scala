package net.shrine.aggregation

import net.shrine.aggregation.BasicAggregator.Valid
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ReadInstanceResultsResponse
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.ShrineResponse
import org.spin.message.Result

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
class ReadInstanceResultsAggregator(
    instanceId: Long, showAggregation: Boolean) extends PackagesErrorsAggregator[ReadInstanceResultsResponse](Some("No results available"), Some("No results available")) {

  import ResultOutputType._
  
  private val setType = Some(PATIENTSET)
  private val statusType = "FINISHED"
  private val allowedSetTypes = ResultOutputType.values.toSet

  /**
   * Default implementation only replaces the description with the spinResult description; Subclasses can override
   * and do something more interesting.
   */
  protected def transformResult(queryResult: QueryResult, spinResult: Result): QueryResult = queryResult.withDescription(spinResult.getDescription)

  private[aggregation] final override def makeResponse(validResponses: Seq[Valid[ReadInstanceResultsResponse]], errorResponses: Seq[QueryResult], invalidResponses: Seq[QueryResult]): ShrineResponse = {

    def isAllowedSetType(result: QueryResult) = result.resultType match {
      case Some(rt) => allowedSetTypes.contains(rt)
      case _ => false
    }
    
    val queryResults =
      for {
        Valid(spinResult, response) <- validResponses
        goodResults = response.results.filter(isAllowedSetType)
        firstResult <- goodResults.headOption
        newResult = firstResult.withResultType(PATIENT_COUNT_XML)
      } yield {
        transformResult(newResult, spinResult.spinResultMetadata)
      }

    val finalQueryResults =
      if(!queryResults.isEmpty && showAggregation) {
        val totalSize = queryResults.map(_.setSize).sum
        val newResult = queryResults.head.copy(instanceId = instanceId, resultType = setType, setSize = totalSize, description = Some("Aggregated Count"), statusType = statusType, statusMessage = None)

        queryResults :+ newResult
      }
      else {
        queryResults
      }

    new ReadInstanceResultsResponse(instanceId, finalQueryResults ++ errorResponses ++ invalidResponses)
  }
}