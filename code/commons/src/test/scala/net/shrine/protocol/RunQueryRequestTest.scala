package net.shrine.protocol

import org.junit.Test
import org.junit.Assert.assertTrue
import xml.Utility
import scala.xml.XML
import net.shrine.util.XmlUtil
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.OccuranceLimited
import net.shrine.protocol.query.Term

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
  val queryDefinition = QueryDefinition("Ostium secundum@14:01:35", Term("""\\SHRINE\SHRINE\Diagnoses\Congenital anomalies\Cardiac and circulatory congenital anomalies\Atrial septal defect\Ostium secundum type atrial septal defect\"""))

  //add weird casing to make sure the code isn't case senstive, the client will send all sorts of weirdness
  val resultOutputTypes = XmlUtil.stripWhitespace(
    <result_output_list>
      <result_output priority_index="1" name="PatiEntSet"/>
      <result_output priority_index="2" name="patIent_Count_Xml"/>
    </result_output_list>)

  override def messageBody = XmlUtil.stripWhitespace(
    <message_body>
      <ns4:psmheader>
        <user group={domain} login={username}>{username}</user>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>0</estimated_time>
        <request_type>CRC_QRY_runQueryInstance_fromQueryDefinition</request_type>
      </ns4:psmheader>
      <ns4:request xsi:type="ns4:query_definition_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        {queryDefinition.toI2b2}
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
        {queryDefinition.toXml}
      </runQuery>)

  @Test
  override def testFromI2b2 {
    val translatedRequest = RunQueryRequest.fromI2b2(request)
    validateRequestWith(translatedRequest) {
      translatedRequest.topicId should equal(topicId)
      translatedRequest.outputTypes should contain(ResultOutputType.PATIENT_COUNT_XML)
      translatedRequest.outputTypes should contain(ResultOutputType.PATIENTSET)
      translatedRequest.queryDefinition should equal(queryDefinition)
      
      val queryDefNode = translatedRequest.queryDefinition.toI2b2
      
      queryDefNode.head.prefix should equal(queryDefNode.head.scope.getPrefix(RunQueryRequest.neededI2b2Namespace))
    }
  }

  @Test
  def testMapQueryDefinition {
    val projectId = "projId"
    val waitTimeMs = 12345L
    val authn = AuthenticationInfo("some-domain", "some-user", Credential("salkjdlasd", false))
    val topicId = "topicId"
    val outputTypes = ResultOutputType.nonBreakdownTypes.toSet
      
    val req = new RunQueryRequest(projectId, waitTimeMs, authn, topicId, outputTypes, queryDefinition)
    
    val bogusTerm = Term("sa;ldk;alskd")
    
    val mapped = req.mapQueryDefinition(_.transform(_ => bogusTerm))
    
    (mapped eq req) should not be(true)
    
    mapped should not equal(req)
    
    mapped.projectId should equal(projectId)
    mapped.waitTimeMs should equal(waitTimeMs)
    mapped.authn should equal(authn)
    mapped.topicId should equal(topicId)
    mapped.outputTypes should equal(outputTypes)
    mapped.queryDefinition.name should equal(queryDefinition.name)
    mapped.queryDefinition.expr should equal(bogusTerm)
  }
  
  @Test
  override def testShrineRequestFromI2b2 {
    val shrineRequest = ShrineRequest.fromI2b2(request)
    assertTrue(shrineRequest.isInstanceOf[RunQueryRequest])
  }

  @Test
  override def testToXml {
    new RunQueryRequest(
      projectId,
      waitTimeMs,
      authn,
      topicId,
      Set(ResultOutputType.PATIENTSET, ResultOutputType.PATIENT_COUNT_XML),
      queryDefinition).toXml should equal(runQueryRequest)
  }

  @Test
  override def testToI2b2 {
    println(new RunQueryRequest(
      projectId,
      waitTimeMs,
      authn,
      topicId,
      Set(ResultOutputType.PATIENTSET, ResultOutputType.PATIENT_COUNT_XML),
      queryDefinition).toI2b2.toString)
  }

  @Test
  override def testFromXml {
    val actual = RunQueryRequest.fromXml(runQueryRequest)
    validateRequestWith(actual) {
      actual.topicId should equal(topicId)
      actual.outputTypes should contain(ResultOutputType.PATIENT_COUNT_XML)
      actual.outputTypes should contain(ResultOutputType.PATIENTSET)
      actual.queryDefinition should equal(queryDefinition)
      
      val queryDefNode = actual.queryDefinition.toXml.head
      
      queryDefNode.prefix should be(null)
      queryDefNode.namespace should be(null)
    }
  }

  @Test
  def testShrineRequestFromXml {
    assertTrue(ShrineRequest.fromXml(runQueryRequest).isInstanceOf[RunQueryRequest])
  }
}