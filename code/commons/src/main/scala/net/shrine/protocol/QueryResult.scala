package net.shrine.protocol

import javax.xml.datatype.XMLGregorianCalendar
import xml.NodeSeq
import org.spin.tools.NetworkTime.makeXMLGregorianCalendar
import net.shrine.util.XmlUtil
import net.shrine.serialization.{ I2b2Marshaller, I2b2Unmarshaller, XmlMarshaller, XmlUnmarshaller }

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
  val resultType: Option[ResultOutputType], //this won't be present in the case of an error result
  val setSize: Long,
  val startDate: Option[XMLGregorianCalendar],
  val endDate: Option[XMLGregorianCalendar],
  val description: Option[String],
  val statusType: String,
  val statusMessage: Option[String],
  val breakdowns: Map[ResultOutputType, I2b2ResultEnvelope] = Map.empty) extends XmlMarshaller with I2b2Marshaller {

  def this(resultId: Long, instanceId: Long, resultType: ResultOutputType, setSize: Long, startDate: XMLGregorianCalendar, endDate: XMLGregorianCalendar, statusType: String) = {
    this(resultId, instanceId, Option(resultType), setSize, Option(startDate), Option(endDate), None, statusType, None)
  }

  def this(resultId: Long, instanceId: Long, resultType: ResultOutputType, setSize: Long, startDate: XMLGregorianCalendar, endDate: XMLGregorianCalendar, description: String, statusType: String) = {
    this(resultId, instanceId, Option(resultType), setSize, Option(startDate), Option(endDate), Option(description), statusType, None)
  }

  def resultTypeIs(testedResultType: ResultOutputType): Boolean = resultType match {
    case Some(rt) => rt == testedResultType
    case _ => false
  }

  import QueryResult._

  def isError = statusType == StatusType.Error

  def toI2b2 = {
    import ResultOutputType._

    XmlUtil.stripWhitespace(
      <query_result_instance>
        <result_instance_id>{ resultId }</result_instance_id>
        <query_instance_id>{ instanceId }</query_instance_id>
        {
          description.map(x => <description>{ x }</description>).orNull
        }
        <query_result_type>
          <name>{ resultType.filter(_ != ERROR).map(_.name).orNull }</name>
          {
            resultType match {
              case Some(PATIENTSET) => <result_type_id>1</result_type_id><display_type>LIST</display_type><visual_attribute_type>LA</visual_attribute_type><description>Patient list</description>
              case Some(PATIENT_COUNT_XML) => <result_type_id>4</result_type_id><display_type>CATNUM</display_type><visual_attribute_type>LA</visual_attribute_type><description>Number of patients</description>
              case _ => null
            }
          }
        </query_result_type>
        <set_size>{ setSize }</set_size>
        {
          startDate.map(x => <start_date>{ x }</start_date>).orNull
        }
        {
          endDate.map(x => <end_date>{ x }</end_date>).orNull
        }
        <query_status_type>
          <name>{ statusType }</name>
          {
            import StatusType._
            
            statusType match {
              case Finished => <status_type_id>3</status_type_id><description>{ Finished }</description>
              //TODO: Can we use the same <status_type_id> for both Queued and Processing?
              case Processing | Queued => <status_type_id>2</status_type_id><description>{ Processing }</description>
              case Error => statusMessage.map(x => <description>{ x }</description>).orNull
            }
          }
        </query_status_type>
      </query_result_instance>)
  }

  def toXml = XmlUtil.stripWhitespace(
    <queryResult>
      <resultId>{ resultId }</resultId>
      <instanceId>{ instanceId }</instanceId>
      <resultType>{ resultType.map(_.name).orNull }</resultType>
      <setSize>{ setSize }</setSize>
      {
        startDate.map(x => <startDate>{ x }</startDate>).orNull
      }
      {
        endDate.map(x => <endDate>{ x }</endDate>).orNull
      }
      {
        description.map(x => <description>{ x }</description>).orNull
      }
      <status>{ statusType }</status>
      {
        statusMessage.map(x => <statusMessage>{ x }</statusMessage>).orNull
      }
      {
        //Sorting isn't strictly necessary, but makes deterministic unit testing easier.  
        //The number of breakdowns will be at most 4, so performance should not be an issue. 
        breakdowns.values.toSeq.sortBy(_.resultType).map(_.toXml)
      }
    </queryResult>)

  def withId(id: Long): QueryResult = copy(resultId = id)

  def withInstanceId(id: Long): QueryResult = copy(instanceId = id)

  def withSetSize(size: Long): QueryResult = copy(setSize = size)

  def withDescription(desc: String): QueryResult = copy(description = Option(desc))

  def withResultType(resType: ResultOutputType): QueryResult = copy(resultType = Option(resType))

  def withBreakdown(breakdownData: I2b2ResultEnvelope) = copy(breakdowns = breakdowns + (breakdownData.resultType -> breakdownData))
  
  def withBreakdowns(newBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope]) = copy(breakdowns = newBreakdowns) 
}

