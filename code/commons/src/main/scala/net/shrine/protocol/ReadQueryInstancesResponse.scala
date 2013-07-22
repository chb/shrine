package net.shrine.protocol

import xml.{Utility, NodeSeq}
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType
import org.spin.tools.NetworkTime.makeXMLGregorianCalendar
import net.shrine.util.XmlUtil

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
final case class ReadQueryInstancesResponse(
    val queryMasterId: Long,
    val userId: String,
    val groupId: String,
    val queryInstances: Seq[QueryInstanceType]) extends ShrineResponse with TranslatableResponse[ReadQueryInstancesResponse] {

  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns5:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:instance_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      {
        queryInstances map {x =>
          XmlUtil.stripWhitespace(
            <query_instance>
              <query_instance_id>{x.getQueryInstanceId} </query_instance_id>
              <query_master_id>{queryMasterId}</query_master_id>
              <user_id>{userId}</user_id>
              <group_id>{groupId}</group_id>
              <start_date>{x.getStartDate}</start_date>
              <end_date>{x.getEndDate}</end_date>
              <query_status_type>
                <status_type_id>6</status_type_id>
                <name>COMPLETED</name>
                <description>COMPLETED</description>
              </query_status_type>
            </query_instance>)
        }
      }
    </ns5:response>)


  def toXml = XmlUtil.stripWhitespace(
    <readQueryInstancesResponse>
      <masterId>{queryMasterId}</masterId>
      <userId>{userId}</userId>
      <groupId>{groupId}</groupId>
      {
        queryInstances map {x =>
          XmlUtil.stripWhitespace(
            <queryInstance>
              <instanceId>{x.getQueryInstanceId}</instanceId>
              <startDate>{x.getStartDate}</startDate>
              <endDate>{x.getEndDate}</endDate>
            </queryInstance>
          )
        }
      }
    </readQueryInstancesResponse>)

  def withId(id: Long): ReadQueryInstancesResponse = this.copy(queryMasterId = id)
}

object ReadQueryInstancesResponse extends I2b2Umarshaller[ReadQueryInstancesResponse] with XmlUnmarshaller[ReadQueryInstancesResponse] {
  def fromI2b2(nodeSeq: NodeSeq) = {
    val queryInstances = (nodeSeq \ "message_body" \ "response" \ "query_instance") map {x =>
      val instance = new QueryInstanceType()
      instance.setQueryInstanceId((x \ "query_instance_id").text)
      instance.setQueryMasterId((x \ "query_master_id").text)
      instance.setUserId((x \ "user_id").text)
      instance.setGroupId((x \ "group_id").text)
      instance.setStartDate(makeXMLGregorianCalendar((x \ "start_date").text))
      instance.setEndDate(makeXMLGregorianCalendar((x \ "end_date").text))
      instance
    }
    val firstInstance = queryInstances(0) //TODO - parsing error if no masters - need to deal with "no result" cases
    new ReadQueryInstancesResponse(firstInstance.getQueryMasterId.toLong, firstInstance.getUserId, firstInstance.getGroupId, queryInstances)
  }

  def fromXml(nodeSeq: NodeSeq) = {
    val masterId = (nodeSeq \ "masterId").text.toLong
    val userId = (nodeSeq \ "userId").text
    val groupId = (nodeSeq \ "groupId").text
    val queryInstances = (nodeSeq \ "queryInstance") map {x =>
      val instance = new QueryInstanceType()
      instance.setQueryInstanceId((x \ "instanceId").text)
      instance.setQueryMasterId(masterId.toString)
      instance.setUserId(userId)
      instance.setGroupId(groupId)
      instance.setStartDate(makeXMLGregorianCalendar((x \ "startDate").text))
      instance.setEndDate(makeXMLGregorianCalendar((x \ "endDate").text))
      instance
    }
    new ReadQueryInstancesResponse(masterId, userId, groupId, queryInstances)
  }
}
