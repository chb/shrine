package net.shrine.protocol

import java.util.Calendar
import org.junit.Test
import org.junit.Assert.assertTrue
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType
import java.util.Date
import org.spin.tools.NetworkTime
import javax.xml.datatype.XMLGregorianCalendar
import xml.Utility
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
class ReadPreviousQueriesResponseTest extends ShrineResponseI2b2SerializableValidator {

  val queryMasterId1 = 1111111L
  val queryName1 = "name1"
  val userId = "user1"
  val groupId = "group1"
  val createDate1 = NetworkTime.makeXMLGregorianCalendar(Calendar.getInstance.getTime)
  val queryMaster1 = makeQueryMaster(queryMasterId1, queryName1, userId, groupId, createDate1)


  val queryMasterId2 = 222222L
  val queryName2 = "name2"
  val createDate2 = NetworkTime.makeXMLGregorianCalendar(Calendar.getInstance.getTime)
  val queryMaster2 = makeQueryMaster(queryMasterId2, queryName2, userId, groupId, createDate2)


  def makeQueryMaster(queryMasterId: Long, queryName: String, userId: String, groupId: String, createDate: XMLGregorianCalendar) = {
    val queryMaster = new QueryMasterType()
    queryMaster.setQueryMasterId(String.valueOf(queryMasterId))
    queryMaster.setName(queryName)
    queryMaster.setUserId(userId)
    queryMaster.setGroupId(groupId)
    queryMaster.setCreateDate(createDate)

    queryMaster
  }

  def messageBody = <message_body>
      <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:master_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      <query_master>
        <query_master_id>{queryMasterId1}</query_master_id>
        <name>{queryName1}</name>
        <user_id>{userId}</user_id>
        <group_id>{groupId}</group_id>
        <create_date>{createDate1}</create_date>
      </query_master>
      <query_master>
        <query_master_id>{queryMasterId2}</query_master_id>
        <name>{queryName2}</name>
        <user_id>{userId}</user_id>
        <group_id>{groupId}</group_id>
        <create_date>{createDate2}</create_date>
      </query_master>
    </ns5:response>
  </message_body>

  val readPreviousQueriesResponse = XmlUtil.stripWhitespace(
    <readPreviousQueriesResponse>
      <userId>{userId}</userId>
      <groupId>{groupId}</groupId>
      <queryMaster>
        <id>{queryMasterId1}</id>
        <name>{queryName1}</name>
        <createDate>{createDate1}</createDate>
      </queryMaster>
      <queryMaster>
        <id>{queryMasterId2}</id>
        <name>{queryName2}</name>
        <createDate>{createDate2}</createDate>
      </queryMaster>
    </readPreviousQueriesResponse>)

  @Test
  def testFromXml() {
    val actual = ReadPreviousQueriesResponse.fromXml(readPreviousQueriesResponse)
    assertTrue(actual.queryMasters.contains(queryMaster1))
    assertTrue(actual.queryMasters.contains(queryMaster2))
  }

  @Test
  def testToXml() {
    //we compare the string versions of the xml because Scala's xml equality does not always behave properly
    new ReadPreviousQueriesResponse(userId, groupId, Seq(queryMaster1, queryMaster2)).toXml.toString should equal(readPreviousQueriesResponse.toString)
  }

  @Test
  def testFromI2b2() {
    val translatedResponse = ReadPreviousQueriesResponse.fromI2b2(response)
    assertTrue(translatedResponse.queryMasters.contains(queryMaster1))
    assertTrue(translatedResponse.queryMasters.contains(queryMaster2))
  }

  @Test
  def testToI2b2() {
    //we compare the string versions of the xml because Scala's xml equality does not always behave properly
    new ReadPreviousQueriesResponse(userId, groupId, Seq(queryMaster1, queryMaster2)).toI2b2.toString should equal(response.toString)
  }
}