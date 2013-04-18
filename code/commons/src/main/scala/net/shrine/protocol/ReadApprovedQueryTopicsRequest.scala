package net.shrine.protocol

import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.I2b2Unmarshaller
import net.shrine.protocol.handlers.ReadApprovedTopicsHandler

/**
 * @author Bill Simons
 * @date 3/9/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * 
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class ReadApprovedQueryTopicsRequest(
    override val projectId: String,
    override val waitTimeMs: Long,
    override val authn: AuthenticationInfo,
    val userId: String) extends ShrineRequest(projectId, waitTimeMs, authn) with HandleableShrineRequest {

  override val requestType = RequestType.SheriffRequest
  
  override def handle(handler: ShrineRequestHandler, shouldBroadcast: Boolean) = handler.readApprovedQueryTopics(this, shouldBroadcast)

  override def toXml = XmlUtil.stripWhitespace {
    <readApprovedQueryTopics>
      {headerFragment}
      <userId>{userId}</userId>
    </readApprovedQueryTopics>
  }
  
  protected override def i2b2MessageBody = XmlUtil.stripWhitespace {
    <message_body>
      <ns8:sheriff_header xsi:type="ns8:sheriffHeaderType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
      <ns8:sheriff_request xsi:type="ns8:sheriffRequestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
    </message_body>
  }
}

object ReadApprovedQueryTopicsRequest extends ShrineRequestUnmarshaller[ReadApprovedQueryTopicsRequest] with I2b2Unmarshaller[ReadApprovedQueryTopicsRequest] {

  override def fromI2b2(nodeSeq: NodeSeq): ReadApprovedQueryTopicsRequest = {
    ReadApprovedQueryTopicsRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_header" \ "security" \ "username").text)
  }

  override def fromXml(nodeSeq: NodeSeq) = {
    ReadApprovedQueryTopicsRequest(
      shrineProjectId(nodeSeq),
      shrineWaitTimeMs(nodeSeq),
      shrineAuthenticationInfo(nodeSeq),
      (nodeSeq \ "userId").text)
  }
}