package net.shrine.aggregation

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import org.spin.message.Result
import net.shrine.protocol.ReadQueryResultResponse
import net.shrine.protocol.AggregatedReadQueryResultResponse
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ReadQueryResultResponse
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Nov 7, 2012
 */
final class ReadQueryResultAggregatorTest extends TestCase with ShouldMatchersForJUnit {
  private val queryId = 12345L

  import ResultOutputType._

  private def asAggregatedResponse(resp: ShrineResponse) = resp.asInstanceOf[AggregatedReadQueryResultResponse]

  private val setSize1 = 123L
  private val setSize2 = 456L
  private val totalSetSize = setSize1 + setSize2

  private val queryResult1 = QueryResult(1L, 2L, Some(PATIENT_COUNT_XML), setSize1, None, None, None, QueryResult.StatusType.Finished, None, Map.empty)
  private val queryResult2 = QueryResult(1L, 2L, Some(PATIENT_COUNT_XML), setSize2, None, None, None, QueryResult.StatusType.Finished, None, Map.empty)

  private val response1 = ReadQueryResultResponse(queryId, queryResult1)
  private val response2 = ReadQueryResultResponse(queryId, queryResult2)

  private val result1 = SpinResultEntry(response1.toXmlString, null)
  private val result2 = SpinResultEntry(response2.toXmlString, null)

  private val errors = Seq(ErrorResponse("blarg"), ErrorResponse("glarg"))

  private val expectedErrorQueryResults = errors.map {
    case ErrorResponse(message) =>
      QueryResult.errorResult(Some(message), "No results available")
  }

  @Test
  def testAggregate {
    val aggregator = new ReadQueryResultAggregator(queryId, true)

    val response = asAggregatedResponse(aggregator.aggregate(Seq(result1, result2), Nil))

    val Seq(actualQueryResult1, actualQueryResult2, aggregatedQueryResult) = response.results

    actualQueryResult1 should equal(queryResult1)
    actualQueryResult2 should equal(queryResult2)

    val expectedAggregatedResult = queryResult1.withSetSize(totalSetSize).withInstanceId(queryId).withDescription("Aggregated Count")

    aggregatedQueryResult should equal(expectedAggregatedResult)
  }

  @Test
  def testAggregateNoAggregatedResult {
    val aggregator = new ReadQueryResultAggregator(queryId, false)

    val response = asAggregatedResponse(aggregator.aggregate(Seq(result1, result2), Nil))

    val Seq(actualQueryResult1, actualQueryResult2) = response.results

    actualQueryResult1 should equal(queryResult1)
    actualQueryResult2 should equal(queryResult2)
  }

  @Test
  def testAggregateNoResponses {
    for (doAggregation <- Seq(true, false)) {
      val aggregator = new ReadQueryResultAggregator(queryId, true)

      val response = asAggregatedResponse(aggregator.aggregate(Nil, Nil))

      response.queryId should equal(queryId)
      response.results.isEmpty should be(true)
    }
  }

  @Test
  def testAggregateOnlyErrorResponses {
    for (doAggregation <- Seq(true, false)) {
      val aggregator = new ReadQueryResultAggregator(queryId, true)

      val response = asAggregatedResponse(aggregator.aggregate(Nil, errors))

      response.queryId should equal(queryId)
      response.results should equal(expectedErrorQueryResults)
    }
  }

  @Test
  def testAggregateSomeErrors {
    val aggregator = new ReadQueryResultAggregator(queryId, true)

    val response = asAggregatedResponse(aggregator.aggregate(Seq(result1, result2), errors))

    val Seq(actualQueryResult1, actualQueryResult2, aggregatedQueryResult, actualErrorQueryResults @ _*) = response.results

    actualQueryResult1 should equal(queryResult1)
    actualQueryResult2 should equal(queryResult2)

    val expectedAggregatedResult = queryResult1.withSetSize(totalSetSize).withInstanceId(queryId).withDescription("Aggregated Count")

    aggregatedQueryResult should equal(expectedAggregatedResult)
    
    actualErrorQueryResults should equal(expectedErrorQueryResults)
  }
  
  @Test
  def testAggregateSomeDownstreamErrors {
    val aggregator = new ReadQueryResultAggregator(queryId, true)

    val result3 = SpinResultEntry(errors.head.toXmlString, null)
    val result4 = SpinResultEntry(errors.last.toXmlString, null)
    
    val response = asAggregatedResponse(aggregator.aggregate(Seq(result1, result2, result3, result4), Nil))

    val Seq(actualQueryResult1, actualQueryResult2, aggregatedQueryResult, actualErrorQueryResults @ _*) = response.results

    actualQueryResult1 should equal(queryResult1)
    actualQueryResult2 should equal(queryResult2)

    val expectedAggregatedResult = queryResult1.withSetSize(totalSetSize).withInstanceId(queryId).withDescription("Aggregated Count")

    aggregatedQueryResult should equal(expectedAggregatedResult)
    
    actualErrorQueryResults should equal(expectedErrorQueryResults)
  }
}