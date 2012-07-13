package net.shrine.aggregation
import net.shrine.protocol.ShrineResponse
import net.shrine.aggregation.BasicAggregator.{Invalid, Error, Valid}
import net.shrine.protocol.ErrorResponse

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
 * Extends BasicAggregator to ignore Errors and Invalid responses
 * 
 * Needs to be an abstract class instead of a trait due to the view bound on T (: Manifest)
 */
abstract class IgnoresErrorsAggregator[T <: ShrineResponse : Manifest] extends BasicAggregator[T] {
  private[aggregation] override def makeResponseFrom(validResponses: Seq[Valid[T]], errorResponses: Seq[Error], invalidResponses: Seq[Invalid]): ShrineResponse = {
    //Filter out errors and invalid responses
    makeResponseFrom(validResponses)
  }

  //Default implementation, just returns first valid response, or if there are none, an ErrorResponse
  private[aggregation] def makeResponseFrom(validResponses: Seq[Valid[T]]): ShrineResponse = {
    validResponses.map(_.response).distinct.headOption.getOrElse(new ErrorResponse("No valid responses to aggregate"))
  }
}