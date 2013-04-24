package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.util.XmlUtil
import scala.xml.XML
import scala.xml.NodeSeq

/**
 * @author clint
 * @date Apr 3, 2013
 */
final class ReadI2b2AdminPreviousQueriesRequestTest extends TestCase with ShouldMatchersForJUnit {
  val projectId = "some-projectid"
  val waitTimeMs = 999999L
  val authn = AuthenticationInfo("d", "u", Credential("p", false))
  val searchString = "aksdhjksadhjksadhksadh"
  val maxResults = 654321

  import ReadI2b2AdminPreviousQueriesRequest._

  @Test
  def testToXml = doTestToXml(makeShrineXml, _.toXml)

  @Test
  def testToI2b2 = doTestToXml(makeI2b2Xml, _.toI2b2)
  
  @Test
  def testFromXml = doTestFromXml(makeShrineXml, ReadI2b2AdminPreviousQueriesRequest.fromXml)
  
  @Test
  def testFromI2b2 = doTestFromXml(makeI2b2Xml, ReadI2b2AdminPreviousQueriesRequest.fromI2b2)

  @Test
  def testToXmlRoundTrip {
    doTestRoundTrip(_.toXml, ReadI2b2AdminPreviousQueriesRequest.fromXml)
  }

  @Test
  def testShrineRequestXmlRoundTrip {
    doTestRoundTrip(_.toXml, ShrineRequest.fromXml)
  }

  @Test
  def testReadI2b2AdminPreviousQueriesRequestToXmlRoundTrip {
    doTestRoundTrip(_.toXml, ReadI2b2AdminPreviousQueriesRequest.fromXml)
  }
  
  @Test
  def testDoubleDispatchingShrineRequestToI2b2RoundTrip {
    doTestRoundTrip(_.toI2b2, HandleableAdminShrineRequest.fromI2b2)
  }
  
  @Test
  def testReadI2b2AdminPreviousQueriesRequestToI2b2RoundTrip {
    doTestRoundTrip(_.toI2b2, ReadI2b2AdminPreviousQueriesRequest.fromI2b2)
  }

  private def request(tuple: (SortOrder, Category, Strategy)) = {
    val (sortOrder, category, strategy) = tuple

    ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, searchString, maxResults, sortOrder, strategy, category)
  }
  
  private def doTestToXml(makeExpectedXml: (SortOrder, Category, Strategy) => NodeSeq, serialize: ReadI2b2AdminPreviousQueriesRequest => NodeSeq) {
    for {
      t @ (sortOrder, category, strategy) <- flagCombinations
    } {
      val req = request(t)
      
      val actualXml = serialize(req).toString
      
      val expectedXml = makeExpectedXml(sortOrder, category, strategy).toString
      
      actualXml should equal(expectedXml)
    }
  }
  
  private def doTestFromXml(makeExpectedXml: (SortOrder, Category, Strategy) => NodeSeq, deserialize: NodeSeq => ReadI2b2AdminPreviousQueriesRequest) {
    for {
      t @ (sortOrder, category, strategy) <- flagCombinations
    } {
      val expectedReq = request(t)

      val actualReq = deserialize(makeExpectedXml(sortOrder, category, strategy))
      
      actualReq should equal(expectedReq)
    }
  }
  
  private def doTestRoundTrip[R](serialize: ReadI2b2AdminPreviousQueriesRequest => NodeSeq, deserialize: NodeSeq => R) {
    for {
      req <- flagCombinationReqs
    } {
      val xml = serialize(req)

      val unmarshalled = deserialize(xml)

      unmarshalled should equal(req)
    }
  }

  private def flagCombinationReqs = flagCombinations.map(request)

  private def flagCombinations: Iterable[(SortOrder, Category, Strategy)] = {
    for {
      sortOrder <- SortOrder.values
      category <- Category.values
      strategy <- Strategy.values
    } yield (sortOrder, category, strategy)
  }

  private def makeShrineXml(sortOrder: SortOrder, category: Category, strategy: Strategy): NodeSeq = XmlUtil.stripWhitespace {
    <readAdminPreviousQueries>
      <projectId>{ projectId }</projectId><waitTimeMs>{ waitTimeMs }</waitTimeMs>
      { authn.toXml }
      <searchString>{ searchString }</searchString>
      <maxResults>{ maxResults }</maxResults>
      <sortOrder>{ sortOrder }</sortOrder>
      <categoryToSearchWithin>{ category }</categoryToSearchWithin>
      <searchStrategy>{ strategy }</searchStrategy>
    </readAdminPreviousQueries>
  }

  private def makeI2b2Xml(sortOrder: SortOrder, category: Category, strategy: Strategy): NodeSeq = XmlUtil.stripWhitespace {
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
        { authn.toI2b2 }
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
        <result_waittime_ms>{ waitTimeMs }</result_waittime_ms>
      </request_header>
      <message_body>
        <ns4:psmheader>
          <user login={ authn.username }>{ authn.username }</user>
          <patient_set_limit>0</patient_set_limit>
          <estimated_time>0</estimated_time>
        </ns4:psmheader>
        <ns4:get_name_info category={ category.toString } max={ maxResults.toString }>
          <match_str strategy={ strategy.toString }>{ searchString }</match_str>
          <ascending>{ sortOrder.isAscending }</ascending>
        </ns4:get_name_info>
      </message_body>
    </ns6:request>
  }
}