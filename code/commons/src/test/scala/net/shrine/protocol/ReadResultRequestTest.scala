package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import net.shrine.util.XmlUtil
import scala.xml.NodeSeq

/**
 * @author clint
 * @date Aug 16, 2012
 */
final class ReadResultRequestTest extends TestCase with AssertionsForJUnit with ShouldMatchers {

  private val projectId = "2qfhsjksdfhkshdf"
  private val waitTime = 12345L
  private val authn = AuthenticationInfo("some-domain", "some-user", Credential("ksdlghjksdghksdghk", true))
  private val resultId = 8734568L

  private val exampleReq = ReadResultRequest(projectId, waitTime, authn, resultId)

  private val expectedI2b2Message = XmlUtil.stripWhitespace(
    <ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns8="http://sheriff.shrine.net/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">
      <message_header>
        <proxy>
          <redirect_url>https://localhost/shrine/QueryToolService/request</redirect_url>
        </proxy>
        <sending_application>
          <application_name>i2b2_QueryTool</application_name>
          <application_version>0.2</application_version>
        </sending_application>
        <sending_facility>
          <facility_name>SHRINE</facility_name>
        </sending_facility>
        <receiving_application>
          <application_name>i2b2_DataRepositoryCell</application_name>
          <application_version>0.2</application_version>
        </receiving_application>
        <receiving_facility>
          <facility_name>SHRINE</facility_name>
        </receiving_facility>
        { exampleReq.authn.toI2b2 }
        <message_type>
          <message_code>Q04</message_code>
          <event_type>EQQ</event_type>
        </message_type>
        <message_control_id>
          <message_num>EQ7Szep1Md11K4E7zEc99</message_num>
          <instance_num>0</instance_num>
        </message_control_id>
        <processing_id>
          <processing_id>P</processing_id>
          <processing_mode>I</processing_mode>
        </processing_id>
        <accept_acknowledgement_type>AL</accept_acknowledgement_type>
        <project_id>{ projectId }</project_id>
        <country_code>US</country_code>
      </message_header>
      <request_header>
        <result_waittime_ms>{ exampleReq.waitTimeMs }</result_waittime_ms>
      </request_header>
      <message_body>
        <ns4:psmheader>
          <user login={ exampleReq.authn.username }>{ exampleReq.authn.username }</user>
          <patient_set_limit>0</patient_set_limit>
          <estimated_time>0</estimated_time>
          <request_type>{ CRCRequestType.ResultRequestType.unsafeI2b2RequestType }</request_type>
        </ns4:psmheader>
        <ns4:request xsi:type="ns4:result_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <query_result_instance_id>{ resultId }</query_result_instance_id>
        </ns4:request>
      </message_body>
    </ns6:request>)

  val expectedShrineMessage = XmlUtil.stripWhitespace(
    <readResult>
      <projectId>{ exampleReq.projectId }</projectId>
      <waitTimeMs>{ exampleReq.waitTimeMs }</waitTimeMs>
      { exampleReq.authn.toXml }
      <resultId>{ exampleReq.resultId }</resultId>
    </readResult>)

  @Test
  def testRequestType {
    ReadResultRequest("", 123L, null, 456L).requestType should equal(CRCRequestType.ResultRequestType)
  }

  @Test
  def testAuxConstructor {
    val req = new ReadResultRequest(RequestHeader(projectId, waitTime, authn), resultId)

    req should equal(ReadResultRequest(projectId, waitTime, authn, resultId))

    req.projectId should equal(projectId)
    req.waitTimeMs should equal(waitTime)
    req.authn should equal(authn)
    req.resultId should equal(resultId)
  }

  @Test
  def testFromXml {
    doRoundTrip(_.toXml, ReadResultRequest.fromXml)(exampleReq)

    ReadResultRequest.fromXml(expectedShrineMessage) should equal(exampleReq)
  }

  @Test
  def testFromI2b2 {
    doRoundTrip(_.toI2b2, ReadResultRequest.fromI2b2)(exampleReq)

    ReadResultRequest.fromI2b2(expectedI2b2Message) should equal(exampleReq)
  }

  private def doRoundTrip(serialize: ReadResultRequest => NodeSeq, deserialize: NodeSeq => ReadResultRequest)(req: ReadResultRequest) {
    val roundTripped = deserialize(serialize(req))

    roundTripped should equal(req)
  }

  @Test
  def testToXml {
    exampleReq.toXml.toString should equal(expectedShrineMessage.toString)
  }

  @Test
  def testI2b2MessageBody {
    exampleReq.toI2b2String should equal(expectedI2b2Message.toString)
  }
}