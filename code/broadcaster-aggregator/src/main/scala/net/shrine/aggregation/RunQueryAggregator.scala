package net.shrine.aggregation

import org.spin.tools.NetworkTime
import net.shrine.protocol.{ ShrineResponse, QueryResult, RunQueryResponse }
import net.shrine.aggregation.BasicAggregator.Valid
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.query.QueryDefinition
import org.spin.message.Result
import net.shrine.util.Util
import net.shrine.protocol.AggregatedRunQueryResponse

/**
 *
 *
 * @author Justin Quan
 * @author Clint Gilbert
 * @link http://chip.org
 * Date: 8/11/11
 */
class RunQueryAggregator(
  val queryId: Long,
  val userId: String,
  val groupId: String,
  val requestQueryDefinition: QueryDefinition,
  val addAggregatedResult: Boolean) extends PackagesErrorsAggregator[RunQueryResponse](errorMessage = None, invalidMessage = Some("Unexpected response")) {

  def withQueryId(qId: Long) = new RunQueryAggregator(qId, userId, groupId, requestQueryDefinition, addAggregatedResult)
  
  /* We need to override this some place in Carranet, we need this method to change descriptions of responses */
  protected def transformResult(n: QueryResult, metaData: Result): QueryResult = n.withDescription(metaData.getDescription)

  import RunQueryAggregator._
  
  private[aggregation] final override def makeResponse(validResponses: Seq[Valid[RunQueryResponse]], errorResponses: Seq[QueryResult], invalidResponses: Seq[QueryResult]): ShrineResponse = {

    val results = validResponses.flatMap {
      case Valid(spinResult, response) =>
        response.results.map(transformResult(_, spinResult.spinResultMetadata))
    }

    import ResultOutputType._

    val counts = validResponses.map {
      case Valid(spinResult, response) =>
        val setResultOption = response.results.find(_.resultTypeIs(PATIENTSET)).map(_.setSize)

        val countResultOption = response.results.find(_.resultTypeIs(PATIENT_COUNT_XML)).map(_.setSize)

        (setResultOption orElse countResultOption).getOrElse(0L)
    }

    val now = Util.now

    val aggResults = {
      if (addAggregatedResult) {
        val sumResult = new QueryResult(0L, invalidInstanceId, PATIENT_COUNT_XML, counts.sum, now, now, "TOTAL COUNT", QueryResult.StatusType.Finished)

        results :+ sumResult
      } else {
        results
      }
    }

    AggregatedRunQueryResponse(queryId, now, userId, groupId, requestQueryDefinition, invalidInstanceId, aggResults ++ errorResponses ++ invalidResponses)
  }
}

object RunQueryAggregator {
  val invalidInstanceId = -1L
}