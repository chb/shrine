package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import scala.xml.NodeSeq
import org.spin.tools.NetworkTime
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.util.XmlUtil

/**
 * @author clint
 * @date Nov 5, 2012
 */
final class ShrineResponseTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testFromXml {
    ShrineResponse.fromXml(null) should be(None)
    ShrineResponse.fromXml(NodeSeq.Empty) should be(None)
    
    def doTestFromXml(response: ShrineResponse) {
      val unmarshalled = ShrineResponse.fromXml(response.toXml)
      
      unmarshalled.get.getClass should equal(response.getClass)
      unmarshalled should not be(null)
      unmarshalled should equal(Some(response))
    }
    
    def now = (new NetworkTime).getXMLGregorianCalendar
    
    val queryResult1 = QueryResult(1L, 2342L, Some(ResultOutputType.PATIENT_COUNT_XML), 123L, None, None, None, QueryResult.StatusType.Finished, None, Map.empty)
    
    doTestFromXml(new ReadQueryResultResponse(123L, queryResult1))
    doTestFromXml(new AggregatedReadQueryResultResponse(123L, Seq(queryResult1)))
    doTestFromXml(new DeleteQueryResponse(123L))
    doTestFromXml(new ReadInstanceResultsResponse(2342L, queryResult1))
    doTestFromXml(new AggregatedReadInstanceResultsResponse(2342L, Seq(queryResult1)))
    doTestFromXml(new ReadPreviousQueriesResponse("userId", "groupId", Seq.empty))
    doTestFromXml(new ReadQueryDefinitionResponse(8457L, "name", "userId", now, "queryDefXml"))
    doTestFromXml(new ReadQueryInstancesResponse(12345L, "userId", "groupId", Seq.empty))
    doTestFromXml(new RenameQueryResponse(12345L, "name"))
    doTestFromXml(new RunQueryResponse(38957L, now, "userId", "groupId", QueryDefinition("foo", Term("bar")), 2342L, queryResult1))
    doTestFromXml(new AggregatedRunQueryResponse(38957L, now, "userId", "groupId", QueryDefinition("foo", Term("bar")), 2342L, Seq(queryResult1)))
    
    doTestFromXml(new ErrorResponse("errorMessage"))
  }
  
  @Test
  def testToXml {
    val response = new FooResponse
    
    response.toXmlString should equal("<foo></foo>")
  }
  
  @Test
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
    
    response.toI2b2String should equal(expected.toString)
  }
  
  private final class FooResponse extends ShrineResponse {
    protected override def i2b2MessageBody = <foo></foo>
      
    override def toXml = i2b2MessageBody
  }
}