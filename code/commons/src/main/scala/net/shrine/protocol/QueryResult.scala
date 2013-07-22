package net.shrine.protocol

import javax.xml.datatype.XMLGregorianCalendar
import xml.{NodeSeq, Utility}
import org.spin.tools.NetworkTime.makeXMLGregorianCalendar
import net.shrine.util.XmlUtil

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
final case class QueryResult(
    val resultId: Long,
    val instanceId: Long,
    val resultType: String,
    val setSize: Long,
    val startDate: Option[XMLGregorianCalendar],
    val endDate: Option[XMLGregorianCalendar],
    val description: Option[String],
    val statusType: String,
    val statusMessage: Option[String]) extends XmlMarshaller with I2b2Marshaller {

  def this(resultId: Long, instanceId: Long, resultType: String, setSize: Long, startDate: XMLGregorianCalendar, endDate: XMLGregorianCalendar, statusType: String) = {
    this (resultId, instanceId, resultType, setSize, Some(startDate), Some(endDate), None, statusType, None)
  }

  def this(resultId: Long, instanceId: Long, resultType: String, setSize: Long, startDate: XMLGregorianCalendar, endDate: XMLGregorianCalendar, description: String, statusType: String) = {
    this (resultId, instanceId, resultType, setSize, Some(startDate), Some(endDate), Some(description), statusType, None)
  }

  def toI2b2 = XmlUtil.stripWhitespace(
    <query_result_instance>
      <result_instance_id>{resultId}</result_instance_id>
      <query_instance_id>{instanceId}</query_instance_id>
      {
        description.map(x => <description>{x}</description>).orNull
      }
      <query_result_type>
        <name>{resultType}</name>
        {
          resultType match {
            case name if name == "PATIENTSET" => <result_type_id>1</result_type_id><display_type>LIST</display_type><visual_attribute_type>LA</visual_attribute_type><description>Patient list</description>
            case name if name == "PATIENT_COUNT_XML" => <result_type_id>4</result_type_id><display_type>CATNUM</display_type><visual_attribute_type>LA</visual_attribute_type><description>Number of patients</description>
            case _ => null
          }
        }
      </query_result_type>
      <set_size>{setSize}</set_size>
      {
        startDate.map(x => <start_date>{x}</start_date>).orNull
      }
      {
        endDate.map(x => <end_date>{x}</end_date>).orNull
      }
      <query_status_type>
        <name>{statusType}</name>
        {
          statusType match {
            case name if name == "FINISHED" => <status_type_id>3</status_type_id><description>FINISHED</description>
            case name if name == "PROCESSING" => <status_type_id>2</status_type_id><description>PROCESSING</description>
            case name if name == "ERROR" => statusMessage.map(x => <description>{x}</description>).orNull
          }
        }
      </query_status_type>
    </query_result_instance>)

  def toXml = XmlUtil.stripWhitespace(
    <queryResult>
      <resultId>{resultId}</resultId>
      <instanceId>{instanceId}</instanceId>
      <resultType>{resultType}</resultType>
      <setSize>{setSize}</setSize>
      {
        startDate.map(x => <startDate>{x}</startDate>).orNull
      }
      {
        endDate.map(x => <endDate>{x}</endDate>).orNull
      }
      {
        description.map(x => <description>{x}</description>).orNull
      }
      <status>{statusType}</status>
      {
       statusMessage.map(x => <statusMessage>{x}</statusMessage>).orNull
      }
    </queryResult>)

  def withId(id: Long): QueryResult = copy(resultId = id)

  def withInstanceId(id: Long): QueryResult = copy(instanceId = id)

  def withSetSize(size: Long): QueryResult = copy(setSize = size)

  def withDescription(desc: String): QueryResult = copy(description = Option(desc))

  def withResultType(resType: String): QueryResult = copy(resultType = resType)
}

object QueryResult extends I2b2Umarshaller[QueryResult] with XmlUnmarshaller[QueryResult] {
  def fromXml(nodeSeq: NodeSeq) = {
    val desc = (nodeSeq \ "description").text
    val description = desc match {
      case "" => None
      case x => Some(x)
    }

    val sMessage = (nodeSeq \ "statusMessage").text
    val statusMessage = sMessage match {
      case "" => None
      case x => Some(x)
    }

    val sDate = (nodeSeq \ "startDate").text
    val startDate = sDate match {
      case "" => None
      case x => Some(makeXMLGregorianCalendar(x))
    }

    val eDate = (nodeSeq \ "endDate").text
    val endDate = eDate match {
      case "" => None
      case x => Some(makeXMLGregorianCalendar(x))
    }

    new QueryResult(
      (nodeSeq \ "resultId").text.toLong,
      (nodeSeq \ "instanceId").text.toLong,
      (nodeSeq \ "resultType").text,
      (nodeSeq \ "setSize").text.toLong,
      startDate,
      endDate,
      description,
      (nodeSeq \ "status").text,
      statusMessage)
  }

  def fromI2b2(nodeSeq: NodeSeq) = new QueryResult(
    (nodeSeq \ "result_instance_id").text.toLong,
    (nodeSeq \ "query_instance_id").text.toLong,
    (nodeSeq \ "query_result_type" \ "name").text,
    (nodeSeq \ "set_size").text.toLong,
    makeXMLGregorianCalendar((nodeSeq \ "start_date").text),
    makeXMLGregorianCalendar((nodeSeq \ "end_date").text),
    (nodeSeq \ "query_status_type" \ "name").text)

  def errorResult(description: String, statusMessage: String) = new QueryResult(
    0L, 0L, "", 0L, None, None, Some(description), "ERROR", Some(statusMessage))
}
