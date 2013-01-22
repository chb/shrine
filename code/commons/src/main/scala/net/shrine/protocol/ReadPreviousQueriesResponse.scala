package net.shrine.protocol

import xml.NodeSeq
import org.spin.tools.NetworkTime.makeXMLGregorianCalendar
import net.shrine.util.XmlUtil
import net.shrine.serialization.{ I2b2Unmarshaller, XmlUnmarshaller }

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
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class ReadPreviousQueriesResponse(val userId: String, val groupId: String, val queryMasters: Seq[QueryMaster]) extends ShrineResponse {
  override def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:master_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      {
        queryMasters.map { x =>
          XmlUtil.stripWhitespace(
            <query_master>
              <query_master_id>{ x.queryMasterId }</query_master_id>
              <name>{ x.name }</name>
              <user_id>{ userId }</user_id>
              <group_id>{ groupId }</group_id>
              <create_date>{ x.createDate }</create_date>
            </query_master>)
        }
      }
    </ns5:response>)

  override def toXml = XmlUtil.stripWhitespace(
    <readPreviousQueriesResponse>
      <userId>{ userId }</userId>
      <groupId>{ groupId }</groupId>
      {
        queryMasters.map { x =>
          XmlUtil.stripWhitespace(
            <queryMaster>
              <id>{ x.queryMasterId }</id>
              <name>{ x.name }</name>
              <createDate>{ x.createDate }</createDate>
            </queryMaster>)
        }
      }
    </readPreviousQueriesResponse>)
}

object ReadPreviousQueriesResponse extends I2b2Unmarshaller[ReadPreviousQueriesResponse] with XmlUnmarshaller[ReadPreviousQueriesResponse] {
  override def fromI2b2(nodeSeq: NodeSeq): ReadPreviousQueriesResponse = {
    val queryMasters = (nodeSeq \ "message_body" \ "response" \ "query_master").map { x =>
      val queryMasterId = (x \ "query_master_id").text
      val name = (x \ "name").text
      val userId = (x \ "user_id").text
      val groupId = (x \ "group_id").text
      val createDate = makeXMLGregorianCalendar((x \ "create_date").text)

      QueryMaster(queryMasterId, name, userId, groupId, createDate)
    }
    
    val firstMaster = queryMasters.head //TODO - parsing error if no masters - need to deal with "no result" case

    ReadPreviousQueriesResponse(firstMaster.userId, firstMaster.groupId, queryMasters)
  }

  override def fromXml(nodeSeq: NodeSeq): ReadPreviousQueriesResponse = {
    val userId = (nodeSeq \ "userId").text
    val groupId = (nodeSeq \ "groupId").text

    val queryMasters = (nodeSeq \ "queryMaster").map { x =>
      val queryMasterId = (x \ "id").text
      val name = (x \ "name").text
      val createDate = makeXMLGregorianCalendar((x \ "createDate").text)

      QueryMaster(queryMasterId, name, userId, groupId, createDate)
    }

    ReadPreviousQueriesResponse(userId, groupId, queryMasters)
  }
}