package net.shrine.aggregation

import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.ErrorResponse
import net.shrine.util.Loggable

/**
 *
 * @author Clint Gilbert
 * @date Sep 16, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * Represents the basic aggregation strategy shared by several aggregators:
 *   - Parses a sequence of SpinResultEntries into a sequence of some
 *   combination of valid responses, ErrorResponses, and invalid
 *   responses (cases where ShrineResponse.fromXml returns None)
 *   - Filters the valid responses, weeding out responses that aren't of
 *   the expected type
 *   Invokes an abstract method with the valid responses, errors, and
 *   invalid responses.
 * 
 * Needs to be an abstract class instead of a trait due to the view bound on T (: Manifest)
 */
abstract class BasicAggregator[T <: ShrineResponse : Manifest] extends Aggregator with Loggable {
  
  private[aggregation] def isAggregatable(response: ShrineResponse): Boolean = {
    manifest[T].erasure.isAssignableFrom(response.getClass)
  }
  
  import BasicAggregator._
  
  def aggregate(spinCacheResults: Seq[SpinResultEntry]) = {
    
    val resultsOrErrors: Seq[ParsedResult[T]] =
      for {
        result <- spinCacheResults
        unmarshalled = ShrineResponse.fromXml(result.spinResultXml)
      } yield unmarshalled match {
        case Some(errorResponse: ErrorResponse) => Error(result, errorResponse)
        case Some(unmarshalled: T) if isAggregatable(unmarshalled) => Valid(result, unmarshalled)
        case _ => Invalid(result, "Unexpected response in" + this.getClass.toString + ":\r\n" + result.spinResultXml)
      }

    val invalidResponses = resultsOrErrors.collect { case invalid: Invalid => invalid }

    val validResponses = resultsOrErrors.collect { case valid: Valid[T] => valid }
    
    val errorResponses = resultsOrErrors.collect { case error: Error => error }
    
    //Log all parsing errors
    invalidResponses.map(_.errorMessage).foreach(this.error(_))
    
    makeResponseFrom(validResponses, errorResponses, invalidResponses)
  }
  
  private[aggregation] def makeResponseFrom(validResponses: Seq[Valid[T]], errorResponses: Seq[Error], invalidResponses: Seq[Invalid]): ShrineResponse
}

object BasicAggregator {
  private[aggregation] sealed abstract class ParsedResult[+T]
  
  private[aggregation] final case class Valid[T](spinResult: SpinResultEntry, response: T) extends ParsedResult[T]
  private[aggregation] final case class Error(spinResult: SpinResultEntry, esponse: ErrorResponse) extends ParsedResult[Nothing]
  private[aggregation] final case class Invalid(spinResult: SpinResultEntry, errorMessage: String) extends ParsedResult[Nothing]
}