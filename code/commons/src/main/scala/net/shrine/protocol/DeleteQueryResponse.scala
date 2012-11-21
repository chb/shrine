package net.shrine.protocol

import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.{I2b2Unmarshaller, XmlUnmarshaller}

/**
 * @author Bill Simons
 * @date 4/12/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class DeleteQueryResponse(val queryId: Long) extends ShrineResponse {
  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns6:response xsi:type="ns6:master_responseType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      <query_master>
        <query_master_id>{queryId}</query_master_id>
      </query_master>
    </ns6:response>)

  def withId(id: Long): DeleteQueryResponse = new DeleteQueryResponse(id)

  def toXml = XmlUtil.stripWhitespace(
    <deleteQueryResponse>
      <queryId>{queryId}</queryId>
    </deleteQueryResponse>)
}

object DeleteQueryResponse extends I2b2Unmarshaller[DeleteQueryResponse] with XmlUnmarshaller[DeleteQueryResponse] {
  def fromI2b2(nodeSeq: NodeSeq) = new DeleteQueryResponse((nodeSeq \ "message_body" \ "response" \ "query_master" \ "query_master_id").text.toLong)

  def fromXml(nodeSeq: NodeSeq) = new DeleteQueryResponse((nodeSeq \ "queryId").text.toLong)
}