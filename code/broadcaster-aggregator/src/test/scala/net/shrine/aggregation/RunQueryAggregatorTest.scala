package net.shrine.aggregation

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.spin.tools.NetworkTime
import org.junit.Test
import org.junit.Assert._
import java.util.Date
import net.shrine.protocol._
import org.spin.query.message.headers.Result
import xml.Utility
import net.shrine.util.XmlUtil

/**
 *
 *
 * @author Justin Quan
 * @link http://chip.org
 * Date: 8/12/11
 */

class RunQueryAggregatorTest extends AssertionsForJUnit with ShouldMatchersForJUnit {
    val queryId = 1234L
    val queryName = "someQueryName"
    val now = NetworkTime.makeXMLGregorianCalendar(new Date())
    val userId = "user"
    val groupId = "group"
    val requestXml = XmlUtil.stripWhitespace(
      <query_definition>
        <query_name>{queryName}</query_name>
        <specificity_scale>0</specificity_scale>
        <panel>
          <panel_number>1</panel_number>
          <invert>0</invert>
          <total_item_occurrences>1</total_item_occurrences>
          <item>
            <hlevel>3</hlevel>
            <item_name>0-9 years old</item_name>
            <item_key>\\i2b2\i2b2\Demographics\Age\0-9 years old\</item_key>
            <tooltip>Demographic \ Age \ 0-9 years old</tooltip>
            <class>ENC</class>
            <constrain_by_date>
            </constrain_by_date>
            <item_icon>FA</item_icon>
            <item_is_synonym>false</item_is_synonym>
          </item>
        </panel>
      </query_definition>).toString

    val queryInstanceId = 9999L


  @Test
  def testAggregate() {
    val qrCount = new QueryResult(1L, queryInstanceId, "PATIENT_COUNT_XML", 10L, now, now, "Desc", "FINISHED")
    val qrSet = new QueryResult(2L, queryInstanceId, "PATIENTSET", 10L, now, now, "Desc", "FINISHED")

    val rqr1 = new RunQueryResponse(queryId, now, userId, groupId, requestXml, queryInstanceId, List(qrCount, qrSet))
    val rqr2 = new RunQueryResponse(queryId, now, userId, groupId, requestXml, queryInstanceId, List(qrCount, qrSet))

    val result2 = new SpinResultEntry(rqr2.toXml.toString(), new Result(null, "description1", null, null))
    val result1 = new SpinResultEntry(rqr1.toXml.toString(), new Result(null, "description2", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestXml, queryInstanceId, true)
    val actual = aggregator.aggregate(Vector(result1, result2)).asInstanceOf[RunQueryResponse]
    assertTrue(actual.isInstanceOf[RunQueryResponse])

    actual.results.size should equal(5)
    actual.results.filter(x=>x.resultType.equalsIgnoreCase("PATIENT_COUNT_XML")).size should equal(3)
    actual.results.filter(x=>x.resultType.equalsIgnoreCase("PATIENTSET")).size should equal(2)
    actual.results.filter(x=>x.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")).size should equal(1)
    actual.results.filter(x=>x.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")).head.setSize should equal(20)
    actual.queryName should equal(queryName)

  }

  @Test
  def testAggCount() {
    val qrSet = new QueryResult(2L, queryInstanceId, "PATIENTSET", 10L, now, now, "Desc", "FINISHED")

    val rqr1 = new RunQueryResponse(queryId, now, userId, groupId, requestXml, queryInstanceId, List(qrSet))
    val rqr2 = new RunQueryResponse(queryId, now, userId, groupId, requestXml, queryInstanceId, List(qrSet))

    val result1 = new SpinResultEntry(rqr1.toXml.toString(), new Result(null, "description1", null, null))
    val result2 = new SpinResultEntry(rqr2.toXml.toString(), new Result(null, "description2", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestXml, queryInstanceId, true)
    val actual = aggregator.aggregate(Vector(result1, result2)).asInstanceOf[RunQueryResponse]
    assertTrue(actual.isInstanceOf[RunQueryResponse])
    actual.results.filter(x=>x.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")).head.setSize should equal(20)
  }

  @Test
  def testHandleErrorResponse() {
    val qrCount = new QueryResult(1L, queryInstanceId, "PATIENT_COUNT_XML", 10L, now, now, "Desc", "FINISHED")

    val rqr1 = new RunQueryResponse(queryId, now, userId, groupId, requestXml, queryInstanceId, List(qrCount))
    val errorMessage = "error message"
    val errorResponse = new ErrorResponse(errorMessage)

    val result2 = new SpinResultEntry(errorResponse.toXml.toString(), new Result(null, "description1", null, null))
    val result1 = new SpinResultEntry(rqr1.toXml.toString(), new Result(null, "description2", null, null))

    val aggregator = new RunQueryAggregator(queryId, userId, groupId, requestXml, queryInstanceId, true)
    val actual = aggregator.aggregate(Vector(result1, result2)).asInstanceOf[RunQueryResponse]
    assertTrue(actual.isInstanceOf[RunQueryResponse])
    actual.results.size should equal(3)
    actual.results.filter(x=>x.resultType.equalsIgnoreCase("PATIENT_COUNT_XML")).head.setSize should equal(10)
    actual.results.filter(x=>x.statusType.equalsIgnoreCase("ERROR")).head.statusMessage should equal(Some(errorMessage))
    actual.results.filter(x=>x.description.getOrElse("").equalsIgnoreCase("TOTAL COUNT")).head.setSize should equal(10)
  }

}
