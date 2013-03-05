package net.shrine.protocol

import xml.NodeSeq
import org.apache.log4j.MDC
import net.shrine.filters.LogFilter
import util.Random
import net.shrine.util.XmlUtil
import net.shrine.serialization.{ XmlMarshaller, XmlUnmarshaller }
import scala.util.Try
import org.spin.message.serializer.Stringable

/**
 * @author Bill Simons
 * @date 4/5/11
 * @link http://cbmi.med.harvard.edu
 */
final case class BroadcastMessage(requestId: Long, request: ShrineRequest) extends XmlMarshaller {

  def withRequestId(id: Long) = this.copy(requestId = id)

  def withRequest(req: ShrineRequest) = this.copy(request = req)

  override def toXml = XmlUtil.stripWhitespace(
    <broadcastMessage>
      <requestId>{ requestId }</requestId>
      <request>{ request.toXml }</request>
    </broadcastMessage>)
}

object BroadcastMessage extends XmlUnmarshaller[BroadcastMessage] {
  def apply(request: ShrineRequest): BroadcastMessage = BroadcastMessage(Ids.next, request)

  /**
   * @author clint
   * @date Nov 29, 2012
   */
  object Ids {
    private val random = new java.util.Random

    def next = random.nextLong.abs
  }

  override def fromXml(nodeSeq: NodeSeq) = {
    BroadcastMessage(
      (nodeSeq \ "requestId").text.toLong,
      ShrineRequest.fromXml(nodeSeq \ "request" \ "_"))
  }

  implicit object BroadcastMessageIsStringable extends Stringable[BroadcastMessage] {
    override def toString(message: BroadcastMessage) = message.toXmlString
  }
}