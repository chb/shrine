package net.shrine.aggregation

import org.spin.query.message.headers.Result
import net.shrine.protocol._

import collection.mutable.ArrayBuffer

class ReadPdoResponseAggregator extends Aggregator {

  def aggregate(spinCacheResults: Seq[SpinResultEntry]) = {
    val eventBuffer = new ArrayBuffer[EventResponse]
    val patientBuffer = new ArrayBuffer[PatientResponse]
    val observationBuffer = new ArrayBuffer[ObservationResponse]
    spinCacheResults.foreach { result =>
        val response = transform(ReadPdoResponse.fromXml(result.spinResultXml), result.spinResultMetadata) //TODO handle errors
        eventBuffer ++= response.events
        patientBuffer ++= response.patients
        observationBuffer ++= response.observations
    }

    val pdoResponse = new ReadPdoResponse(eventBuffer, patientBuffer, observationBuffer);

    pdoResponse
  }


  /**
   * This method is a no-op transformation but subclasses can override it
   * and do something more interesting.
   */
  protected def transform(entry: ReadPdoResponse, metadata: Result): ReadPdoResponse = {
    entry
  }

}