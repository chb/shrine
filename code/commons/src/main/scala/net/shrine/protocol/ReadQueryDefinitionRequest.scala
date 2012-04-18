package net.shrine.protocol

import xml.NodeSeq
import net.shrine.protocol.CRCRequestType.GetRequestXml
import net.shrine.util.XmlUtil
import net.shrine.serialization.I2b2Unmarshaller


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
final case class ReadQueryDefinitionRequest(
    override val projectId: String,
    override val waitTimeMs: Long,
    override val authn: AuthenticationInfo,
    val queryId: Long) extends ShrineRequest(projectId, waitTimeMs, authn) {

  val requestType = GetRequestXml

  def toXml = XmlUtil.stripWhitespace(
    <readQueryDefinition>
      {headerFragment}
      <queryId>{queryId}</queryId>
    </readQueryDefinition>)

  def handle(handler: ShrineRequestHandler) = {
    handler.readQueryDefinition(this)
  }

  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <message_body>
      <ns4:psmheader>
        <user login={authn.username}>{authn.username}</user>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>0</estimated_time>
        <request_type>CRC_QRY_getRequestXml_fromQueryMasterId</request_type>
      </ns4:psmheader>
      <ns4:request xsi:type="ns4:master_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <query_master_id>{queryId}</query_master_id>
      </ns4:request>
    </message_body>)
}

object ReadQueryDefinitionRequest extends I2b2Unmarshaller[ReadQueryDefinitionRequest] with ShrineRequestUnmarshaller[ReadQueryDefinitionRequest] {

  def fromI2b2(nodeSeq: NodeSeq): ReadQueryDefinitionRequest = {
    new ReadQueryDefinitionRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_body" \ "request" \ "query_master_id").text.toLong)
  }

  def fromXml(nodeSeq: NodeSeq) = {
    new ReadQueryDefinitionRequest(
      shrineProjectId(nodeSeq),
      shrineWaitTimeMs(nodeSeq),
      shrineAuthenticationInfo(nodeSeq),
      (nodeSeq \ "queryId").text.toLong)
  }
}