package net.shrine.protocol

import xml.NodeSeq
import org.apache.log4j.MDC
import net.shrine.filters.LogFilter
import util.Random
import org.spin.message.serializer.BasicSerializer
import net.shrine.util.XmlUtil
import net.shrine.serialization.{ XmlMarshaller, XmlUnmarshaller }
import net.shrine.util.Try

/**
 * @author Bill Simons
 * @date 4/5/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final case class BroadcastMessage(requestId: Long, request: ShrineRequest) extends XmlMarshaller {

  override def toXml = XmlUtil.stripWhitespace(
    <broadcastMessage>
      <requestId>{ requestId }</requestId>
      <request>{ request.toXml }</request>
    </broadcastMessage>)
}

object BroadcastMessage extends XmlUnmarshaller[BroadcastMessage] {
  def apply(request: ShrineRequest): BroadcastMessage = new BroadcastMessage(randomId, request)
  
  private val random: Random = new Random(System.currentTimeMillis)

  override def fromXml(nodeSeq: NodeSeq) = {
    BroadcastMessage(
      (nodeSeq \ "requestId").text.toLong,
      ShrineRequest.fromXml(nodeSeq \ "request" \ "_"))
  }

  //TODO: why not random.nextLong, or random.nextLong.abs?
  private def randomId: Long = BigInt(63, random).abs.toLong

  object serializer extends BasicSerializer[BroadcastMessage] {
    override def fromString(s: String) = BroadcastMessage.fromXml(s)

    override def toString(o: BroadcastMessage) = o.toXmlString
  }
}