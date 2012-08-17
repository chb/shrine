package net.shrine.protocol

import CRCRequestType.ResultListRequestType
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
final case class ReadResultListRequest(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo,
  val instanceId: Long) extends ShrineRequest(projectId, waitTimeMs, authn) with CrcRequest {

  def this(header: RequestHeader, instanceId: Long) = this(header.projectId, header.waitTimeMs, header.authn, instanceId)
  
  override val requestType: CRCRequestType = ResultListRequestType

  override def handle(handler: ShrineRequestHandler): ShrineResponse = {
    handler.readResultList(this)
  }

  override protected def i2b2MessageBody: NodeSeq = XmlUtil.stripWhitespace(
    <message_body>
      { i2b2PsmHeader }
      <ns4:request xsi:type="ns4:instance_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <query_instance_id>{ instanceId }</query_instance_id>
      </ns4:request>
    </message_body>)

  def toXml: NodeSeq = XmlUtil.stripWhitespace(
    <readResultList>
      { headerFragment }
      <instanceId>{ instanceId }</instanceId>
    </readResultList>)
}

object ReadResultListRequest extends ShrineRequestUnmarshaller[ReadResultListRequest] with I2b2Unmarshaller[ReadResultListRequest] {
  def fromXml(xml: NodeSeq): ReadResultListRequest = {
    val header = shrineHeader(xml)
    
    val instanceId = (xml \ "instanceId").text.toLong
    
    new ReadResultListRequest(header, instanceId)
  }
  
  def fromI2b2(xml: NodeSeq): ReadResultListRequest = {
    val header = i2b2Header(xml)
    
    val instanceId = (xml \ "message_body" \ "request" \ "query_instance_id").text.toLong
    
    new ReadResultListRequest(header, instanceId)
  }
}