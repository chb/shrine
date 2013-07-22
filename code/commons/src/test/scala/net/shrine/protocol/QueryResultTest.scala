package net.shrine.protocol

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import org.spin.tools.NetworkTime
import java.util.Calendar
import xml.Utility
import net.shrine.util.XmlUtil

/**
 * @author Bill Simons
 * @date 8/19/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class QueryResultTest extends AssertionsForJUnit with ShouldMatchersForJUnit with I2b2SerializableValidator{
  def testFromI2b2() = null

  @Test
  def testToI2b2() = {
    val date = NetworkTime.makeXMLGregorianCalendar(Calendar.getInstance.getTime)
    val resultId = 1L
    val instanceId = 2L
    val resultType = "PATIENTSET"
    val setSize = 12L
    val statusType = "FINISHED"
    val description = "description"
    val queryResult = new QueryResult(resultId, instanceId, resultType, setSize, date, date, description, statusType)
    queryResult.toI2b2 should equal (XmlUtil.stripWhitespace(
      <query_result_instance>
        <result_instance_id>{resultId}</result_instance_id>
        <query_instance_id>{instanceId}</query_instance_id>
        <description>{description}</description>
        <query_result_type>
          <name>{resultType}</name>
          <result_type_id>1</result_type_id><display_type>LIST</display_type><visual_attribute_type>LA</visual_attribute_type><description>Patient list</description>
          </query_result_type>
        <set_size>{setSize}</set_size>
        <start_date>{date}</start_date>
        <end_date>{date}</end_date>
        <query_status_type>
          <name>{statusType}</name>
          <status_type_id>3</status_type_id><description>FINISHED</description>
        </query_status_type>
      </query_result_instance>))
    println(queryResult.toI2b2.toString)
  }

  @Test
  def testToI2b2WithErrors() {
    val statusMessage = "status message"
    val description = "description"
    val actual = QueryResult.errorResult(description, statusMessage).toI2b2
    val expected = XmlUtil.stripWhitespace(
      <query_result_instance>
        <result_instance_id>0</result_instance_id>
        <query_instance_id>0</query_instance_id>
        <description>{description}</description>
        <query_result_type>
          <name></name>
        </query_result_type>
        <set_size>0</set_size>
        <query_status_type>
          <name>ERROR</name>
          <description>{statusMessage}</description>
        </query_status_type>
      </query_result_instance>)
    actual.toString() should equal (expected.toString()) //compare strings because scala xml comparison isn't working right here

  }
}