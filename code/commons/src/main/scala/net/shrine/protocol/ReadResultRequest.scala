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
  val localResultId: String) extends ShrineRequest(projectId, waitTimeMs, authn) with CrcRequest {

  def this(header: RequestHeader, localResultId: String) = this(header.projectId, header.waitTimeMs, header.authn, localResultId)

  override val requestType: CRCRequestType = ResultRequestType
  
  //NB: This request is never sent through the broadcaster-aggregator/shrine service, so it doesn't make sense
  //to have it be handled by a ShrineRequestHandler.
  
  override protected def i2b2MessageBody: NodeSeq = XmlUtil.stripWhitespace {
    <message_body>
      { i2b2PsmHeader }
      <ns4:request xsi:type="ns4:result_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <query_result_instance_id>{ localResultId }</query_result_instance_id>
      </ns4:request>
    </message_body>
  }

  override def toXml: NodeSeq = XmlUtil.stripWhitespace {
    <readResult>
      { headerFragment }
      <resultId>{ localResultId }</resultId>
    </readResult>
  }
}

object ReadResultRequest extends ShrineRequestUnmarshaller[ReadResultRequest] with I2b2Unmarshaller[ReadResultRequest] {
  override def fromXml(xml: NodeSeq): ReadResultRequest = {
    val header = shrineHeader(xml)

    //NB: This is the LOCAL, NOT NETWORK, resultId
    val resultId = (xml \ "resultId").text

    new ReadResultRequest(header, resultId)
  }

  override def fromI2b2(xml: NodeSeq): ReadResultRequest = {
    val header = i2b2Header(xml)

    //NB: This is the LOCAL, NOT NETWORK, resultId
    val resultId = (xml \ "message_body" \ "request" \ "query_result_instance_id").text

    new ReadResultRequest(header, resultId)
  }
}