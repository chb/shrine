package net.shrine.aggregation

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import org.junit.Assert.{assertTrue, assertFalse}
import org.scalatest.mock.EasyMockSugar
import org.easymock.EasyMock.{expect => invoke}
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType
import java.util.Calendar
import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime
import net.shrine.protocol.ReadPreviousQueriesResponse

/**
 * @author Bill Simons
 * @date 6/8/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadPreviousQueriesAggregatorTest extends AssertionsForJUnit with ShouldMatchersForJUnit with EasyMockSugar {
  private def newQueryMasterType(groupId: String, userId: String, masterId: String, name: String, createDate: XMLGregorianCalendar): QueryMasterType = {
    val master = new QueryMasterType()
    master.setGroupId(groupId)
    master.setUserId(userId)
    master.setCreateDate(createDate)
    master.setQueryMasterId(masterId)
    master.setName(name)
    master
  }

  @Test
  def testOldestToNewest() {
    val groupId = "groupId"
    val userId = "userId"
    val aggregator = new ReadPreviousQueriesAggregator(userId, groupId)
    val date = new NetworkTime(Calendar.getInstance.getTime)
    val older = newQueryMasterType(groupId, userId, "1", "name1", date.getXMLGregorianCalendar)
    val newer = newQueryMasterType(groupId, userId, "1", "name1", date.addDays(1).getXMLGregorianCalendar)

    assertFalse(aggregator.oldestToNewest(newer, older))
    assertFalse(aggregator.oldestToNewest(newer, newer))
    assertTrue(aggregator.oldestToNewest(older, newer))
  }

  @Test
  def testNewestToOldest() {
    val groupId = "groupId"
    val userId = "userId"
    val aggregator = new ReadPreviousQueriesAggregator(userId, groupId)
    val date = new NetworkTime(Calendar.getInstance.getTime)
    val older = newQueryMasterType(groupId, userId, "1", "name1", date.getXMLGregorianCalendar)
    val newer = newQueryMasterType(groupId, userId, "1", "name1", date.addDays(1).getXMLGregorianCalendar)

    assertTrue(aggregator.newestToOldest(newer, older))
    assertFalse(aggregator.newestToOldest(newer, newer))
    assertFalse(aggregator.newestToOldest(older, newer))
  }

  @Test
  def testAggregate() {
    val userId = "userId"
    val groupId = "groupId"
    val firstDate = new NetworkTime(Calendar.getInstance.getTime)
    val firstQm = newQueryMasterType(groupId, userId, "1", "name1", firstDate.getXMLGregorianCalendar)
    val lastQma = newQueryMasterType(groupId, userId, "2", "name2", firstDate.addDays(2).getXMLGregorianCalendar)
    val lastQmb = newQueryMasterType(groupId, userId, "2", "name2", firstDate.addDays(1).getXMLGregorianCalendar)
    val middleQm = newQueryMasterType(groupId, userId, "3", "name3", firstDate.addHours(1).getXMLGregorianCalendar)
    val masters1 = Seq(firstQm, lastQma)
    val masters2 = Seq(lastQmb, middleQm)
    val response1 = new ReadPreviousQueriesResponse(userId, groupId, masters1)
    val response2 = new ReadPreviousQueriesResponse(userId, groupId, masters2)
    val result1 = new SpinResultEntry(response1.toXml.toString(), null)
    val result2 = new SpinResultEntry(response2.toXml.toString(), null)
    val aggregator = new ReadPreviousQueriesAggregator(userId, groupId)

    val actual = aggregator.aggregate(Seq(result1, result2)).asInstanceOf[ReadPreviousQueriesResponse]
    assertTrue(actual.isInstanceOf[ReadPreviousQueriesResponse])

    actual.queryMasters.size should equal(3)
    actual.queryMasters(0) should equal(lastQma)
    actual.queryMasters(0).getCreateDate should equal(lastQmb.getCreateDate)
    actual.queryMasters(1) should equal(middleQm)
    actual.queryMasters(2) should equal(firstQm)
    actual.userId should equal(userId)
    actual.groupId should equal(groupId)
  }
}