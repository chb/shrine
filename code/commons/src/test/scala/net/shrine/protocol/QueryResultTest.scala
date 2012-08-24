package net.shrine.protocol

import org.scalatest.junit.{ ShouldMatchersForJUnit, AssertionsForJUnit }
import org.junit.Test
import org.spin.tools.NetworkTime
import java.util.Calendar
import xml.Utility
import net.shrine.util.XmlUtil
import junit.framework.TestCase
import java.util.GregorianCalendar
import net.shrine.protocol.I2b2ResultEnvelope.Column

/**
 * @author Bill Simons
 * @author clint
 * @date 8/19/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class QueryResultTest extends TestCase with XmlRoundTripper[QueryResult] with AssertionsForJUnit with ShouldMatchersForJUnit with I2b2SerializableValidator {
  private val date = NetworkTime.makeXMLGregorianCalendar(new GregorianCalendar)
  private val resultId = 1L
  private val instanceId = 2L
  private val resultType = ResultOutputType.PATIENTSET
  private val setSize = 12L
  private val statusType = "FINISHED"
  private val description = "description"
  private val statusMessage = "lakjdalsjd"
  private val queryResult = new QueryResult(resultId, instanceId, resultType, setSize, Option(date), Option(date), Option(description), statusType, Option(statusMessage))

  private def intColumn(name: String, value: Int) = Column("int", name, value)

  import ResultOutputType._

  private val resultWithBreakDowns = queryResult.copy(breakdowns =
    Map(PATIENT_AGE_COUNT_XML -> I2b2ResultEnvelope(PATIENT_AGE_COUNT_XML, Seq(intColumn("foo", 1), intColumn("bar", 2))),
      PATIENT_RACE_COUNT_XML -> I2b2ResultEnvelope(PATIENT_RACE_COUNT_XML, Seq(intColumn("nuh", 3), intColumn("zuh", 4))),
      PATIENT_VITALSTATUS_COUNT_XML -> I2b2ResultEnvelope(PATIENT_VITALSTATUS_COUNT_XML, Seq(intColumn("blarg", 5), intColumn("glarg", 6))),
      PATIENT_GENDER_COUNT_XML -> I2b2ResultEnvelope(PATIENT_GENDER_COUNT_XML, Seq(intColumn("huh", 7), intColumn("yeah", 8)))))

  private val expectedWhenBreakdownsArePresent = XmlUtil.stripWhitespace(
    <queryResult>
      <resultId>{ resultId }</resultId>
      <instanceId>{ instanceId }</instanceId>
      <resultType>{ resultType.name }</resultType>
      <setSize>{ setSize }</setSize>
      <startDate>{ date }</startDate>
      <endDate>{ date }</endDate>
      <description>{ description }</description>
      <status>{ statusType }</status>
      <statusMessage>{ statusMessage }</statusMessage>
      <resultEnvelope>
        <resultType>{ PATIENT_AGE_COUNT_XML }</resultType>
        <column>
          <type>int</type>
          <name>foo</name>
          <value>1</value>
        </column>
        <column>
          <type>int</type>
          <name>bar</name>
          <value>2</value>
        </column>
      </resultEnvelope>
      <resultEnvelope>
        <resultType>{ PATIENT_RACE_COUNT_XML }</resultType>
        <column>
          <type>int</type>
          <name>nuh</name>
          <value>3</value>
        </column>
        <column>
          <type>int</type>
          <name>zuh</name>
          <value>4</value>
        </column>
      </resultEnvelope>
      <resultEnvelope>
        <resultType>{ PATIENT_VITALSTATUS_COUNT_XML }</resultType>
        <column>
          <type>int</type>
          <name>blarg</name>
          <value>5</value>
        </column>
        <column>
          <type>int</type>
          <name>glarg</name>
          <value>6</value>
        </column>
      </resultEnvelope>
      <resultEnvelope>
        <resultType>{ PATIENT_GENDER_COUNT_XML }</resultType>
        <column>
          <type>int</type>
          <name>huh</name>
          <value>7</value>
        </column>
        <column>
          <type>int</type>
          <name>yeah</name>
          <value>8</value>
        </column>
      </resultEnvelope>
    </queryResult>).toString

  private val expectedI2b2Xml = XmlUtil.stripWhitespace(
    <query_result_instance>
      <result_instance_id>{ resultId }</result_instance_id>
      <query_instance_id>{ instanceId }</query_instance_id>
      <description>{ description }</description>
      <query_result_type>
        <name>{ resultType }</name>
        <result_type_id>1</result_type_id><display_type>LIST</display_type><visual_attribute_type>LA</visual_attribute_type><description>Patient list</description>
      </query_result_type>
      <set_size>{ setSize }</set_size>
      <start_date>{ date }</start_date>
      <end_date>{ date }</end_date>
      <query_status_type>
        <name>{ statusType }</name>
        <status_type_id>3</status_type_id><description>FINISHED</description>
      </query_status_type>
    </query_result_instance>).toString

  private val expectedI2b2ErrorXml = XmlUtil.stripWhitespace(
    <query_result_instance>
      <result_instance_id>0</result_instance_id>
      <query_instance_id>0</query_instance_id>
      <description>{ description }</description>
      <query_result_type>
        <name></name>
      </query_result_type>
      <set_size>0</set_size>
      <query_status_type>
        <name>ERROR</name>
        <description>{ statusMessage }</description>
      </query_status_type>
    </query_result_instance>).toString

  @Test
  def testToXml {
    val expectedWhenNoBreakdowns = XmlUtil.stripWhitespace(
      <queryResult>
        <resultId>{ resultId }</resultId>
        <instanceId>{ instanceId }</instanceId>
        <resultType>{ resultType.name }</resultType>
        <setSize>{ setSize }</setSize>
        <startDate>{ date }</startDate>
        <endDate>{ date }</endDate>
        <description>{ description }</description>
        <status>{ statusType }</status>
        <statusMessage>{ statusMessage }</statusMessage>
      </queryResult>).toString

    queryResult.toXmlString should equal(expectedWhenNoBreakdowns)

    val expectedWhenNoStartDate = XmlUtil.stripWhitespace(
      <queryResult>
        <resultId>{ resultId }</resultId>
        <instanceId>{ instanceId }</instanceId>
        <resultType>{ resultType.name }</resultType>
        <setSize>{ setSize }</setSize>
        <endDate>{ date }</endDate>
        <description>{ description }</description>
        <status>{ statusType }</status>
        <statusMessage>{ statusMessage }</statusMessage>
      </queryResult>).toString

    queryResult.copy(startDate = None).toXmlString should equal(expectedWhenNoStartDate)

    val expectedWhenNoEndDate = XmlUtil.stripWhitespace(
      <queryResult>
        <resultId>{ resultId }</resultId>
        <instanceId>{ instanceId }</instanceId>
        <resultType>{ resultType.name }</resultType>
        <setSize>{ setSize }</setSize>
        <startDate>{ date }</startDate>
        <description>{ description }</description>
        <status>{ statusType }</status>
        <statusMessage>{ statusMessage }</statusMessage>
      </queryResult>).toString

    queryResult.copy(endDate = None).toXmlString should equal(expectedWhenNoEndDate)

    val expectedWhenNoDescription = XmlUtil.stripWhitespace(
      <queryResult>
        <resultId>{ resultId }</resultId>
        <instanceId>{ instanceId }</instanceId>
        <resultType>{ resultType.name }</resultType>
        <setSize>{ setSize }</setSize>
        <startDate>{ date }</startDate>
        <endDate>{ date }</endDate>
        <status>{ statusType }</status>
        <statusMessage>{ statusMessage }</statusMessage>
      </queryResult>).toString

    queryResult.copy(description = None).toXmlString should equal(expectedWhenNoDescription)

    val expectedWhenNoStatusMessage = XmlUtil.stripWhitespace(
      <queryResult>
        <resultId>{ resultId }</resultId>
        <instanceId>{ instanceId }</instanceId>
        <resultType>{ resultType.name }</resultType>
        <setSize>{ setSize }</setSize>
        <startDate>{ date }</startDate>
        <endDate>{ date }</endDate>
        <description>{ description }</description>
        <status>{ statusType }</status>
      </queryResult>).toString

    queryResult.copy(statusMessage = None).toXmlString should equal(expectedWhenNoStatusMessage)

    resultWithBreakDowns.toXmlString should equal(expectedWhenBreakdownsArePresent)
  }

  @Test
  def testFromXml {
    QueryResult.fromXml(expectedWhenBreakdownsArePresent) should equal(resultWithBreakDowns)
  }

  @Test
  def testShrineRoundTrip = doShrineXmlRoundTrip(resultWithBreakDowns, QueryResult)

  private def compareIgnoringBreakdownsDescriptionAndStatusMessage(actual: QueryResult, expected: QueryResult) {
    //Ignore breakdowns field, since this can't be serialized to i2b2 format as part of a <query_result_instance>
    actual.breakdowns should equal(Map.empty)
    actual.description should equal(None) //this field is ignored when unmarshalling
    actual.endDate should equal(expected.endDate)
    actual.instanceId should equal(expected.instanceId)
    actual.resultId should equal(expected.resultId)
    actual.resultType should equal(expected.resultType)
    actual.setSize should equal(expected.setSize)
    actual.startDate should equal(expected.startDate)
    actual.statusMessage should equal(None) //this field is ignored when unmarshalling
    actual.statusType should equal(expected.statusType)
  }
  
  @Test
  def testI2b2RoundTrip = doI2b2XmlRoundTrip(resultWithBreakDowns, QueryResult, compareIgnoringBreakdownsDescriptionAndStatusMessage)

  @Test
  def testFromI2b2 {
    compareIgnoringBreakdownsDescriptionAndStatusMessage(QueryResult.fromI2b2(expectedI2b2Xml), queryResult)
  }

  @Test
  def testFromI2b2WithErrors {
    val errorResult = QueryResult.errorResult(Some(description), statusMessage)
    
    compareIgnoringBreakdownsDescriptionAndStatusMessage(QueryResult.fromI2b2(expectedI2b2ErrorXml), errorResult)
  }

  @Test
  def testToI2b2 {
    queryResult.toI2b2String should equal(expectedI2b2Xml)
  }

  @Test
  def testToI2b2WithErrors {
    val actual = QueryResult.errorResult(Some(description), statusMessage).toI2b2String

    actual should equal(expectedI2b2ErrorXml)
  }
}