object QueryResult extends I2b2Unmarshaller[QueryResult] with XmlUnmarshaller[QueryResult] {
  object StatusType {
    val Error = "ERROR"
    val Finished = "FINISHED"
    val Processing = "PROCESSING"
    val Queued = "QUEUED"
  }

  def extractLong(nodeSeq: NodeSeq)(elemName: String): Long = (nodeSeq \ elemName).text.toLong

  def fromXml(nodeSeq: NodeSeq) = {
    def extract(elemName: String): Option[String] = {
      Option((nodeSeq \ elemName).text).filter(!_.trim.isEmpty)
    }

    def extractDate(elemName: String): Option[XMLGregorianCalendar] = extract(elemName).map(makeXMLGregorianCalendar)

    def asLong = extractLong(nodeSeq) _

    def extractBreakdowns(elemName: String): Map[ResultOutputType, I2b2ResultEnvelope] = {
      val envelopes = (nodeSeq \ elemName).flatMap(I2b2ResultEnvelope.fromXml)

      Map.empty ++ envelopes.map(envelope => (envelope.resultType -> envelope))
    }

    new QueryResult(
      asLong("resultId"),
      asLong("instanceId"),
      tryOrNone(ResultOutputType.valueOf((nodeSeq \ "resultType").text)),
      asLong("setSize"),
      extractDate("startDate"),
      extractDate("endDate"),
      extract("description"),
      (nodeSeq \ "status").text,
      extract("statusMessage"),
      extractBreakdowns("resultEnvelope"))
  }

  private def tryOrNone[T](f: => T): Option[T] = {
    try {
      Option(f)
    } catch {
      case e: Exception => None
    }
  }

  def fromI2b2(nodeSeq: NodeSeq) = {
    def asLong = extractLong(nodeSeq) _

    def asText(elemNames: String*) = (elemNames.foldLeft(nodeSeq)(_ \ _)).text

    def asXmlGc(elemName: String) = Option(asText(elemName)).filter(!_.isEmpty).map(makeXMLGregorianCalendar)

    def asResultOutputType(elemNames: String*) = {
      try {
        ResultOutputType.valueOf(asText(elemNames: _*))
      } catch {
        case e: Exception => null
      }
    }

    new QueryResult(
      asLong("result_instance_id"),
      asLong("query_instance_id"),
      tryOrNone(asResultOutputType("query_result_type", "name")),
      asLong("set_size"),
      asXmlGc("start_date"),
      asXmlGc("end_date"),
      None, //no description
      asText("query_status_type", "name"),
      //no status message
      None)
  }

  def errorResult(description: Option[String], statusMessage: String) = {
    QueryResult(0L, 0L, None, 0L, None, None, description, "ERROR", Option(statusMessage))
  }
}
