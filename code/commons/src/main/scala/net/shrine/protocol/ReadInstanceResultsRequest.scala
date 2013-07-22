package net.shrine.protocol

import xml.{Utility, NodeSeq}
import net.shrine.serializers.crc.CRCRequestType.InstanceRequestType
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
 * 
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class ReadInstanceResultsRequest(
    override val projectId: String,
    override val waitTimeMs: Long,
    override val authn: AuthenticationInfo,
    val instanceId: Long) extends ShrineRequest(projectId, waitTimeMs, authn) with TranslatableRequest[ReadInstanceResultsRequest] {

  val requestType = InstanceRequestType

  def toXml = XmlUtil.stripWhitespace(
    <readInstanceResults>
      {headerFragment}
      <instanceId>{instanceId}</instanceId>
    </readInstanceResults>)

  def handle(handler: ShrineRequestHandler) = {
    handler.readInstanceResults(this)
  }

  def withId(id: Long) = new ReadInstanceResultsRequest(projectId, waitTimeMs, authn, id)

  def withProject(proj: String) = new ReadInstanceResultsRequest(proj, waitTimeMs, authn, instanceId)

  def withAuthn(ai: AuthenticationInfo) = new ReadInstanceResultsRequest(projectId, waitTimeMs, ai, instanceId)

  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <message_body>
      <ns4:psmheader>
        <user login={authn.username}>{authn.username}</user>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>0</estimated_time>
        <request_type>CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId</request_type>
      </ns4:psmheader>
        <ns4:request xsi:type="ns4:instance_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <query_instance_id>{instanceId}</query_instance_id>
        </ns4:request>
    </message_body>)
}

object ReadInstanceResultsRequest extends I2b2Umarshaller[ReadInstanceResultsRequest] with ShrineRequestUnmarshaller[ReadInstanceResultsRequest] {

  def fromI2b2(nodeSeq: NodeSeq): ReadInstanceResultsRequest = {
    new ReadInstanceResultsRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_body" \ "request" \ "query_instance_id").text.toLong)
  }

  def fromXml(nodeSeq: NodeSeq) = {
    new ReadInstanceResultsRequest(
      shrineProjectId(nodeSeq),
      shrineWaitTimeMs(nodeSeq),
      shrineAuthenticationInfo(nodeSeq),
      (nodeSeq \ "instanceId").text.toLong)

  }
}