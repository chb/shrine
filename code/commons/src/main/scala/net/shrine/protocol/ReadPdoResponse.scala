package net.shrine.protocol

import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.{I2b2Unmarshaller, XmlUnmarshaller}

/**
 * @author Bill Simons (??)
 * @date ??
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * NB: a case class for structural equals() and hashCode()
 */
final case class ReadPdoResponse(
    val events: Seq[EventResponse],
    val patients: Seq[PatientResponse],
    val observations: Seq[ObservationResponse]) extends ShrineResponse {

  def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns3:response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns3:patient_data_responseType">
      <ns2:patient_data>
        <ns2:event_set>
          {events map {
          x => x.i2b2MessageBody
        }}
        </ns2:event_set>
        <ns2:patient_set>
          {patients map {
          x => x.i2b2MessageBody
        }}
        </ns2:patient_set>
        <ns2:observation_set>
          {observations map {
          x => x.toI2b2
        }}
        </ns2:observation_set>
      </ns2:patient_data>
    </ns3:response>)

  def toXml = XmlUtil.stripWhitespace(
    <PdoResponse>
      <events>
        {events map { x =>
        x.toXml
      }}
      </events>
      <patients>
        {patients map { x =>
        x.toXml
      }}
      </patients>
      <observations>
        {observations map { x =>
        x.toXml
      }}
      </observations>
    </PdoResponse>)
}


object ReadPdoResponse extends I2b2Unmarshaller[ReadPdoResponse] with XmlUnmarshaller[ReadPdoResponse] {
  def fromI2b2(nodeSeq: NodeSeq) = {
    val events = (nodeSeq \ "message_body" \ "response" \ "patient_data" \ "event_set" \ "event") map  { x =>
      EventResponse.fromI2b2(x)
    }
    val patients = (nodeSeq \ "message_body" \ "response" \ "patient_data" \ "patient_set" \ "patient") map { x =>
      PatientResponse.fromI2b2(x)
    }
    val observations = (nodeSeq \ "message_body" \ "response" \ "patient_data" \ "observation_set" \ "observation") map  { x =>
      ObservationResponse.fromI2b2(x)
    }
    new ReadPdoResponse(events, patients, observations)
  }

  def fromXml(nodeSeq: NodeSeq) = {
    val events = (nodeSeq \ "events" \ "event") map { x =>
      EventResponse.fromXml(x)
    }
    val patients = (nodeSeq \ "patients" \ "patient") map { x =>
      PatientResponse.fromXml(x)
    }
    val observations = (nodeSeq \ "observations" \ "observation") map  { x =>
      ObservationResponse.fromXml(x)
    }
    new ReadPdoResponse(events, patients, observations)
  }
}
