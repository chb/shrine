package net.shrine.aggregation

import net.shrine.protocol._
import collection.mutable.ArrayBuffer
import org.spin.message.Result

/**
 * @author ???
 * @date ???
 */
class ReadPdoResponseAggregator(val useBinary: Boolean) extends Aggregator {

  override def aggregate(spinCacheResults: Seq[SpinResultEntry], errors: Seq[ErrorResponse]) = {
    val eventBuffer = new ArrayBuffer[EventResponse]
    val patientBuffer = new ArrayBuffer[PatientResponse]
    val observationBuffer = new ArrayBuffer[ObservationResponse]

    spinCacheResults.foreach {
      result =>

        val responseObj = useBinary match {
          case true => ReadPdoResponse.fromBinary(result.spinResultXml)
          case _ => ReadPdoResponse.fromXml(result.spinResultXml)
        }


        val response = transform(responseObj, result.spinResultMetadata) //TODO handle errors

        eventBuffer ++= response.events
        patientBuffer ++= response.patients
        observationBuffer ++= response.observations
    }

    //TODO: What to do with passed-in errors?

    new ReadPdoResponse(eventBuffer, patientBuffer, observationBuffer);
  }

  /**
   * This method is a no-op transformation but subclasses can override it
   * and do something more interesting.
   */
  protected def transform(entry: ReadPdoResponse, metadata: Result): ReadPdoResponse = entry
}