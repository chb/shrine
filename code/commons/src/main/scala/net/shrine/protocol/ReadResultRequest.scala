package net.shrine.protocol

import CRCRequestType.ResultRequestType
import scala.xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.XmlMarshaller
import net.shrine.serialization.I2b2Marshaller
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.serialization.I2b2Unmarshaller

/**
 * @author clint
 * @date Aug 16, 2012
 */
final case class ReadResultRequest(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo,
  val resultId: Long) extends ShrineRequest(projectId, waitTimeMs, authn) with CrcRequest {

  def this(header: RequestHeader, resultId: Long) = this(header.projectId, header.waitTimeMs, header.authn, resultId)

  override val requestType: CRCRequestType = ResultRequestType

  override def handle(handler: ShrineRequestHandler): ShrineResponse = handler.readResult(this)

  override protected def i2b2MessageBody: NodeSeq = XmlUtil.stripWhitespace(
    <message_body>
      { i2b2PsmHeader }
      <ns4:request xsi:type="ns4:result_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <query_result_instance_id>{ resultId }</query_result_instance_id>
      </ns4:request>
    </message_body>)

  def toXml: NodeSeq = XmlUtil.stripWhitespace(
    <readResult>
      { headerFragment }
      <resultId>{ resultId }</resultId>
    </readResult>)
}

object ReadResultRequest extends ShrineRequestUnmarshaller[ReadResultRequest] with I2b2Unmarshaller[ReadResultRequest] {
  def fromXml(xml: NodeSeq): ReadResultRequest = {
    val header = shrineHeader(xml)

    val resultId = (xml \ "resultId").text.toLong

    new ReadResultRequest(header, resultId)
  }

  def fromI2b2(xml: NodeSeq): ReadResultRequest = {
    val header = i2b2Header(xml)

    val resultId = (xml \ "message_body" \ "request" \ "query_result_instance_id").text.toLong

    new ReadResultRequest(header, resultId)
  }
}