package net.shrine.protocol

import xml.{Utility, NodeSeq}
import net.shrine.serializers.crc.CRCRequestType.SheriffRequestType
import net.shrine.util.XmlUtil


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
    val userId: String) extends ShrineRequest(projectId, waitTimeMs, authn) {

  val requestType = SheriffRequestType

  def toXml = XmlUtil.stripWhitespace(
    <readApprovedQueryTopics>
      {headerFragment}
      <userId>{userId}</userId>
    </readApprovedQueryTopics>)

  def handle(handler: ShrineRequestHandler) = {
    handler.readApprovedQueryTopics(this)
  }

  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <message_body>
      <ns8:sheriff_header xsi:type="ns8:sheriffHeaderType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
      <ns8:sheriff_request xsi:type="ns8:sheriffRequestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
    </message_body>)
}

object ReadApprovedQueryTopicsRequest extends ShrineRequestUnmarshaller[ReadApprovedQueryTopicsRequest] with I2b2Umarshaller[ReadApprovedQueryTopicsRequest] {

  def fromI2b2(nodeSeq: NodeSeq): ReadApprovedQueryTopicsRequest = {
    new ReadApprovedQueryTopicsRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_header" \ "security" \ "username").text)
  }

  def fromXml(nodeSeq: NodeSeq) = {
    new ReadApprovedQueryTopicsRequest(
      shrineProjectId(nodeSeq),
      shrineWaitTimeMs(nodeSeq),
      shrineAuthenticationInfo(nodeSeq),
      (nodeSeq \ "userId").text)
  }
}