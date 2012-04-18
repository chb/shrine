package net.shrine.protocol

/**
 * @author Bill Simons
 * @date 4/13/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * 
 * NB: the 'self: T =>' bit means that this trait can only be mixed into ShrineResponse
 */
trait TranslatableResponse[T <: ShrineResponse] { self: T =>
  def asResponse: T = this
}