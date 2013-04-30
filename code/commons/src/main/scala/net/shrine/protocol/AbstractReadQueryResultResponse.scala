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
    val queryId: Long) extends ShrineResponse with HasQueryResults with NonI2b2ableResponse {

  override def toXml: NodeSeq = XmlUtil.stripWhitespace {
    XmlUtil.renameRootTag(rootTagName) {
      <placeHolder>
        <queryId>{ queryId }</queryId>
        <results>{ results.map(_.toXml) }</results>
      </placeHolder>
    }
  }
}

object AbstractReadQueryResultResponse {
  //
  //NB: Creatable trait and companion object implement the typeclass pattern:
  //http://www.casualmiracles.com/2012/05/03/a-small-example-of-the-typeclass-pattern-in-scala/
  //A typeclass is used here in place of an abstract method with multiple concrete implementations,
  //or another similar strategy. -Clint
  
  private trait Creatable[T] {
    def apply(id: Long, results: Seq[QueryResult]): T
  }

  private object Creatable {
    implicit val readQueryResultResponseIsCreatable: Creatable[ReadQueryResultResponse] = new Creatable[ReadQueryResultResponse] {
      //NB: Will fail loudly if QueryResults Seq is empty
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