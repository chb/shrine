package net.shrine.aggregation

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import org.junit.Assert.{assertNotNull, assertTrue}
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType
import org.spin.tools.NetworkTime
import net.shrine.protocol.{Credential, AuthenticationInfo, ReadQueryInstancesResponse}

/**
 * @author Bill Simons
 * @date 6/10/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadQueryInstancesAggregatorTest extends AssertionsForJUnit with ShouldMatchersForJUnit {
  def newQueryInstance(queryId: Long, userId: String, projectId: String, instance1Id: String): QueryInstanceType = {
    val instance1 = new QueryInstanceType()
    instance1.setQueryMasterId(String.valueOf(queryId))
    instance1.setUserId(userId)
    instance1.setGroupId(projectId)
    instance1.setQueryInstanceId(instance1Id)
    instance1.setStartDate(new NetworkTime().getXMLGregorianCalendar)
    instance1.setEndDate(new NetworkTime().getXMLGregorianCalendar)
    instance1
  }

  @Test
  def testAggregate() {
    val queryId = 134L
    val userId = "userId"
    val projectId = "projectId"
    val instance1 = newQueryInstance(queryId, userId, projectId, "instance1Id")
    val instance2 = newQueryInstance(queryId, userId, projectId, "instance2Id")
    val response1 = new ReadQueryInstancesResponse(queryId, userId, projectId, Vector(instance1))
    val response2 = new ReadQueryInstancesResponse(queryId, userId, projectId, Vector(instance1, instance2))
    val result1 = new SpinResultEntry(response1.toXml.toString(), null)
    val result2 = new SpinResultEntry(response2.toXml.toString(), null)
    val authn = new AuthenticationInfo("domain", userId, new Credential("value", false))
    val aggregator = new ReadQueryInstancesAggregator(queryId, userId, projectId)
    val actual = aggregator.aggregate(Seq(result1, result2)).asInstanceOf[ReadQueryInstancesResponse]
    assertNotNull(actual)
    assertTrue(actual.isInstanceOf[ReadQueryInstancesResponse])
    actual.queryInstances.size should equal(2)
    assertTrue(actual.queryInstances.contains(instance1))
    assertTrue(actual.queryInstances.contains(instance2))
  }
}