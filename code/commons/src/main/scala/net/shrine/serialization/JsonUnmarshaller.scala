package net.shrine.serialization

import net.liftweb.json.JsonAST.JValue


/**
 * @author Bill Simons
 * @date 3/23/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
trait JsonUnmarshaller[T] {
  def fromJson(json: JValue): T

}