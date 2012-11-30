package net.shrine.protocol

import scala.xml.NodeSeq

import net.shrine.util.XmlUtil

/**
 * @author clint
 * @date Nov 30, 2012
 */
abstract class AbstractReadInstanceResultsResponse(
  /*
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * NOTE: Now that the adapter caches/stores results from the CRC, Instead of an
     * i2b2 instance id, this class now contains the Shrine-generated, network-wide 
     * id of a query, which is used to obtain results previously obtained from the 
     * CRC from Shrine's datastore.
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
  val shrineNetworkQueryId: Long) extends ShrineResponse with HasQueryResults {

  type ActualResponseType <: AbstractReadInstanceResultsResponse

  def withId(id: Long): ActualResponseType

  def withResults(seq: Seq[QueryResult]): ActualResponseType

  private def addIds(queryResult: QueryResult) = queryResult.withInstanceId(shrineNetworkQueryId)

  override protected final def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:result_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      { //NB: Set QueryResults' instanceIds to the query id of the enclosing response
        results.map(addIds(_).toI2b2)
      }
    </ns5:response>)

  override final def toXml = XmlUtil.stripWhitespace(
    <readInstanceResultsResponse>
      <shrineNetworkQueryId>{ shrineNetworkQueryId }</shrineNetworkQueryId>
      <queryResults>
        { //NB: Set QueryResults' instanceIds to the query id of the enclosing response
          results.map(addIds(_).toXml)
        }
      </queryResults>
    </readInstanceResultsResponse>)
}

object AbstractReadInstanceResultsResponse {
  private trait Creatable[T] {
    def apply(id: Long, results: Seq[QueryResult]): T
  }
  
  private object Creatable {
    implicit val readInstanceResultsResponseIsCreatable: Creatable[ReadInstanceResultsResponse] = new Creatable[ReadInstanceResultsResponse] {
      override def apply(id: Long, results: Seq[QueryResult]) = ReadInstanceResultsResponse(id, results)
    }
  }
  
  abstract class Companion[R <: AbstractReadInstanceResultsResponse : Creatable] {
    private val createResponse = implicitly[Creatable[R]]
    
    protected final def unmarshalFromI2b2(nodeSeq: NodeSeq): R = {
      val results = (nodeSeq \ "message_body" \ "response" \ "query_result_instance").map(QueryResult.fromI2b2)

      //TODO - parsing error if no results - need to deal with "no result" cases
      val shrineNetworkQueryId = results.head.instanceId

      createResponse(shrineNetworkQueryId, results)
    }

    protected final def unmarshalFromXml(nodeSeq: NodeSeq): R = {
      val shrineNetworkQueryId = (nodeSeq \ "shrineNetworkQueryId").text.toLong

      val results = (nodeSeq \ "queryResults" \ "_").map(QueryResult.fromXml)

      createResponse(shrineNetworkQueryId, results)
    }
  }
}