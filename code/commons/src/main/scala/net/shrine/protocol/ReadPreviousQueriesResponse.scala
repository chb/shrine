package net.shrine.protocol

import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType
import xml.{NodeSeq, Utility}
import org.spin.tools.NetworkTime.makeXMLGregorianCalendar
import net.shrine.util.XmlUtil

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
final case class ReadPreviousQueriesResponse(val userId: String, val groupId: String, val queryMasters: Seq[QueryMasterType]) extends ShrineResponse {
  def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:master_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      {
        queryMasters map {x =>
          XmlUtil.stripWhitespace(
            <query_master>
              <query_master_id>{x.getQueryMasterId}</query_master_id>
              <name>{x.getName}</name>
              <user_id>{userId}</user_id>
              <group_id>{groupId}</group_id>
              <create_date>{x.getCreateDate}</create_date>
            </query_master>)
        }
      }
    </ns5:response>)

  def toXml = XmlUtil.stripWhitespace(
    <readPreviousQueriesResponse>
      <userId>{userId}</userId>
      <groupId>{groupId}</groupId>
      {
        queryMasters map {x =>
          XmlUtil.stripWhitespace(
            <queryMaster>
              <id>{x.getQueryMasterId}</id>
              <name>{x.getName}</name>
              <createDate>{x.getCreateDate}</createDate>
            </queryMaster>)
        }
      }
    </readPreviousQueriesResponse>)
}

object ReadPreviousQueriesResponse extends I2b2Umarshaller[ReadPreviousQueriesResponse] with XmlUnmarshaller[ReadPreviousQueriesResponse] {
  def fromI2b2(nodeSeq: NodeSeq) = {
    val queryMasters = (nodeSeq \ "message_body" \ "response" \ "query_master") map {x =>
      val master = new QueryMasterType()
      master.setQueryMasterId((x \ "query_master_id").text)
      master.setName((x \ "name").text)
      master.setUserId((x \ "user_id").text)
      master.setGroupId((x \ "group_id").text)
      master.setCreateDate(makeXMLGregorianCalendar((x \ "create_date").text))
      master
    }
    val firstMaster = queryMasters(0) //TODO - parsing error if no masters - need to deal with "no result" case
    new ReadPreviousQueriesResponse(firstMaster.getUserId, firstMaster.getGroupId, queryMasters)
  }

  def fromXml(nodeSeq: NodeSeq) = {
    val userId = (nodeSeq \ "userId").text
    val groupId = (nodeSeq \ "groupId").text
    val queryMasters = (nodeSeq \ "queryMaster") map {x =>
      val master = new QueryMasterType()
      master.setQueryMasterId((x \ "id").text)
      master.setName((x \ "name").text)
      master.setUserId(userId)
      master.setGroupId(groupId)
      master.setCreateDate(makeXMLGregorianCalendar((x \ "createDate").text))
      master
    }
    new ReadPreviousQueriesResponse(userId, groupId, queryMasters)
  }
}