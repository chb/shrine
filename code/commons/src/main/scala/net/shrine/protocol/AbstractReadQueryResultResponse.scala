package net.shrine.protocol

import scala.xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.XmlUnmarshaller
import scala.util.Try
import net.shrine.util.Util

/**
 * @author clint
 * @date Dec 4, 2012
 */
abstract class AbstractReadQueryResultResponse(
    rootTagName: String,
    val queryId: Long) extends ShrineResponse with HasQueryResults {

  override def toXml: NodeSeq = XmlUtil.stripWhitespace {
    XmlUtil.renameRootTag(rootTagName) {
      <placeHolder>
        <queryId>{ queryId }</queryId>
        <results>{ results.map(_.toXml) }</results>
      </placeHolder>
    }
  }

  override protected def i2b2MessageBody = Util.???

  override def toI2b2 = ErrorResponse("ReadQueryResultResponse can't be marshalled to i2b2 XML, as it has no i2b2 analog").toI2b2
}

object AbstractReadQueryResultResponse {
  private trait Creatable[T] {
    def apply(id: Long, results: Seq[QueryResult]): T
  }

  private object Creatable {
    implicit val readQueryResultResponseIsCreatable: Creatable[ReadQueryResultResponse] = new Creatable[ReadQueryResultResponse] {
      override def apply(id: Long, results: Seq[QueryResult]) = ReadQueryResultResponse(id, results.head)
    }

    implicit val aggregatedReadQueryResultResponseIsCreatable: Creatable[AggregatedReadQueryResultResponse] = new Creatable[AggregatedReadQueryResultResponse] {
      override def apply(id: Long, results: Seq[QueryResult]) = AggregatedReadQueryResultResponse(id, results)
    }
  }

  abstract class Companion[R: Creatable] extends XmlUnmarshaller[R] {
    private val makeResponse = implicitly[Creatable[R]]

    override def fromXml(xml: NodeSeq): R = {
      val queryId = (xml \ "queryId").text.toLong
      val results = (xml \ "results" \ "queryResult").map(QueryResult.fromXml)

      makeResponse(queryId, results)
    }
  }
}