package net.shrine.protocol
import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import org.spin.tools.NetworkTime
import scala.xml.NodeSeq
import net.shrine.util.XmlUtil

/**
 *
 * @author Clint Gilbert
 * @date Sep 20, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 */
final class ShrineResponseTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {
  def testToXml {
    val response = new FooResponse
    
    response.toXml.toString should equal("<foo></foo>")
  }
  
  def testToI2b2 {
    val expected = XmlUtil.stripWhitespace(<ns4:response xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/cell/pm/1.1/" xmlns:ns7="http://sheriff.shrine.net/" xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns9="http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/" xmlns:ns10="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns11="http://www.i2b2.org/xsd/hive/msg/result/1.1/">
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
          <status type="DONE">DONE</status>
        </result_status>
      </response_header>
      <message_body>
        <foo></foo>
      </message_body>
    </ns4:response>)
      
    val response = new FooResponse
    
    response.toI2b2.toString should equal(expected.toString)
  }
  
  def testFromXml {
    ShrineResponse.fromXml(null) should be(None)
    ShrineResponse.fromXml(NodeSeq.Empty) should be(None)
    
    def doTestFromXml(response: ShrineResponse) {
      val unmarshalled = ShrineResponse.fromXml(response.toXml)
      
      unmarshalled should not be(null)
      unmarshalled should equal(Some(response))
    }
    
    def now = (new NetworkTime).getXMLGregorianCalendar
    
    doTestFromXml(new DeleteQueryResponse(123L))
    doTestFromXml(new ReadInstanceResultsResponse(456L, Seq.empty))
    doTestFromXml(new ReadPreviousQueriesResponse("userId", "groupId", Seq.empty))
    doTestFromXml(new ReadQueryDefinitionResponse(8457L, "name", "userId", now, "queryDefXml"))
    doTestFromXml(new ReadQueryInstancesResponse(12345L, "userId", "groupId", Seq.empty))
    doTestFromXml(new RenameQueryResponse(12345L, "name"))
    doTestFromXml(new RunQueryResponse(38957L, now, "userId", "groupId", "requestXml", 2342L, Seq.empty))
    doTestFromXml(new ErrorResponse("errorMessage"))
  }
  
  private final class FooResponse extends ShrineResponse {
    protected override def i2b2MessageBody = <foo></foo>
      
    override def toXml = i2b2MessageBody
  }
}