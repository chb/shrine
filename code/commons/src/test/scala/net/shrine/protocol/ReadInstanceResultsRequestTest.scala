package net.shrine.protocol

import org.junit.Test
import org.junit.Assert.assertTrue
import xml.Utility
import net.shrine.util.XmlUtil

/**
 * @author Bill Simons
 * @date 3/17/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadInstanceResultsRequestTest extends ShrineRequestValidator {
  val instanceId = 1105351618885108053L

  def messageBody = <message_body>
      <ns4:psmheader>
        <user login={username}>{username}</user>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>0</estimated_time>
        <request_type>CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId</request_type>
      </ns4:psmheader>
        <ns4:request xsi:type="ns4:instance_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <query_instance_id>{instanceId}</query_instance_id>
        </ns4:request>
    </message_body>

  val readInstanceResultsRequest = XmlUtil.stripWhitespace(
      <readInstanceResults>
        {requestHeaderFragment}
        <instanceId>{instanceId}</instanceId>
      </readInstanceResults>)


  @Test
  def testFromI2b2() {
    val translatedRequest = ReadInstanceResultsRequest.fromI2b2(request)
    validateRequestWith(translatedRequest) {
      translatedRequest.instanceId should equal(instanceId)
    }
  }

  @Test
  def testShrineRequestFromI2b2() {
    val shrineRequest = ShrineRequest.fromI2b2(request)
    assertTrue(shrineRequest.isInstanceOf[ReadInstanceResultsRequest])
  }

  @Test
  def testToI2b2() {
    new ReadInstanceResultsRequest(projectId, waitTimeMs, authn, instanceId).toI2b2 should equal(request)
  }

  @Test
  def testToXml() {
    new ReadInstanceResultsRequest(projectId, waitTimeMs, authn, instanceId).toXml should equal(readInstanceResultsRequest)
  }

  @Test
  def testFromXml() {
    val actual = ReadInstanceResultsRequest.fromXml(readInstanceResultsRequest)
    validateRequestWith(actual) {
      actual.instanceId should equal(instanceId)
    }
  }

  @Test
  def testShrineRequestFromXml() {
    assertTrue(ShrineRequest.fromXml(readInstanceResultsRequest).isInstanceOf[ReadInstanceResultsRequest])
  }

}