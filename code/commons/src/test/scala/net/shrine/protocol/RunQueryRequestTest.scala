package net.shrine.protocol

import org.junit.Test
import org.junit.Assert.assertTrue
import xml.Utility
import scala.xml.XML
import net.shrine.util.XmlUtil

/**
 * @author Bill Simons
 * @date 3/17/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class RunQueryRequestTest extends ShrineRequestValidator {
  val topicId = "1"
  val queryDefinition = XmlUtil.stripWhitespace(
    <query_definition>
      <query_name>Ostium secundum@14:01:35</query_name>
      <specificity_scale>0</specificity_scale>
      <use_shrine>1</use_shrine>
      <panel>
        <panel_number>1</panel_number>
        <invert>0</invert>
        <total_item_occurrences>1</total_item_occurrences>
        <item>
          <hlevel>5</hlevel>
          <item_name>Ostium secundum type atrial septal defect</item_name>
          <item_key>\\SHRINE\SHRINE\Diagnoses\Congenital anomalies\Cardiac and circulatory congenital anomalies\Atrial septal defect\Ostium secundum type atrial septal defect\</item_key>
          <tooltip>Diagnoses\Congenital anomalies\Cardiac and circulatory congenital anomalies\Atrial septal defect\Ostium secundum type atrial septal defect</tooltip>
          <class>ENC</class>
          <constrain_by_date>
          </constrain_by_date>
          <item_icon>LA</item_icon>
          <item_is_synonym>false</item_is_synonym>
        </item>
      </panel>
    </query_definition>)

  //add weird casing to make sure the code isn't case senstive, the client will send all sorts of weirdness
  val resultOutputTypes = XmlUtil.stripWhitespace(
    <result_output_list>
      <result_output priority_index="1" name="PatiEntSet"/>
      <result_output priority_index="2" name="patIent_Count_Xml"/>
    </result_output_list>)

  def messageBody = XmlUtil.stripWhitespace(
    <message_body>
      <ns4:psmheader>
        <user group={domain} login={username}>{username}</user>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>0</estimated_time>
        <request_type>CRC_QRY_runQueryInstance_fromQueryDefinition</request_type>
      </ns4:psmheader>
      <ns4:request xsi:type="ns4:query_definition_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        {queryDefinition}
        {resultOutputTypes}
      </ns4:request>
      <shrine><queryTopicID>{topicId}</queryTopicID></shrine>
    </message_body>)

  val runQueryRequest = XmlUtil.stripWhitespace(
      <runQuery>
        {requestHeaderFragment}
        <topicId>{topicId}</topicId>
        <outputTypes>
          <outputType>PATIENTSET</outputType>
          <outputType>PATIENT_COUNT_XML</outputType>
        </outputTypes>
        <queryDefinition>{queryDefinition.toString}</queryDefinition>
      </runQuery>)

  @Test
  def testFromI2b2() {
    val translatedRequest = RunQueryRequest.fromI2b2(request)
    validateRequestWith(translatedRequest) {
      translatedRequest.topicId should equal(topicId)
      translatedRequest.outputTypes should contain(ResultOutputType.PATIENT_COUNT_XML)
      translatedRequest.outputTypes should contain(ResultOutputType.PATIENTSET)
      translatedRequest.queryDefinitionXml should equal(queryDefinition.toString)
      
      val queryDefNode = XML.loadString(translatedRequest.queryDefinitionXml).head
      
      queryDefNode.prefix should equal(queryDefNode.scope.getPrefix(RunQueryRequest.neededI2b2Namespace))
    }
  }

  @Test
  def testShrineRequestFromI2b2() {
    val shrineRequest = ShrineRequest.fromI2b2(request)
    assertTrue(shrineRequest.isInstanceOf[RunQueryRequest])
  }

  @Test
  def testToXml() {
    new RunQueryRequest(
      projectId,
      waitTimeMs,
      authn,
      topicId,
      Set(ResultOutputType.PATIENTSET, ResultOutputType.PATIENT_COUNT_XML),
      queryDefinition.toString).toXml should equal(runQueryRequest)
  }

  @Test
  def testToI2b2() {
    println(new RunQueryRequest(
      projectId,
      waitTimeMs,
      authn,
      topicId,
      Set(ResultOutputType.PATIENTSET, ResultOutputType.PATIENT_COUNT_XML),
      queryDefinition.toString).toI2b2.toString)
  }

  @Test
  def testFromXml() {
    val actual = RunQueryRequest.fromXml(runQueryRequest)
    validateRequestWith(actual) {
      actual.topicId should equal(topicId)
      actual.outputTypes should contain(ResultOutputType.PATIENT_COUNT_XML)
      actual.outputTypes should contain(ResultOutputType.PATIENTSET)
      actual.queryDefinitionXml should equal(queryDefinition.toString)
      
      val queryDefNode = XML.loadString(actual.queryDefinitionXml).head
      
      queryDefNode.prefix should be(null)
      queryDefNode.namespace should be(null)
    }
  }

  @Test
  def testShrineRequestFromXml() {
    assertTrue(ShrineRequest.fromXml(runQueryRequest).isInstanceOf[RunQueryRequest])
  }
}