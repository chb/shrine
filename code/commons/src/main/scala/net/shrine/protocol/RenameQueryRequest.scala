package net.shrine.protocol

import xml.NodeSeq
import net.shrine.protocol.CRCRequestType.MasterRenameRequestType
import net.shrine.util.XmlUtil
import net.shrine.serialization.I2b2Unmarshaller

/**
 * @author Bill Simons
 * @date 3/28/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class RenameQueryRequest(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo,
  val queryId: Long,
  val queryName: String) extends ShrineRequest(projectId, waitTimeMs, authn) with CrcRequest with TranslatableRequest[RenameQueryRequest] {

  val requestType = MasterRenameRequestType

  override def toXml = XmlUtil.stripWhitespace(
    <renameQuery>
      { headerFragment }
      <queryId>{ queryId }</queryId>
      <queryName>{ queryName }</queryName>
    </renameQuery>)

  override def handle(handler: ShrineRequestHandler, shouldBroadcast: Boolean) = handler.renameQuery(this, shouldBroadcast)

  def withId(id: Long) = this.copy(queryId = id)

  override def withAuthn(ai: AuthenticationInfo) = this.copy(authn = ai)

  override def withProject(proj: String) = this.copy(projectId = proj)

  protected override def i2b2MessageBody = XmlUtil.stripWhitespace(
    <message_body>
      { i2b2PsmHeader }
      <ns4:request xsi:type="ns4:master_rename_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <user_id>{ authn.username }</user_id>
        <query_master_id>{ queryId }</query_master_id>
        <query_name>{ queryName }</query_name>
      </ns4:request>
    </message_body>)
}

object RenameQueryRequest extends I2b2Unmarshaller[RenameQueryRequest] with ShrineRequestUnmarshaller[RenameQueryRequest] {

  override def fromI2b2(nodeSeq: NodeSeq): RenameQueryRequest = {
    new RenameQueryRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_body" \ "request" \ "query_master_id").text.toLong,
      (nodeSeq \ "message_body" \ "request" \ "query_name").text)
  }

  override def fromXml(nodeSeq: NodeSeq) = {
    new RenameQueryRequest(
      shrineProjectId(nodeSeq),
      shrineWaitTimeMs(nodeSeq),
      shrineAuthenticationInfo(nodeSeq),
      (nodeSeq \ "queryId").text.toLong,
      (nodeSeq \ "queryName").text)
  }
}