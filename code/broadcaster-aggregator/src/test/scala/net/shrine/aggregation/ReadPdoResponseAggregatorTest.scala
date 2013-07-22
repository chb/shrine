package net.shrine.aggregation

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import org.junit.Assert.assertTrue
import net.shrine.protocol._
import collection.mutable.ListBuffer
import org.spin.tools.NetworkTime._

class ReadPdoResponseAggregatorTest extends AssertionsForJUnit with ShouldMatchersForJUnit {
  val aggregator = new ReadPdoResponseAggregator()

  def createPdoResponse(): ReadPdoResponse = {
    val patientId1 = "1000000001";
    val patientId2 = "1000000002";
    val patient1Param1 = new ParamResponse("vital_status_cd", "vital_status_cd", "N")
    val patient1Param2 = new ParamResponse("birth_date", "birth_date", "1985-11-17T00:00:00.000-05:00")
    val patient2Param1 = new ParamResponse("vital_status_cd", "vital_status_cd", "N")
    val patient2Param2 = new ParamResponse("birth_date", "birth_date", "1966-08-29T00:00:00.000-04:00")
    val patient1 = new PatientResponse(patientId1, List(patient1Param1, patient1Param2))
    val patient2 = new PatientResponse(patientId2, List(patient2Param1, patient2Param2))
    val event1 = new EventResponse(789012.toString, patientId1,
      Some(makeXMLGregorianCalendar("2011-01-29T00:00:00.000-05:00")),
      Some(makeXMLGregorianCalendar("2011-01-29T00:00:00.000-05:00")),
      List(patient1Param1, patient1Param2))
    val event2 = new EventResponse(123456.toString, patientId2, None, None,
      List(patient1Param1, patient1Param2))
    val observationEvent1 = <event_id>2005000001</event_id>
    val observationEvent2 = <event_id>2005000002</event_id>
    val observation1 = new ObservationResponse(None, "eventId", None, "patientId", None, None, None, "observerCode", "startDate", None, "valueTypeCode",None,None,None,None,None,None,None, Seq(new ParamResponse("someParam1", "someColumn1", "someValue1")))
    val observation2 = new ObservationResponse(None, "eventId", None, "patientId", None, None, None, "observerCode", "startDate", None, "valueTypeCode",None,None,None,None,None,None,None, Seq(new ParamResponse("someParam2", "someColumn2", "someValue2")))
    new ReadPdoResponse(List(event1, event2), List(patient1, patient2), List(observation1, observation2))
  }

  @Test
  def testAggregate() {
    val result1 = new SpinResultEntry(createPdoResponse().toXml.toString, null)
    val result2 = new SpinResultEntry(createPdoResponse().toXml.toString, null)
    val result3 = new SpinResultEntry(createPdoResponse().toXml.toString, null)
    val userId = "userId"
    val authn = new AuthenticationInfo("domain", userId, new Credential("value", false))
    var actual = aggregator.aggregate(Vector(result1, result2, result3))
    assertTrue(actual.isInstanceOf[ReadPdoResponse])
    actual = actual.asInstanceOf[ReadPdoResponse]

    actual.patients.size should equal(6)

    val paramList = new ListBuffer[ParamResponse]

    for (
      p <- actual.patients) {
      p.params.size should equal(2)
      paramList ++= p.params
    }

    paramList.filter {
      x => x.name == "vital_status_cd"
    }.size should equal(6)

    paramList.filter {
      x => x.name == "birth_date"
    }.size should equal(6)

    actual.patients.map{x => x.patientId}.size should equal(6)

    actual.observations.size should equal(6)

    actual.events.size should equal(6)
  }
}