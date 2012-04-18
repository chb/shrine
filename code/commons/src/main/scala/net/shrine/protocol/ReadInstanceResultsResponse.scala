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
 */
final case class ReadInstanceResultsResponse(
    val queryInstanceId: Long,
    val results: Seq[QueryResult]) extends ShrineResponse with TranslatableResponse[ReadInstanceResultsResponse] {

  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:result_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      { //NB: Set QueryResults' instanceIds to the instanceId of the enclosing response
        results map {x =>
          x.withInstanceId(queryInstanceId).toI2b2
        }
      }
    </ns5:response>)


  def toXml = XmlUtil.stripWhitespace(
    <readInstanceResultsResponse>
      <instanceId>{queryInstanceId}</instanceId>
      <queryResults>
      { //NB: Set QueryResults' instanceIds to the instanceId of the enclosing response
        results map {x =>
          x.withInstanceId(queryInstanceId).toXml
        }
      }
      </queryResults>
    </readInstanceResultsResponse>)

  def withId(id: Long) = this.copy(queryInstanceId = id)

  def withResults(seq: Seq[QueryResult]) = this.copy(results = seq)
}

object ReadInstanceResultsResponse extends I2b2Unmarshaller[ReadInstanceResultsResponse] with XmlUnmarshaller[ReadInstanceResultsResponse] {
  def fromI2b2(nodeSeq: NodeSeq) = {
    val results = (nodeSeq \ "message_body" \ "response" \ "query_result_instance") map {
      QueryResult.fromI2b2
    }
    val firstResult = results(0) //TODO - parsing error if no results - need to deal with "no result" cases
    new ReadInstanceResultsResponse(firstResult.instanceId, results)
  }

  def fromXml(nodeSeq: NodeSeq) = {
    val instanceId = (nodeSeq \ "instanceId").text.toLong
    val results = (nodeSeq \ "queryResults" \ "_") map {
      QueryResult.fromXml
    }
    new ReadInstanceResultsResponse(instanceId, results)
  }
}
