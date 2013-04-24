package net.shrine.protocol

import java.util.Calendar
import org.junit.Test
import org.junit.Assert.assertTrue
import org.spin.tools.NetworkTime
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.util.XmlUtil

/**
 * @author Bill Simons
 * @date 4/12/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ReadPreviousQueriesResponseTest extends ShrineResponseI2b2SerializableValidator {

  val queryMasterId1 = 1111111L
  val queryName1 = "name1"
  val userId = Some("user1")
  val groupId = Some("group1")
  val createDate1 = NetworkTime.makeXMLGregorianCalendar(Calendar.getInstance.getTime)
  val queryMaster1 = makeQueryMaster(queryMasterId1, queryName1, userId, groupId, createDate1)

  val queryMasterId2 = 222222L
  val queryName2 = "name2"
  val createDate2 = NetworkTime.makeXMLGregorianCalendar(Calendar.getInstance.getTime)
  val queryMaster2 = makeQueryMaster(queryMasterId2, queryName2, userId, groupId, createDate2)

  def makeQueryMaster(queryMasterId: Long, queryName: String, userId: Option[String], groupId: Option[String], createDate: XMLGregorianCalendar) = {
    QueryMaster(String.valueOf(queryMasterId), queryName, userId.get, groupId.get, createDate)
  }

  def messageBody = XmlUtil.stripWhitespace {
    <message_body>
      <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:master_responseType">
        <status>
          <condition type="DONE">DONE</condition>
        </status>
        <query_master>
          <query_master_id>{ queryMasterId1 }</query_master_id>
          <name>{ queryName1 }</name>
          <user_id>{ userId.get }</user_id>
          <group_id>{ groupId.get }</group_id>
          <create_date>{ createDate1 }</create_date>
        </query_master>
        <query_master>
          <query_master_id>{ queryMasterId2 }</query_master_id>
          <name>{ queryName2 }</name>
          <user_id>{ userId.get }</user_id>
          <group_id>{ groupId.get }</group_id>
          <create_date>{ createDate2 }</create_date>
        </query_master>
      </ns5:response>
    </message_body>
  }

  val readPreviousQueriesResponse = XmlUtil.stripWhitespace {
    <readPreviousQueriesResponse>
      <userId>{ userId.get }</userId>
      <groupId>{ groupId.get }</groupId>
      <queryMaster>
        <id>{ queryMasterId1 }</id>
        <name>{ queryName1 }</name>
        <createDate>{ createDate1 }</createDate>
      </queryMaster>
      <queryMaster>
        <id>{ queryMasterId2 }</id>
        <name>{ queryName2 }</name>
        <createDate>{ createDate2 }</createDate>
      </queryMaster>
    </readPreviousQueriesResponse>
  }

  @Test
  def testFromI2b2FailsFast {
    intercept[Exception] {
      ReadPreviousQueriesResponse.fromI2b2(<foo/>)
    }
    
    intercept[Exception] {
      ReadPreviousQueriesResponse.fromI2b2(ErrorResponse("foo!").toI2b2)
    }
  }
  
  @Test
  def testFromXml {
    val actual = ReadPreviousQueriesResponse.fromXml(readPreviousQueriesResponse)

    actual.queryMasters.toSet should equal(Set(queryMaster1, queryMaster2))
  }

  @Test
  def testToXml {
    //we compare the string versions of the xml because Scala's xml equality does not always behave properly
    ReadPreviousQueriesResponse(userId, groupId, Seq(queryMaster1, queryMaster2)).toXmlString should equal(readPreviousQueriesResponse.toString)
  }

  @Test
  def testFromI2b2 {
    val translatedResponse = ReadPreviousQueriesResponse.fromI2b2(response)

    translatedResponse.queryMasters.toSet should equal(Set(queryMaster1, queryMaster2))
  }

  @Test
  def testToI2b2 {
    //we compare the string versions of the xml because Scala's xml equality does not always behave properly
    val actual = ReadPreviousQueriesResponse(userId, groupId, Seq(queryMaster1, queryMaster2)).toI2b2String 
    
    val expected = response.toString
    
    actual should equal(expected)
  }
}