package net.shrine.protocol

import xml.{ NodeSeq, XML }
import org.spin.tools.NetworkTime._
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.util.XmlUtil
import net.shrine.serialization.{ I2b2Unmarshaller, XmlUnmarshaller }
import scala.xml.NodeBuffer

/**
 * @author Bill Simons
 * @date 4/11/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * 
 * NB: Ugh, made everything optional, since the i2b2 response coming back may be "empty", as in
 * <ns4:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns4:master_responseType">
 *    <status>
 *      <condition type="DONE">DONE</condition>
 *    </status>
 * </ns4:response>
 * In which case, all the fields that qould be extracted from a query_master element will not be available
 */
final case class ReadQueryDefinitionResponse(
  val masterId: Option[Long],
  val name: Option[String],
  val userId: Option[String],
  val createDate: Option[XMLGregorianCalendar],
  val queryDefinition: Option[String]) extends ShrineResponse {

  override protected def i2b2MessageBody = XmlUtil.stripWhitespace {
    <ns6:response xsi:type="ns6:master_responseType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      {
        val xml = for {
          mId <- masterId
          queryName <- name
          uId <- userId
          date <- createDate
          queryDefXml <- queryDefinition
        } yield {
          <query_master>
            <query_master_id>{ mId }</query_master_id>
            <name>{ queryName }</name>
            <user_id>{ uId }</user_id>
            <create_date>{ date }</create_date>
            <request_xml>{ XML.loadString(queryDefXml) }</request_xml>
          </query_master>
        }
        
        xml.getOrElse(NodeSeq.Empty)
      }
    </ns6:response>
  }

  override def toXml = XmlUtil.stripWhitespace {
    <readQueryDefinitionResponse>
      {
        val xml = for {
          mId <- masterId
          queryName <- name
          uId <- userId
          date <- createDate
          queryDefXml <- queryDefinition
        } yield {
          <masterId>{ mId }</masterId>
          <name>{ queryName }</name>
          <userId>{ uId }</userId>
          <createDate>{ date }</createDate>
          <queryDefinition>{ queryDefXml }</queryDefinition>
        }
        
        xml.getOrElse(NodeSeq.Empty)
      }
    </readQueryDefinitionResponse>
  }

  override def canEqual(other: Any): Boolean = other.isInstanceOf[ReadQueryDefinitionResponse]

  //NB: Does not include create date in equality
  override def equals(other: Any): Boolean = {
    other match {
      case that: ReadQueryDefinitionResponse => (that canEqual this) &&
        masterId == that.masterId &&
        name == that.name &&
        userId == that.userId &&
        queryDefinition == that.queryDefinition
      case _ => false
    }
  }

  //NB: Does not include create date in hashCode
  override def hashCode: Int = 41 * (41 * (41 * (41 + masterId.hashCode) + name.hashCode) + userId.hashCode) + queryDefinition.hashCode
}

object ReadQueryDefinitionResponse extends I2b2Unmarshaller[ReadQueryDefinitionResponse] with XmlUnmarshaller[ReadQueryDefinitionResponse] {
  val Empty = ReadQueryDefinitionResponse(None, None, None, None, None)

  override def fromI2b2(nodeSeq: NodeSeq) = {
    val queryMasterXml = nodeSeq \ "message_body" \ "response" \ "query_master"
    
    if(queryMasterXml.isEmpty) { Empty }
    else {
    ReadQueryDefinitionResponse(
      Option((queryMasterXml \ "query_master_id").text.toLong),
      Option((queryMasterXml \ "name").text),
      Option((queryMasterXml \ "user_id").text),
      Option(makeXMLGregorianCalendar((queryMasterXml \ "create_date").text)),
      Option((queryMasterXml \ "request_xml" \ "query_definition").toString))
    }
  }

  override def fromXml(nodeSeq: NodeSeq) = ReadQueryDefinitionResponse(
    (nodeSeq \ "masterId").headOption.map(_.text.toLong),
    (nodeSeq \ "name").headOption.map(_.text),
    (nodeSeq \ "userId").headOption.map(_.text),
    (nodeSeq \ "createDate").headOption.map(xml => makeXMLGregorianCalendar(xml.text)),
    (nodeSeq \ "queryDefinition").headOption.map(_.text))
}