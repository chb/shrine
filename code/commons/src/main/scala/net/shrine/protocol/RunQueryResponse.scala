package net.shrine.protocol

import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime.makeXMLGregorianCalendar
import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.protocol.query.QueryDefinition
import scala.xml.Elem
import scala.xml.Atom
import net.shrine.serialization.{I2b2Unmarshaller, XmlUnmarshaller}

/**
 * @author Bill Simons
 * @date 4/15/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * 
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class RunQueryResponse(
    val queryId: Long,
    val createDate: XMLGregorianCalendar,
    val userId: String,
    val groupId: String,
    val requestXml: QueryDefinition,
    val queryInstanceId: Long,
    val results: Seq[QueryResult]) extends ShrineResponse with TranslatableResponse[RunQueryResponse] {

  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:master_instance_result_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      <query_master>
        <query_master_id>{queryId}</query_master_id>
        <name>{queryName}</name>
        <user_id>{userId}</user_id>
        <group_id>{groupId}</group_id>
        <create_date>{createDate}</create_date>
        <request_xml>{requestXml.toI2b2}</request_xml>
      </query_master>
      <query_instance>
        <query_instance_id>{queryInstanceId}</query_instance_id>
        <query_master_id>{queryId}</query_master_id>
        <user_id>{userId}</user_id>
        <group_id>{groupId}</group_id>
        <query_status_type>
          <status_type_id>6</status_type_id>
          <name>COMPLETED</name>
          <description>COMPLETED</description>
        </query_status_type>
      </query_instance>
      {
        results.map(_.withInstanceId(queryInstanceId).toI2b2)
      }
    </ns5:response>)


  def toXml = XmlUtil.stripWhitespace(
    <runQueryResponse>
      <queryId>{queryId}</queryId>
      <instanceId>{queryInstanceId}</instanceId>
      <userId>{userId}</userId>
      <groupId>{groupId}</groupId>
      <requestXml>{requestXml.toXml}</requestXml>
      <createDate>{createDate}</createDate>
      <queryResults>
      {
        results.map(_.withInstanceId(queryInstanceId).toXml)
      }
      </queryResults>
    </runQueryResponse>)

  def withId(id: Long): RunQueryResponse = this.copy(queryId = id)

  def withInstanceId(id: Long): RunQueryResponse = this.copy(queryInstanceId = id)

  def withResults(seq: Seq[QueryResult]): RunQueryResponse = this.copy(results = seq)

  def queryName = requestXml.name
  
  def resultsPartitioned = results.partition(!_.isError)
}

object RunQueryResponse extends I2b2Unmarshaller[RunQueryResponse] with XmlUnmarshaller[RunQueryResponse] {
  def fromI2b2(nodeSeq: NodeSeq) = {
    def firstChild(nodeSeq: NodeSeq) = nodeSeq.head.asInstanceOf[Elem].child.head
    
    val results = (nodeSeq \ "message_body" \ "response" \ "query_result_instance").map(QueryResult.fromI2b2)
    val queryId = (nodeSeq \ "message_body" \ "response" \ "query_master" \ "query_master_id").text.toLong
    val userId = (nodeSeq \ "message_body" \ "response" \ "query_master" \ "user_id").text
    val groupId = (nodeSeq \ "message_body" \ "response" \ "query_master" \ "group_id").text
    val createDate = (nodeSeq \ "message_body" \ "response" \ "query_master" \ "create_date").text

    def asString(a: Atom[_]): String = a.data.asInstanceOf[String]
    
    def holdsString(a: Atom[_]): Boolean = a.data.isInstanceOf[String] 
    
    //NB: We need to handle the query def situtation two different ways:
    // 1) Where the query def is an escaped String, as is returned by the CRC
    // 2) Where the query def is plain XML, as is provided by Shrine
    val requestXml = firstChild(nodeSeq \ "message_body" \ "response" \ "query_master" \ "request_xml") match {
      case a: Atom[_] if holdsString(a) => QueryDefinition.fromI2b2(asString(a))
      case xml: NodeSeq => QueryDefinition.fromI2b2(xml)
    }
    
    val queryInstanceId = (nodeSeq \ "message_body" \ "response" \ "query_instance" \ "query_instance_id").text.toLong
    
    //TODO: Remove unsafe requestXml.get call
    new RunQueryResponse(queryId, makeXMLGregorianCalendar(createDate), userId, groupId, requestXml.get, queryInstanceId, results)
  }

  def fromXml(nodeSeq: NodeSeq) = {
    val results = (nodeSeq \ "queryResults" \ "_").map(QueryResult.fromXml)

    new RunQueryResponse(
      (nodeSeq \ "queryId").text.toLong,
      makeXMLGregorianCalendar((nodeSeq \ "createDate").text),
      (nodeSeq \ "userId").text,
      (nodeSeq \ "groupId").text,
      QueryDefinition.fromXml(nodeSeq \ "requestXml" \ "queryDefinition").get, //TODO: Remove unsafe get call
      (nodeSeq \ "instanceId").text.toLong,
      results)
  }
}