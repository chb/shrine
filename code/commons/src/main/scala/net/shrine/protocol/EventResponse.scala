package net.shrine.protocol

import xml.{NodeSeq, Utility}
import java.util.{Calendar, Date}
import javax.xml.bind.DatatypeConverter
import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime._
import net.shrine.util.XmlUtil

/**
 * @author Justin Quan
 * @date 10/27/11
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final case class EventResponse(
    val eventId: String,
    val patientId: String,
    val startDate: Option[XMLGregorianCalendar],
    val endDate: Option[XMLGregorianCalendar],
    val params: Seq[ParamResponse]) extends ShrineResponse {

  def i2b2MessageBody = XmlUtil.stripWhitespace(<event>
    <event_id>
      {eventId}
    </event_id>
    <patient_id>
      {patientId}
    </patient_id>
    {params map {
      x =>
        x.i2b2MessageBody
    }}
    { startDate.map(x => <start_date>{x}</start_date>).orNull }
    { endDate.map(x => <end_date>{x}</end_date>).orNull }
  </event>)

  def toXml = XmlUtil.stripWhitespace(<event>
    <event_id>
      {eventId}
    </event_id>
    <patient_id>
      {patientId}
    </patient_id>
    {params map {
      x =>
        x.toXml
    }}
    { startDate.map(x => <start_date>{x}</start_date>).orNull }
    { endDate.map(x => <end_date>{x}</end_date>).orNull }
  </event>)
}

object EventResponse extends I2b2Umarshaller[EventResponse] with XmlUnmarshaller[EventResponse] {
  def fromXml(nodeSeq: NodeSeq) = {
    val sDate = (nodeSeq \ "start_date").text
    val startDate = sDate match {
      case "" => None
      case x => Some(makeXMLGregorianCalendar(x))
    }

    val eDate = (nodeSeq \ "end_date").text
    val endDate = eDate match {
      case "" => None
      case x => Some(makeXMLGregorianCalendar(x))
    }

    new EventResponse(
      (nodeSeq \ "event_id").text,
      (nodeSeq \ "patient_id").text,
      startDate,
      endDate,
      (nodeSeq \ "param") map {
        x =>
          new ParamResponse(
            (x \ "@name").text,
            (x \ "@column").text,
            x.text)
      })
  }

  def fromI2b2(nodeSeq: NodeSeq) = {

    val sDate = (nodeSeq \ "start_date").text
    val startDate = sDate match {
      case "" => None
      case x => Some(makeXMLGregorianCalendar(x))
    }

    val eDate = (nodeSeq \ "end_date").text
    val endDate = eDate match {
      case "" => None
      case x => Some(makeXMLGregorianCalendar(x))
    }

    new EventResponse(
      (nodeSeq \ "event_id").text,
      (nodeSeq \ "patient_id").text,
      startDate,
      endDate,
      (nodeSeq \ "param") map {
        x =>
          new ParamResponse(
            (x \ "@name").text,
            (x \ "@column").text,
            x .text)
      })
  }
}