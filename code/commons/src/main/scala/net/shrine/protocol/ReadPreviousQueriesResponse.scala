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
final case class ReadPreviousQueriesResponse(
    val userId: Option[String], 
    val groupId: Option[String], 
    val queryMasters: Seq[QueryMaster]) extends ShrineResponse {
  
  override def i2b2MessageBody = XmlUtil.stripWhitespace {
    <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:master_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      {
        for {
          uid <- userId.toSeq
          gid <- groupId.toSeq
          master <- queryMasters
        } yield {
          XmlUtil.stripWhitespace {
            <query_master>
              <query_master_id>{ master.queryMasterId }</query_master_id>
              <name>{ master.name }</name>
              <user_id>{ uid }</user_id>
              <group_id>{ gid }</group_id>
              <create_date>{ master.createDate }</create_date>
            </query_master>
          }
        }
      }
    </ns5:response>
  }

  override def toXml = XmlUtil.stripWhitespace {
    <readPreviousQueriesResponse>
      { 
        //TODO: Is this right?
        userId.map(uid => <userId>{ uid }</userId>).orNull
      }
      {
        //TODO: Is this right?
        groupId.map(gid => <groupId>{ gid }</groupId>).orNull
      }
      {
        queryMasters.map { master =>
          XmlUtil.stripWhitespace {
            <queryMaster>
              <id>{ master.queryMasterId }</id>
              <name>{ master.name }</name>
              <createDate>{ master.createDate }</createDate>
            </queryMaster>
          }
        }
      }
    </readPreviousQueriesResponse>
  }
}

object ReadPreviousQueriesResponse extends I2b2Unmarshaller[ReadPreviousQueriesResponse] with XmlUnmarshaller[ReadPreviousQueriesResponse] {
  override def fromI2b2(nodeSeq: NodeSeq): ReadPreviousQueriesResponse = {
    val queryMasters = (nodeSeq \ "message_body" \ "response" \ "query_master").map { querymasterXml =>
      val queryMasterId = (querymasterXml \ "query_master_id").text
      val name = (querymasterXml \ "name").text
      val userId = (querymasterXml \ "user_id").text
      val groupId = (querymasterXml \ "group_id").text
      val createDate = makeXMLGregorianCalendar((querymasterXml \ "create_date").text)

      QueryMaster(queryMasterId, name, userId, groupId, createDate)
    }

    val firstMaster = queryMasters.headOption

    ReadPreviousQueriesResponse(firstMaster.map(_.userId), firstMaster.map(_.groupId), queryMasters)
  }

  override def fromXml(nodeSeq: NodeSeq): ReadPreviousQueriesResponse = {
    val userId = (nodeSeq \ "userId").text.trim
    val groupId = (nodeSeq \ "groupId").text.trim

    val queryMasters = (nodeSeq \ "queryMaster").map { querymasterXml =>
      val queryMasterId = (querymasterXml \ "id").text
      val name = (querymasterXml \ "name").text
      val createDate = makeXMLGregorianCalendar((querymasterXml \ "createDate").text)

      QueryMaster(queryMasterId, name, userId, groupId, createDate)
    }

    def notEmpty(o: Option[String]) = o.filter(!_.isEmpty)

    ReadPreviousQueriesResponse(notEmpty(Option(userId)), notEmpty(Option(groupId)), queryMasters)
  }
}