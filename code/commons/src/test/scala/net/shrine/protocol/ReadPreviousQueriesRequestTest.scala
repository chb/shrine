package net.shrine.protocol

import org.junit.Test
import org.junit.Assert.assertTrue
import xml.Utility
import net.shrine.util.XmlUtil

/**
 * @author Bill Simons
 * @date 3/11/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadPreviousQueriesRequestTest extends ShrineRequestValidator {
  val fetchSize = 20

  def messageBody = <message_body>
      <ns4:psmheader>
        <user login={username}>{username}</user>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>0</estimated_time>
        <request_type>CRC_QRY_getQueryMasterList_fromUserId</request_type>
      </ns4:psmheader>
      <ns4:request xsi:type="ns4:user_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <user_id>{username}</user_id>
        <group_id>{projectId}</group_id>
        <fetch_size>{fetchSize}</fetch_size>
      </ns4:request>
    </message_body>

  val readPreviousQueriesRequest = XmlUtil.stripWhitespace(
      <readPreviousQueries>
        {requestHeaderFragment}
        <userId>{username}</userId>
        <fetchSize>{fetchSize}</fetchSize>
      </readPreviousQueries>)

  @Test
  def testFromI2b2() {
    val translatedRequest = ReadPreviousQueriesRequest.fromI2b2(request)
    validateRequestWith(translatedRequest) {
      translatedRequest.userId should equal(username)
      translatedRequest.fetchSize should equal(fetchSize)
    }
  }

  @Test
  def testShrineRequestFromI2b2() {
    val shrineRequest = WillComeFromI2b2ShrineRequest.fromI2b2(request)
    assertTrue(shrineRequest.isInstanceOf[ReadPreviousQueriesRequest])
  }

  @Test
  def testToXml() {
    new ReadPreviousQueriesRequest(projectId, waitTimeMs, authn, username, fetchSize).toXml should equal(readPreviousQueriesRequest)
  }

  @Test
  def testToI2b2() {
    new ReadPreviousQueriesRequest(projectId, waitTimeMs, authn, username, fetchSize).toI2b2 should equal(request)
  }

  @Test
  def testFromXml() {
    val actual = ReadPreviousQueriesRequest.fromXml(readPreviousQueriesRequest)
    validateRequestWith(actual) {
      actual.userId should equal(username)
      actual.fetchSize should equal(fetchSize)
    }
  }

  @Test
  def testShrineRequestFromXml() {
    assertTrue(ShrineRequest.fromXml(readPreviousQueriesRequest).isInstanceOf[ReadPreviousQueriesRequest])
  }


}