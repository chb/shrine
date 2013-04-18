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
class ReadApprovedQueryTopicsRequestTest extends ShrineRequestValidator {

  def messageBody = XmlUtil.stripWhitespace {
    <message_body>
      <ns8:sheriff_header xsi:type="ns8:sheriffHeaderType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
      <ns8:sheriff_request xsi:type="ns8:sheriffRequestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
    </message_body>
  }

  val readApprovedQueryTopicsRequest = XmlUtil.stripWhitespace {
    <readApprovedQueryTopics>
      { requestHeaderFragment }
      <userId>{ username }</userId>
    </readApprovedQueryTopics>
  }

  @Test
  def testFromI2b2 {
    val translatedRequest = ReadApprovedQueryTopicsRequest.fromI2b2(request)
    validateRequestWith(translatedRequest) {
      translatedRequest.userId should equal(username)
    }
  }

  @Test
  def testShrineRequestFromI2b2 {
    val shrineRequest = HandleableShrineRequest.fromI2b2(request)

    assertTrue(shrineRequest.isInstanceOf[ReadApprovedQueryTopicsRequest])
  }

  @Test
  def testToI2b2 {
    ReadApprovedQueryTopicsRequest(projectId, waitTimeMs, authn, username).toI2b2 should equal(request)
  }

  @Test
  def testToXml {
    ReadApprovedQueryTopicsRequest(projectId, waitTimeMs, authn, username).toXml should equal(readApprovedQueryTopicsRequest)
  }

  @Test
  def testFromXml {
    val request = ReadApprovedQueryTopicsRequest.fromXml(readApprovedQueryTopicsRequest)
    validateRequestWith(request) {
      request.userId should equal(username)
    }
  }

  @Test
  def testShrineRequestFromXml {
    assertTrue(ShrineRequest.fromXml(readApprovedQueryTopicsRequest).isInstanceOf[ReadApprovedQueryTopicsRequest])
  }
}