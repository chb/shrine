package net.shrine.protocol

import scala.xml.NodeSeq

import org.spin.message.serializer.Stringable

import net.shrine.serialization.XmlMarshaller
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.util.XmlUtil

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