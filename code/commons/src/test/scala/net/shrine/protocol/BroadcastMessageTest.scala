package net.shrine.protocol

import org.scalatest.junit.{AssertionsForJUnit, ShouldMatchersForJUnit}
import org.junit.Test
import org.junit.Assert.{assertTrue, assertNotNull}
import xml.Utility
import net.shrine.util.XmlUtil

/**
 * @author Bill Simons
 * @date 4/5/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class BroadcastMessageTest extends AssertionsForJUnit with ShouldMatchersForJUnit with XmlSerializableValidator {
  val requestId = 123456
  val masterId = 875875
  val instanceId = 984757
  val resultId1 = 656565
  val resultId2 = 1212121
  val request = new ReadPreviousQueriesRequest("projectId", 10, new AuthenticationInfo("domain", "username", new Credential("cred", false)), "username", 20)
  val message = XmlUtil.stripWhitespace(
        <broadcastMessage>
          <requestId>{requestId}</requestId>
          <masterId>{masterId}</masterId>
          <instanceId>{instanceId}</instanceId>
          <resultIds>
            <resultId>{resultId1}</resultId>
            <resultId>{resultId2}</resultId>
          </resultIds>
          <request>{request.toXml}</request>
        </broadcastMessage>)

  @Test
  def testFromXml() {
    val actual = BroadcastMessage.fromXml(message)
    actual.requestId should equal(requestId)
    actual.masterId.get should equal(masterId)
    actual.instanceId.get should equal(instanceId)
    actual.resultIds.get should equal(Vector(resultId1, resultId2))
    assertNotNull(actual.request)
    assertTrue(actual.request.isInstanceOf[ReadPreviousQueriesRequest])
  }

  @Test
  def testFromXmlWithoutOptionalElements() {
    val actual = BroadcastMessage.fromXml(XmlUtil.stripWhitespace(
        <broadcastMessage>
            <requestId>{requestId}</requestId>
            <masterId/>
            <instanceId/>
            <resultIds/>
            <request>{request.toXml}</request>
          </broadcastMessage>))
    actual.requestId should equal(requestId)
    actual.masterId should equal(None)
    actual.instanceId should equal(None)
    actual.resultIds should equal(None)
    assertNotNull(actual.request)
    assertTrue(actual.request.isInstanceOf[ReadPreviousQueriesRequest])
  }

  @Test
  def testToXml() {
    new BroadcastMessage(requestId, masterId, instanceId, Vector(resultId1, resultId2), request).toXml should equal(message)
  }

  @Test
  def testToXmlWithAuxConstructor() {
    val expected = XmlUtil.stripWhitespace(
    <broadcastMessage>
          <requestId>{requestId}</requestId>
          <masterId/>
          <instanceId/>
          <resultIds/>
          <request>{request.toXml}</request>
        </broadcastMessage>)
    new BroadcastMessage(requestId, request).toXml should equal(expected)
  }
}