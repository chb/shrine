package net.shrine.protocol

import xml.NodeSeq
import org.apache.log4j.MDC
import net.shrine.filters.LogFilter
import util.Random
import org.spin.message.serializer.BasicSerializer
import net.shrine.util.XmlUtil
import net.shrine.serialization.{XmlMarshaller, XmlUnmarshaller}

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
final class BroadcastMessage private(
    val requestId: Long,
    val masterId: Option[Long],
    val instanceId: Option[Long],
    val resultIds: Option[Seq[Long]],
    val request: ShrineRequest) extends XmlMarshaller {

  def this(requestId: Long, masterId: Long, instanceId: Long, resultIds: Seq[Long], request: ShrineRequest) = {
    this (requestId, Some(masterId), Some(instanceId), Some(resultIds), request)
  }

  def this(requestId: Long, request: ShrineRequest) = this (requestId, None, None, None, request)

  def toXml = XmlUtil.stripWhitespace(
    <broadcastMessage>
      <requestId>{requestId}</requestId>
      {
        masterId.map(x => <masterId>{x}</masterId>).getOrElse(<masterId/>)
      }
      {
        instanceId.map(x => <instanceId>{x}</instanceId>).getOrElse(<instanceId/>)
      }
      {
        resultIds.map(makeResultIdsElement).getOrElse(<resultIds/>)
      }
      <request>{request.toXml}</request>
    </broadcastMessage>)

  private[this] def makeResultIdsElement(ids: Seq[Long]) = {
    <resultIds>
      {
        ids.map(x => <resultId>{x}</resultId>)
      }
    </resultIds>
  }

  def getResultIdsJava(): java.util.List[java.lang.Long] = {
    import scala.collection.JavaConverters._
    import java.lang.{Long => JLong}
    
    (for {
      ids <- resultIds.toSeq
      id <- ids
    } yield JLong.valueOf(id)).asJava
  }
}

object BroadcastMessage extends XmlUnmarshaller[BroadcastMessage] {
  private val random: Random = new Random(System.currentTimeMillis)

  def fromXml(nodeSeq: NodeSeq) = {
    val idSeq = nodeSeq \ "resultIds" \ "resultId"
    val resultIds = idSeq match {
      case x if x.nonEmpty => Some(x.map(_.text.toLong))
      case _ => None
    }

    def parseLong(s: String) = s match {
      case x if x.nonEmpty => Some(x.toLong)
      case _ => None
    }

    new BroadcastMessage(
      (nodeSeq \ "requestId").text.toLong,
      parseLong((nodeSeq \ "masterId").text),
      parseLong((nodeSeq \ "instanceId").text),
      resultIds,
      ShrineRequest.fromXml(nodeSeq \ "request" \ "_"))
  }

  //Will blow up if not called from an app-server environment
  private def log4jLogId: Long = MDC.get(LogFilter.GRID).asInstanceOf[Long]

  private def randomId: Long = BigInt(63, random).abs.toLong

  def apply(request: RunQueryRequest, logId: Option[Long] = None): BroadcastMessage = {
    val resultIds: Seq[Long] = for(i <- 1 to request.outputTypes.size) yield randomId
    
    new BroadcastMessage(logId.getOrElse(log4jLogId), randomId, randomId, resultIds, request)
  }

  def serializer: BasicSerializer[BroadcastMessage] = new BasicSerializer[BroadcastMessage] {
    override def fromString(s: String) = BroadcastMessage.fromXml(s)

    override def toString(o: BroadcastMessage) = o.toXmlString
  }
}