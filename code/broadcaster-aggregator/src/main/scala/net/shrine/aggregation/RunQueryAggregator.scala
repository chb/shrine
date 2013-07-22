package net.shrine.aggregation

import org.spin.tools.NetworkTime
import java.util.Date
import org.spin.query.message.headers.Result
import net.shrine.protocol.{ErrorResponse, ShrineResponse, QueryResult, RunQueryResponse}
import net.shrine.util.Loggable
import net.shrine.aggregation.BasicAggregator.Valid
import net.shrine.protocol.ResultOutputType

/**
 *
 *
 * @author Justin Quan
 * @author Clint Gilbert
 * @link http://chip.org
 * Date: 8/11/11
 */
class RunQueryAggregator(
    private val queryId: Long,
    private val userId: String,
    private val groupId: String,
    private val requestXml: String,
    private val queryInstance: Long,
    private val doAggregation: Boolean) extends PackagesErrorsAggregator[RunQueryResponse](errorMessage = None, invalidMessage = Some("Unexpected response")) {

  /* We need to override this some place in Carranet, we need this method to change descriptions of responses */
  protected def transformResult(n: QueryResult, metaData: Result): QueryResult = n.withDescription(metaData.getDescription)

  private[aggregation] final override def makeResponse(validResponses: Seq[Valid[RunQueryResponse]], errorResponses: Seq[QueryResult], invalidResponses: Seq[QueryResult]): ShrineResponse = {


    import ResultOutputType._

    val results = validResponses.flatMap {
      case Valid(spinResult, response) =>
        response.results.map(transformResult(_,spinResult.spinResultMetadata))
    }

    val counts = validResponses.map {
      case Valid(spinResult, response) =>
        val setResultOption = response.results.find(_.resultType.equalsIgnoreCase(PATIENTSET.name)).map(_.setSize)

        val countResultOption = response.results.find(_.resultType.equalsIgnoreCase(PATIENT_COUNT_XML.name)).map(_.setSize)

        setResultOption.getOrElse(countResultOption.getOrElse(0L))
    }

    val now = (new NetworkTime).getXMLGregorianCalendar

    val aggResults =
      if(doAggregation) {
        val sumResult = new QueryResult(0L, queryInstance, PATIENT_COUNT_XML.name, counts.sum, now, now, "TOTAL COUNT", "FINISHED")

        results :+ sumResult
      }
      else {
        results
      }

    new RunQueryResponse(queryId, now, userId, groupId, requestXml, queryInstance, aggResults ++ errorResponses ++ invalidResponses)
  }
}