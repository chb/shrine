package net.shrine.aggregation

import org.junit.Test
import org.junit.Assert.{assertNotNull, assertTrue}
import org.spin.tools.NetworkTime
import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.spin.message.Result
import net.shrine.protocol.{ErrorResponse, QueryResult, ReadInstanceResultsResponse}
import net.shrine.protocol.ResultOutputType._
import junit.framework.TestCase


/**
 * @author Bill Simons
 * @date 6/13/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ReadInstanceResultsAggregatorTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {
  
  @Test
  def testAggregate {
    val instanceId = 123L
    val startDate = new NetworkTime().getXMLGregorianCalendar
    val endDate = new NetworkTime().getXMLGregorianCalendar
    val queryResult1 = new QueryResult(1L, instanceId, PATIENT_COUNT_XML, 12, startDate, endDate, "FINISHED")
    val queryResult1_set = new QueryResult(1L, instanceId, PATIENTSET, 12, startDate, endDate, "FINISHED")

    val queryResult2 = new QueryResult(2L, instanceId, PATIENTSET, 14, startDate, endDate, "FINISHED")
    val aggregator = new ReadInstanceResultsAggregator(instanceId, true)
    val aggregatorNoAggregate = new ReadInstanceResultsAggregator(instanceId, false)

    val response1 = new ReadInstanceResultsResponse(instanceId, Vector(queryResult1, queryResult1_set))
    val response2 = new ReadInstanceResultsResponse(instanceId, Vector(queryResult2))

    val description1 = "NODE1"
    val description2 = "NODE2"
    val result1 = new SpinResultEntry(response1.toXml.toString(), new Result(null, description1, null, null))
    val result2 = new SpinResultEntry(response2.toXml.toString(), new Result(null, description2, null, null))

    {
    	//TODO: test handling error responses
	    val actual = aggregator.aggregate(Seq(result1, result2), Nil).asInstanceOf[ReadInstanceResultsResponse]
	    assertTrue(actual.isInstanceOf[ReadInstanceResultsResponse])
	
	    assertNotNull(actual)
	    assertNotNull(actual.results)
	    actual.results.size should equal(3)
	    assertTrue(actual.results.contains(queryResult1.withDescription(description1).withResultType(PATIENT_COUNT_XML)))
	    assertTrue(actual.results.contains(queryResult2.withDescription(description2).withResultType(PATIENT_COUNT_XML)))
    }

    {
    	//TODO: test handling error responses
	    val actual = aggregatorNoAggregate.aggregate(Seq(result1, result2), Nil).asInstanceOf[ReadInstanceResultsResponse]
	    assertTrue(actual.isInstanceOf[ReadInstanceResultsResponse])

	    assertNotNull(actual)
	    assertNotNull(actual.results)
	    actual.results.size should equal(2)
	    assertTrue(actual.results.contains(queryResult1.withDescription(description1).withResultType(PATIENT_COUNT_XML)))
	    assertTrue(actual.results.contains(queryResult2.withDescription(description2).withResultType(PATIENT_COUNT_XML)))
    }
  }

  @Test
  def testAggregateWithError {
    val instanceId = 123L
    val startDate = (new NetworkTime).getXMLGregorianCalendar
    val endDate = (new NetworkTime).getXMLGregorianCalendar
    val queryResult = new QueryResult(1L, instanceId, PATIENT_COUNT_XML, 12, startDate, endDate, "FINISHED")
    val aggregator = new ReadInstanceResultsAggregator(instanceId, true)
    val errorMessage = "you are an error"

    val patientCountResponse = new ReadInstanceResultsResponse(instanceId, Vector(queryResult))
    val errorResponse = new ErrorResponse(errorMessage)

    val patientCountNodeDescription = "NODE1"
    val errorNodeDescription = "NODE2"
    val result1 = new SpinResultEntry(patientCountResponse.toXml.toString(), new Result(null, patientCountNodeDescription, null, null))
    val result2 = new SpinResultEntry(errorResponse.toXml.toString(), new Result(null, errorNodeDescription, null, null))

    //TODO: test handling error responses
    val actual = aggregator.aggregate(Seq(result1, result2), Nil).asInstanceOf[ReadInstanceResultsResponse]
    assertTrue(actual.isInstanceOf[ReadInstanceResultsResponse])
    assertNotNull(actual)
    assertNotNull(actual.results)
    actual.results.size should equal(3)
    assertTrue(actual.results.contains(queryResult.withDescription(patientCountNodeDescription)))
    assertTrue(actual.results.contains(QueryResult.errorResult(Option(errorNodeDescription), "No results available")))
  }
}