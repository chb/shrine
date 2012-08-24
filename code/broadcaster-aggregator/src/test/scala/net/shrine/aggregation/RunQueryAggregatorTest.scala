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

/**
 *
 *
 * @author Justin Quan
 * @link http://chip.org
 * Date: 8/12/11
 */
final class RunQueryAggregatorTest extends AssertionsForJUnit with ShouldMatchersForJUnit {
  
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

    val result2 = new SpinResultEntry(rqr2.toXml.toString(), new Result(null, "description1", null, null))
    val result1 = new SpinResultEntry(rqr1.toXml.toString(), new Result(null, "description2", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestQueryDef, queryInstanceId, true)
    
    //TODO: test handling error responses
    val actual = aggregator.aggregate(Vector(result1, result2), Nil).asInstanceOf[RunQueryResponse]
    
    assertTrue(actual.isInstanceOf[RunQueryResponse])

    actual.results.size should equal(5)
    actual.results.filter(_.resultType == PATIENT_COUNT_XML).size should equal(3)
    actual.results.filter(_.resultType == PATIENTSET).size should equal(2)
    actual.results.filter(_.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")).size should equal(1)
    actual.results.filter(_.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")).head.setSize should equal(20)
    actual.queryName should equal(queryName)
  }

  @Test
  def testAggCount() {
    val qrSet = new QueryResult(2L, queryInstanceId, PATIENTSET, 10L, now, now, "Desc", "FINISHED")

    val rqr1 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, List(qrSet))
    val rqr2 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, List(qrSet))

    val result1 = new SpinResultEntry(rqr1.toXml.toString(), new Result(null, "description1", null, null))
    val result2 = new SpinResultEntry(rqr2.toXml.toString(), new Result(null, "description2", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestQueryDef, queryInstanceId, true)
    
    //TODO: test handling error responses
    val actual = aggregator.aggregate(Vector(result1, result2), Nil).asInstanceOf[RunQueryResponse]
    
    assertTrue(actual.isInstanceOf[RunQueryResponse])
    actual.results.filter(_.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")).head.setSize should equal(20)
  }

  @Test
  def testHandleErrorResponse() {
    val qrCount = new QueryResult(1L, queryInstanceId, PATIENT_COUNT_XML, 10L, now, now, "Desc", "FINISHED")

    val rqr1 = new RunQueryResponse(queryId, now, userId, groupId, requestQueryDef, queryInstanceId, List(qrCount))
    val errorMessage = "error message"
    val errorResponse = new ErrorResponse(errorMessage)

    val result2 = new SpinResultEntry(errorResponse.toXml.toString(), new Result(null, "description1", null, null))
    val result1 = new SpinResultEntry(rqr1.toXml.toString(), new Result(null, "description2", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestQueryDef, queryInstanceId, true)
    
    //TODO: test handling error responses
    val actual = aggregator.aggregate(Vector(result1, result2), Nil).asInstanceOf[RunQueryResponse]
    
    assertTrue(actual.isInstanceOf[RunQueryResponse])
    actual.results.size should equal(3)
    actual.results.filter(_.resultType == PATIENT_COUNT_XML).head.setSize should equal(10)
    actual.results.filter(_.statusType.equalsIgnoreCase("ERROR")).head.statusMessage should equal(Some(errorMessage))
    actual.results.filter(_.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")).head.setSize should equal(10)
  }

}
