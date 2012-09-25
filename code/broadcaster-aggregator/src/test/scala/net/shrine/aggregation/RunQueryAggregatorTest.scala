package net.shrine.aggregation

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.spin.tools.NetworkTime
import org.junit.Test
import org.junit.Assert._
import java.util.Date
import net.shrine.protocol._
import org.spin.message.Result
import xml.Utility
import net.shrine.util.XmlUtil
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.ResultOutputType._
import junit.framework.TestCase
import net.shrine.protocol.I2b2ResultEnvelope.Column

/**
 *
 *
 * @author Justin Quan
 * @link http://chip.org
 * Date: 8/12/11
 */
final class RunQueryAggregatorTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {
  
  private val queryId = 1234L
  private val queryName = "someQueryName"
  private val now = NetworkTime.makeXMLGregorianCalendar(new Date())
  private val userId = "user"
  private val groupId = "group"
  private val requestQueryDef = QueryDefinition(queryName, Term("""\\i2b2\i2b2\Demographics\Age\0-9 years old\"""))
  private val requestQueryDefString = requestQueryDef.toI2b2String
  private val queryInstanceId = 9999L

  @Test
  def testAggregate {
    val qrCount = new QueryResult(1L, queryInstanceId, PATIENT_COUNT_XML, 10L, now, now, "Desc", "FINISHED")
    val qrSet = new QueryResult(2L, queryInstanceId, PATIENTSET, 10L, now, now, "Desc", "FINISHED")

    val rqr1 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, List(qrCount, qrSet))
    val rqr2 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, List(qrCount, qrSet))

    val result2 = new SpinResultEntry(rqr2.toXmlString, new Result(null, "description1", null, null))
    val result1 = new SpinResultEntry(rqr1.toXmlString, new Result(null, "description2", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestQueryDef, queryInstanceId, true)
    
    val actual = aggregator.aggregate(Vector(result1, result2), Nil).asInstanceOf[RunQueryResponse]

    actual.results.size should equal(5)
    actual.results.filter(_.resultTypeIs(PATIENT_COUNT_XML)).size should equal(3)
    actual.results.filter(_.resultTypeIs(PATIENTSET)).size should equal(2)
    actual.results.filter(hasTotalCount).size should equal(1)
    actual.results.filter(hasTotalCount).head.setSize should equal(20)
    actual.queryName should equal(queryName)
  }

  @Test
  def testAggCount {
    val qrSet = new QueryResult(2L, queryInstanceId, PATIENTSET, 10L, now, now, "Desc", "FINISHED")

    val rqr1 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, List(qrSet))
    val rqr2 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, List(qrSet))

    val result1 = new SpinResultEntry(rqr1.toXmlString, new Result(null, "description1", null, null))
    val result2 = new SpinResultEntry(rqr2.toXmlString, new Result(null, "description2", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestQueryDef, queryInstanceId, true)
    
    //TODO: test handling error responses
    val actual = aggregator.aggregate(Vector(result1, result2), Nil).asInstanceOf[RunQueryResponse]
    
    actual.results.filter(_.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")).head.setSize should equal(20)
  }

  @Test
  def testHandleErrorResponse {
    val qrCount = new QueryResult(1L, queryInstanceId, PATIENT_COUNT_XML, 10L, now, now, "Desc", "FINISHED")

    val rqr1 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, List(qrCount))
    val errorMessage = "error message"
    val errorResponse = new ErrorResponse(errorMessage)

    val result1 = new SpinResultEntry(rqr1.toXmlString, new Result(null, "description1", null, null))
    val result2 = new SpinResultEntry(errorResponse.toXmlString, new Result(null, "description2", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestQueryDef, queryInstanceId, true)
    
    val actual = aggregator.aggregate(Vector(result1, result2), Nil).asInstanceOf[RunQueryResponse]
    
    actual.results.size should equal(3)
    
    actual.results.filter(_.resultTypeIs(PATIENT_COUNT_XML)).head.setSize should equal(10)
    actual.results.filter(_.statusType.equalsIgnoreCase("ERROR")).head.statusMessage should equal(Some(errorMessage))
    actual.results.filter(hasTotalCount).head.setSize should equal(10)
  }

  @Test
  def testAggregateResponsesWithBreakdowns {
    def toColumnTuple(i: Int) = ("x" + i, i.toLong)
    
    val breakdowns1 = Map.empty ++ ResultOutputType.breakdownTypes.map { resultType =>
      resultType -> I2b2ResultEnvelope(resultType, (1 to 10).map(toColumnTuple).toMap)
    }
    
    val breakdowns2 = Map.empty ++ ResultOutputType.breakdownTypes.map { resultType =>
      resultType -> I2b2ResultEnvelope(resultType, (11 to 20).map(toColumnTuple).toMap)
    }
    
    val qr1 = new QueryResult(1L, queryInstanceId, Some(PATIENT_COUNT_XML), 10L, Some(now), Some(now), Some("Desc"), "FINISHED", None, breakdowns1)
    
    val qr2 = new QueryResult(2L, queryInstanceId, Some(PATIENT_COUNT_XML), 20L, Some(now), Some(now), Some("Desc"), "FINISHED", None, breakdowns2)

    val rqr1 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, Seq(qr1))
    
    val rqr2 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, Seq(qr2))

    val result1 = new SpinResultEntry(rqr1.toXmlString, new Result(null, "description2", null, null))
    
    val result2 = new SpinResultEntry(rqr2.toXmlString, new Result(null, "description1", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestQueryDef, queryInstanceId, true)
    
    val actual = aggregator.aggregate(Seq(result1, result2), Nil).asInstanceOf[RunQueryResponse]
    
    actual.results.size should equal(3)
    actual.results.filter(hasTotalCount).size should equal(1)
    
    val Seq(actualQr1, actualQr2, actualQr3) = actual.results.filter(_.resultTypeIs(PATIENT_COUNT_XML))
    
    actualQr1.setSize should equal(10)
    actualQr2.setSize should equal(20)
    actualQr3.setSize should equal(30)
    
    actualQr1.breakdowns should equal(breakdowns1)
    actualQr2.breakdowns should equal(breakdowns2)
    
    actualQr3.breakdowns.isEmpty should be(true)
  }
  
  private def hasTotalCount(result: QueryResult) = result.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")
}
