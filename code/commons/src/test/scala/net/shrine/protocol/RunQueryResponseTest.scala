package net.shrine.protocol

import org.junit.Test
import org.spin.tools.NetworkTime
import java.util.Date
import xml.{XML, Utility}
import net.shrine.util.XmlUtil


/**
 *
 *
 * @author Justin Quan
 * @link http://chip.org
 * Date: 8/12/11
 */

class RunQueryResponseTest extends ShrineResponseI2b2SerializableValidator {
  val queryId = 1L
  val queryName = "queryName"
  val userId = "user"
  val groupId = "group"
  val createDate = NetworkTime.makeXMLGregorianCalendar(new Date())
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
  val queryInstanceId = 2L
  val resultId = 3L
  val description = Option("description")
  val setSize = 10L
  val startDate = createDate
  val endDate = createDate
  val resultId2 = 4L
  val resultType1 = "PATIENTSET"
  val resultType2 = "PATIENT_COUNT_XML"
  val statusType = "FINISHED"

  def messageBody = <message_body>
    <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:master_instance_result_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      <query_master>
        <query_master_id>{queryId}</query_master_id>
        <name>{queryName}</name>
        <user_id>{userId}</user_id>
        <group_id>{groupId}</group_id>
        <create_date>{createDate}</create_date>
        <request_xml>{requestXml}</request_xml>
      </query_master>
      <query_instance>
        <query_instance_id>{queryInstanceId}</query_instance_id>
        <query_master_id>{queryId}</query_master_id>
        <user_id>{userId}</user_id>
        <group_id>{groupId}</group_id>
        <query_status_type>
          <status_type_id>6</status_type_id>
          <name>COMPLETED</name>
          <description>COMPLETED</description>
        </query_status_type>
      </query_instance>
      <query_result_instance>
        <result_instance_id>{resultId}</result_instance_id>
        <query_instance_id>{queryInstanceId}</query_instance_id>
        <query_result_type>
          <name>{resultType1}</name>
          <result_type_id>1</result_type_id>
          <display_type>LIST</display_type>
          <visual_attribute_type>LA</visual_attribute_type>
          <description>Patient list</description>
        </query_result_type>
        <set_size>{setSize}</set_size>
        <start_date>{startDate}</start_date>
        <end_date>{endDate}</end_date>
        <query_status_type>
          <name>{statusType}</name>
          <status_type_id>3</status_type_id>
          <description>FINISHED</description>
        </query_status_type>
      </query_result_instance>
      <query_result_instance>
        <result_instance_id>{resultId2}</result_instance_id>
        <query_instance_id>{queryInstanceId}</query_instance_id>
       <query_result_type>
          <name>{resultType2}</name>
          <result_type_id>4</result_type_id>
          <display_type>CATNUM</display_type>
          <visual_attribute_type>LA</visual_attribute_type>
          <description>Number of patients</description>
        </query_result_type>
        <set_size>{setSize}</set_size>
        <start_date>{startDate}</start_date>
        <end_date>{endDate}</end_date>
        <query_status_type>
          <name>{statusType}</name>
          <status_type_id>3</status_type_id>
          <description>FINISHED</description>
        </query_status_type>
      </query_result_instance>
    </ns5:response>
  </message_body>

  val qr1 = new QueryResult(resultId, queryInstanceId, resultType1, setSize, createDate, createDate, statusType)
  val qr2 = new QueryResult(resultId2, queryInstanceId, resultType2, setSize, createDate, createDate, statusType)

  val runQueryResponse = XmlUtil.stripWhitespace(
    <runQueryResponse>
      <queryId>{queryId}</queryId>
      <instanceId>{queryInstanceId}</instanceId>
      <userId>{userId}</userId>
      <groupId>{groupId}</groupId>
      <requestXml>{requestXml}</requestXml>
      <createDate>{createDate}</createDate>
      <queryResults>
        {
        Seq(qr1, qr2) map {x =>
          x.toXml
        }
        }
      </queryResults>
    </runQueryResponse>)



  @Test
  def testFromXml() {
    val actual = RunQueryResponse.fromXml(runQueryResponse)

    actual.queryId should equal(queryId)
    actual.createDate should equal(createDate)
    actual.userId should equal(userId)
    actual.groupId should equal(groupId)
    actual.requestXml should equal(requestXml)
    actual.queryInstanceId should equal(queryInstanceId)
    actual.results should equal(Seq(qr1,qr2))
    actual.queryName should equal(queryName)

  }

  @Test
  def testToXml() {
    new RunQueryResponse(queryId, createDate, userId, groupId, requestXml, queryInstanceId, Seq(qr1,qr2)).toXml should equal(runQueryResponse)
  }

  @Test
  def testFromI2b2() {
    val translatedResponse = RunQueryResponse.fromI2b2(response)
    translatedResponse.queryId should equal(queryId)
    translatedResponse.createDate should equal(createDate)
    translatedResponse.userId should equal(userId)
    translatedResponse.groupId should equal(groupId)
    translatedResponse.requestXml should equal(requestXml)
    translatedResponse.queryInstanceId should equal(queryInstanceId)
    translatedResponse.results should equal(Seq(qr1,qr2))
    translatedResponse.queryName should equal(queryName)
  }

  @Test
  def testToI2b2() {
    new RunQueryResponse(queryId, createDate, userId, groupId, requestXml, queryInstanceId, Seq(qr1,qr2)).toI2b2.toString should equal(response.toString)
  }
}