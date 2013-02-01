package net.shrine.protocol

import net.shrine.serialization.XmlMarshaller
import scala.xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.XmlUnmarshaller
import scala.util.Try
import net.shrine.util.Util

/**
 * @author clint
 * @date Nov 2, 2012
 */
final case class ReadQueryResultRequest(
    override val projectId: String, //TODO: needed?
    override val waitTimeMs: Long,  //TODO: needed?
    override val authn: AuthenticationInfo, //TODO: needed?
    queryId: Long) extends ShrineRequest(projectId, waitTimeMs, authn) {
  
  //TODO: what to do about this one, that has no meaningful i2b2 representation 
  
  //NB: Unimplemented for now
  def handle(handler: ShrineRequestHandler): ShrineResponse = Util.???

  val requestType: CRCRequestType = CRCRequestType.GetQueryResult
  
  //NB: Intentionally left out
  override def toI2b2 = Util.???

  //NB: intentionally left unimplemented
  protected def i2b2MessageBody: NodeSeq = Util.???
  
  override def toXml: NodeSeq = XmlUtil.stripWhitespace(
    <readPreviousQueryResult>
      <projectId>{ projectId }</projectId>
      <waitTimeMs>{ waitTimeMs }</waitTimeMs>
      { authn.toXml }
      <queryId>{ queryId }</queryId>
    </readPreviousQueryResult>)
}

object ReadQueryResultRequest extends XmlUnmarshaller[Try[ReadQueryResultRequest]] {
  def fromXml(xml: NodeSeq): Try[ReadQueryResultRequest] = {
    for {
      projectId <- Try((xml \ "projectId").text)
      waitTimeMs <- Try((xml \ "waitTimeMs").text.toLong)
      authn <- Try(xml \ "authenticationInfo").map(AuthenticationInfo.fromXml)
      queryId <- Try((xml \ "queryId").text.toLong)
    } yield {
      ReadQueryResultRequest(projectId, waitTimeMs, authn, queryId)
    }
  }
}