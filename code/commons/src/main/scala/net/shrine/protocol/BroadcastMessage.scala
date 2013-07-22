package net.shrine.protocol

import xml.{NodeSeq, Utility}
import collection.JavaConversions._
import org.apache.log4j.MDC
import net.shrine.filters.LogFilter
import util.Random
import org.spin.query.message.serializer.BasicSerializer
import net.shrine.util.XmlUtil

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
class BroadcastMessage private(
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
        resultIds.map(ids => makeResultIdsElement(ids)).getOrElse(<resultIds/>)
      }
      <request>{request.toXml}</request>
    </broadcastMessage>)

  private[this] def makeResultIdsElement(ids: Seq[Long]) = {
    <resultIds>
      {
        ids map {x =>
          <resultId>{x}</resultId>
        }
      }
    </resultIds>
  }

  def getResultIdsJava(): java.util.List[java.lang.Long] =
    seqAsJavaList(resultIds.getOrElse(List()) map {x=> new java.lang.Long(x.toString)})
}

object BroadcastMessage extends XmlUnmarshaller[BroadcastMessage] {
  private val random: Random = new Random(System.currentTimeMillis)

  def fromXml(nodeSeq: NodeSeq) = {
    val idSeq = nodeSeq \ "resultIds" \ "resultId"
    val resultIds = idSeq match {
      case x if x.nonEmpty => Some(x map {y =>
        y.text.toLong
      })
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

  private def logId: Long = MDC.get(LogFilter.GRID).asInstanceOf[Long]

  private def randomId: Long = BigInt(63, random).abs.toLong

  def apply(request: RunQueryRequest): BroadcastMessage = {
    val resultIds: Seq[Long] = for {i <- 1 to request.outputTypes.size} yield randomId
    new BroadcastMessage(logId, randomId, randomId, resultIds, request)
  }

  def serializer: BasicSerializer[Object] = new BasicSerializer[Object]() {
    def fromString(s: String) = {
      BroadcastMessage.fromXml(s)
    }

    def toString(o: Object) = {
      o.asInstanceOf[BroadcastMessage].toXml.toString
    }
  }
}