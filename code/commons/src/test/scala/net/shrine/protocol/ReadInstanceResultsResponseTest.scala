package net.shrine.protocol

import java.util.Calendar
import org.spin.tools.NetworkTime._
import javax.xml.datatype.XMLGregorianCalendar
import org.junit.Assert.assertTrue
import org.junit.Test
import net.shrine.util.XmlUtil
import junit.framework.TestCase
import junit.framework.Assert

/**
 * @author Bill Simons
 * @date 4/14/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ReadInstanceResultsResponseTest extends TestCase with ShrineResponseI2b2SerializableValidator {
  val shrineNetworkQueryId = 1111111L
  val resultId1 = 1111111L
  val setSize = 12
  val type1 = ResultOutputType.PATIENTSET
  val statusName1 = "FINISHED"
  val startDate1 = makeXMLGregorianCalendar(Calendar.getInstance.getTime)
  val endDate1 = makeXMLGregorianCalendar(Calendar.getInstance.getTime)
  val result1 = new QueryResult(resultId1, shrineNetworkQueryId, type1, setSize, startDate1, endDate1, statusName1)

  val resultId2 = 222222L
  val type2 = ResultOutputType.PATIENT_COUNT_XML
  val statusName2 = "FINISHED"
  val startDate2 = makeXMLGregorianCalendar(Calendar.getInstance.getTime)
  val endDate2 = makeXMLGregorianCalendar(Calendar.getInstance.getTime)
  val result2 = new QueryResult(resultId2, shrineNetworkQueryId, type2, setSize, startDate2, endDate2, statusName2)

  override def messageBody = <message_body>
        <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:result_responseType">
            <status>
                <condition type="DONE">DONE</condition>
            </status>
            <query_result_instance>
                <result_instance_id>{resultId1}</result_instance_id>
                <query_instance_id>{shrineNetworkQueryId}</query_instance_id>
                <query_result_type>
                    <name>{type1}</name>
                    <result_type_id>1</result_type_id>
                    <display_type>LIST</display_type>
                    <visual_attribute_type>LA</visual_attribute_type>
                    <description>Patient list</description>
                </query_result_type>
                <set_size>{setSize}</set_size>
                <start_date>{startDate1}</start_date>
                <end_date>{endDate1}</end_date>
                <query_status_type>
                    <name>{statusName1}</name>
                    <status_type_id>3</status_type_id>
                    <description>FINISHED</description>
                </query_status_type>
            </query_result_instance>
            <query_result_instance>
                <result_instance_id>{resultId2}</result_instance_id>
                <query_instance_id>{shrineNetworkQueryId}</query_instance_id>
                <query_result_type>
                    <name>{type2}</name>
                    <result_type_id>4</result_type_id>
                    <display_type>CATNUM</display_type>
                    <visual_attribute_type>LA</visual_attribute_type>
                    <description>Number of patients</description>
                </query_result_type>
                <set_size>{setSize}</set_size>
                <start_date>{startDate2}</start_date>
                <end_date>{endDate2}</end_date>
                <query_status_type>
                    <name>{statusName2}</name>
                    <status_type_id>3</status_type_id>
                    <description>FINISHED</description>
                </query_status_type>
            </query_result_instance>
        </ns5:response>
    </message_body>

  private val readInstanceResultsResponse = XmlUtil.stripWhitespace(
    <readInstanceResultsResponse>
      <shrineNetworkQueryId>{shrineNetworkQueryId}</shrineNetworkQueryId>
      <queryResults>
        <queryResult>
          <resultId>{resultId1}</resultId>
          <instanceId>{shrineNetworkQueryId}</instanceId>
          <resultType>{type1}</resultType>
          <setSize>{setSize}</setSize>
          <startDate>{startDate1}</startDate>
          <endDate>{endDate1}</endDate>
          <status>{statusName1}</status>
        </queryResult>
        <queryResult>
          <resultId>{resultId2}</resultId>
          <instanceId>{shrineNetworkQueryId}</instanceId>
          <resultType>{type2}</resultType>
          <setSize>{setSize}</setSize>
          <startDate>{startDate2}</startDate>
          <endDate>{endDate2}</endDate>
          <status>{statusName2}</status>
        </queryResult>
      </queryResults>
    </readInstanceResultsResponse>)

  @Test
  def testFromXml {
    val actual = ReadInstanceResultsResponse.fromXml(readInstanceResultsResponse)
    
    actual.shrineNetworkQueryId should equal(shrineNetworkQueryId)
    
    assertTrue(actual.results.contains(result1))
    assertTrue(actual.results.contains(result2))
  }

  @Test
  def testToXml {
    //we compare the string versions of the xml because Scala's xml equality does not always behave properly
    new ReadInstanceResultsResponse(shrineNetworkQueryId, Seq(result1, result2)).toXml.toString should equal(readInstanceResultsResponse.toString)
  }

  @Test
  def testFromI2b2 {
    val actual = ReadInstanceResultsResponse.fromI2b2(response)
    actual.shrineNetworkQueryId should equal(shrineNetworkQueryId)
    assertTrue(actual.results.contains(result1))
    assertTrue(actual.results.contains(result2))
  }

  @Test
  def testToI2b2 {
    //we compare the string versions of the xml because Scala's xml equality does not always behave properly
    val actual = new ReadInstanceResultsResponse(shrineNetworkQueryId, Seq(result1, result2)).toI2b2String
    
    actual should equal(response.toString)
  }
}