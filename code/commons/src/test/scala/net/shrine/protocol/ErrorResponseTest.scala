package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import scala.xml.NodeSeq
import net.shrine.util.XmlUtil

/**
 * @author clint
 * @date Apr 5, 2013
 */
final class ErrorResponseTest extends TestCase with ShouldMatchersForJUnit {
  val message = "foo"

  val resp = ErrorResponse(message)

  val expectedShrineXml = XmlUtil.stripWhitespace {
    <errorResponse>
      <message>{ message }</message>
    </errorResponse>
  }

  val expectedI2b2Xml = XmlUtil.stripWhitespace {
    <ns4:response xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/cell/pm/1.1/" xmlns:ns7="http://sheriff.shrine.net/" xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns9="http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/" xmlns:ns10="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns11="http://www.i2b2.org/xsd/hive/msg/result/1.1/">
      <message_header>
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
          <application_name>SHRINE</application_name>
          <application_version>1.3-compatible</application_version>
        </sending_application>
        <sending_facility>
          <facility_name>SHRINE</facility_name>
        </sending_facility>
        <datetime_of_message>2011-04-08T16:21:12.251-04:00</datetime_of_message>
        <security/>
        <project_id xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
      </message_header>
      <response_header>
        <result_status>
          <status type="ERROR">{ message }</status>
        </result_status>
      </response_header>
      <message_body>
      </message_body>
    </ns4:response>
  }

  @Test
  def testToXml = doTestToXml(expectedShrineXml, _.toXml)

  @Test
  def testToI2b2 = doTestToXml(expectedI2b2Xml, _.toI2b2)

  @Test
  def testToXmlRoundTrip = doTestRoundTrip(_.toXml, ErrorResponse.fromXml)

  @Test
  def testToI2b2RoundTrip = doTestRoundTrip(_.toI2b2, ErrorResponse.fromI2b2)

  @Test
  def testFromXml = doTestFromXml(expectedShrineXml, ErrorResponse.fromXml)

  @Test
  def testFromI2b2 = doTestFromXml(expectedI2b2Xml, ErrorResponse.fromI2b2)

  private def doTestFromXml(xml: NodeSeq, deserialize: NodeSeq => ErrorResponse) {
    intercept[Exception] {
      deserialize(null)
    }

    intercept[Exception] {
      deserialize(<foo></foo>)
    }
    
    intercept[Exception] {
      //Correct I2b2 XML structure, wrong status type
      deserialize(<ns4:request><response_header><result_status><status type="NUH">{message}</status></result_status></response_header></ns4:request>)
    }

    deserialize(xml) should equal(resp)
  }

  private def doTestToXml(expected: NodeSeq, serialize: ErrorResponse => NodeSeq) {
    val xml = serialize(resp)

    xml.toString should equal(expected.toString)
  }

  private def doTestRoundTrip(serialize: ErrorResponse => NodeSeq, deserialize: NodeSeq => ErrorResponse) {
    val unmarshalled = deserialize(serialize(resp))

    unmarshalled should equal(resp)
  }
}