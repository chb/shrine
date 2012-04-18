package net.shrine.protocol

import xml.NodeSeq
import net.shrine.protocol.CRCRequestType.UserRequestType
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
final case class ReadPreviousQueriesRequest(
    override val projectId: String,
    override val waitTimeMs: Long,
    override val authn: AuthenticationInfo,
    val userId: String,
    val fetchSize: Int) extends ShrineRequest(projectId, waitTimeMs, authn) {

  val requestType = UserRequestType

  def toXml = XmlUtil.stripWhitespace(
    <readPreviousQueries>
      {headerFragment}
      <userId>{userId}</userId>
      <fetchSize>{fetchSize}</fetchSize>
    </readPreviousQueries>)

  def handle(handler: ShrineRequestHandler) = {
    handler.readPreviousQueries(this)
  }

  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <message_body>
      <ns4:psmheader>
        <user login={authn.username}>{authn.username}</user>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>0</estimated_time>
        <request_type>CRC_QRY_getQueryMasterList_fromUserId</request_type>
      </ns4:psmheader>
      <ns4:request xsi:type="ns4:user_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <user_id>{userId}</user_id>
        <group_id>{projectId}</group_id>
        <fetch_size>{fetchSize}</fetch_size>
      </ns4:request>
    </message_body>)
}

object ReadPreviousQueriesRequest extends I2b2Unmarshaller[ReadPreviousQueriesRequest] with ShrineRequestUnmarshaller[ReadPreviousQueriesRequest] {

  def fromI2b2(nodeSeq: NodeSeq): ReadPreviousQueriesRequest = {
    new ReadPreviousQueriesRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_body" \ "request" \ "user_id").text,
      (nodeSeq \ "message_body" \ "request" \ "fetch_size").text.toInt)
  }

  def fromXml(nodeSeq: NodeSeq) = {
    new ReadPreviousQueriesRequest(
      shrineProjectId(nodeSeq),
      shrineWaitTimeMs(nodeSeq),
      shrineAuthenticationInfo(nodeSeq),
      (nodeSeq \ "userId").text,
      (nodeSeq \ "fetchSize").text.toInt)
  }
}