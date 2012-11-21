package net.shrine.protocol

import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.{I2b2Unmarshaller, XmlUnmarshaller}

/**
 * @author Bill Simons
 * @date 4/13/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * 
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 * 
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * NOTE: Now that the adapter caches/stores results from the CRC, Instead of an
 * i2b2 instance id, this class now contains the Shrine-generated, network-wide 
 * id of a query, which was used to obtain results previously obtained from the 
 * CRC from Shrine's datastore.
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */
final case class ReadInstanceResultsResponse(
    /*
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * NOTE: Now that the adapter caches/stores results from the CRC, Instead of an
     * i2b2 instance id, this class now contains the Shrine-generated, network-wide 
     * id of a query, which is used to obtain results previously obtained from the 
     * CRC from Shrine's datastore.
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */ 
    val shrineNetworkQueryId: Long,
    val results: Seq[QueryResult]) extends ShrineResponse {

  private def addIds(queryResult: QueryResult) = queryResult.withInstanceId(shrineNetworkQueryId)
  
  override protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:result_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      { //NB: Set QueryResults' instanceIds to the query id of the enclosing response
        results.map(addIds(_).toI2b2)
      }
    </ns5:response>)


  override def toXml = XmlUtil.stripWhitespace(
    <readInstanceResultsResponse>
      <shrineNetworkQueryId>{shrineNetworkQueryId}</shrineNetworkQueryId>
      <queryResults>
      { //NB: Set QueryResults' instanceIds to the query id of the enclosing response
        results map(addIds(_).toXml)
      }
      </queryResults>
    </readInstanceResultsResponse>)

  def withId(id: Long) = this.copy(shrineNetworkQueryId = id)

  def withResults(seq: Seq[QueryResult]) = this.copy(results = seq)
}

object ReadInstanceResultsResponse extends I2b2Unmarshaller[ReadInstanceResultsResponse] with XmlUnmarshaller[ReadInstanceResultsResponse] {
  override def fromI2b2(nodeSeq: NodeSeq): ReadInstanceResultsResponse = {
    val results = (nodeSeq \ "message_body" \ "response" \ "query_result_instance").map(QueryResult.fromI2b2)
    
     //TODO - parsing error if no results - need to deal with "no result" cases
    val firstResult = results.head
    
    new ReadInstanceResultsResponse(firstResult.instanceId, results)
  }

  override def fromXml(nodeSeq: NodeSeq): ReadInstanceResultsResponse = {
    val instanceId = (nodeSeq \ "shrineNetworkQueryId").text.toLong
    
    val results = (nodeSeq \ "queryResults" \ "_").map(QueryResult.fromXml)

    new ReadInstanceResultsResponse(instanceId, results)
  }
}
