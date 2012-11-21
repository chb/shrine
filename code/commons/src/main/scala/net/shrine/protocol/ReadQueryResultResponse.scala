package net.shrine.protocol

import net.shrine.serialization.XmlMarshaller
import net.shrine.util.XmlUtil
import scala.xml.NodeSeq
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.util.Try

/**
 * @author clint
 * @date Nov 2, 2012
 */
final case class ReadQueryResultResponse(queryId: Long, results: Seq[QueryResult]) extends ShrineResponse {
  override def toXml: NodeSeq = XmlUtil.stripWhitespace(
    <readPreviousQueryResultResponse>
      <queryId>{ queryId }</queryId>
      <results>{ results.map(_.toXml) }</results>
    </readPreviousQueryResultResponse>)
    
  override protected def i2b2MessageBody = null
  
  override def toI2b2 = ErrorResponse("ReadQueryResultResponse can't be marshalled to i2b2 XML, as it has no i2b2 analog").toI2b2 
}

object ReadQueryResultResponse extends XmlUnmarshaller[Try[ReadQueryResultResponse]] {
  override def fromXml(xml: NodeSeq): Try[ReadQueryResultResponse] = {
    for {
      queryId <- Try((xml \ "queryId").text.toLong)
      results <- Try((xml \ "results" \ "queryResult").map(QueryResult.fromXml))
    } yield {
      ReadQueryResultResponse(queryId, results)
    }
  }
}