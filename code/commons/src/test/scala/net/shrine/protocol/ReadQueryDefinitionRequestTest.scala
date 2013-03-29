package net.shrine.protocol

import org.junit.Test
import org.junit.Assert.assertTrue
import xml.Utility
import net.shrine.util.XmlUtil

/**
 * @author Bill Simons
 * @date 3/14/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadQueryDefinitionRequestTest extends ShrineRequestValidator {
  val queryId = 2422297885846950097L

  def messageBody = <message_body>
      <ns4:psmheader>
        <user login={username}>{username}</user>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>0</estimated_time>
        <request_type>CRC_QRY_getRequestXml_fromQueryMasterId</request_type>
      </ns4:psmheader>
      <ns4:request xsi:type="ns4:master_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <query_master_id>{queryId}</query_master_id>
      </ns4:request>
    </message_body>

  val readQueryDefinitionRequest = XmlUtil.stripWhitespace(
      <readQueryDefinition>
        {requestHeaderFragment}
        <queryId>{queryId}</queryId>
      </readQueryDefinition>)

  @Test
  def testFromI2b2() {
    val translatedRequest = ReadQueryDefinitionRequest.fromI2b2(request)
    validateRequestWith(translatedRequest) {
      translatedRequest.queryId should equal(queryId)
    }
  }

  @Test
  def testShrineRequestFromI2b2() {
    val shrineRequest = WillComeFromI2b2ShrineRequest.fromI2b2(request)
    assertTrue(shrineRequest.isInstanceOf[ReadQueryDefinitionRequest])
  }

  @Test
  def testToXml() {
    new ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, queryId).toXml should equal(readQueryDefinitionRequest)
  }

  @Test
  def testToI2b2() {
    new ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, queryId).toI2b2 should equal(request)
  }

  @Test
  def testFromXml() {
    val actual = ReadQueryDefinitionRequest.fromXml(readQueryDefinitionRequest)
    validateRequestWith(actual) {
      actual.queryId should equal(queryId)
    }
  }

  @Test
  def testShrineRequestFromXml() {
    assertTrue(ShrineRequest.fromXml(readQueryDefinitionRequest).isInstanceOf[ReadQueryDefinitionRequest])
  }


}