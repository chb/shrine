package net.shrine.protocol

import net.shrine.serialization.XmlMarshaller
import scala.xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.XmlUnmarshaller
import scala.util.Try
import net.shrine.util.Util
import net.shrine.protocol.handlers.ReadQueryResultHandler

/**
 * @author clint
 * @date Nov 2, 2012
 */
final case class ReadQueryResultRequest(
    override val projectId: String, //TODO: needed?
    override val waitTimeMs: Long,  //TODO: needed?
    override val authn: AuthenticationInfo, //TODO: needed?
    queryId: Long) extends ShrineRequest(projectId, waitTimeMs, authn) with NonI2b2ableRequest {
  
  override val requestType = RequestType.GetQueryResult 
  
  override def toXml: NodeSeq = XmlUtil.stripWhitespace(
    <readQueryResult>
      <projectId>{ projectId }</projectId>
      <waitTimeMs>{ waitTimeMs }</waitTimeMs>
      { authn.toXml }
      <queryId>{ queryId }</queryId>
    </readQueryResult>)
}

object ReadQueryResultRequest extends XmlUnmarshaller[ReadQueryResultRequest] {
  override def fromXml(xml: NodeSeq): ReadQueryResultRequest = {
    val resultAttempt = for {
      projectId <- Try((xml \ "projectId").text)
      waitTimeMs <- Try((xml \ "waitTimeMs").text.toLong)
      authn <- Try(xml \ "authenticationInfo").map(AuthenticationInfo.fromXml)
      queryId <- Try((xml \ "queryId").text.toLong)
    } yield {
      ReadQueryResultRequest(projectId, waitTimeMs, authn, queryId)
    }
    
    resultAttempt.toOption.orNull
  }
}