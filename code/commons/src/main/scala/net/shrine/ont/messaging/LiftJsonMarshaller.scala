package net.shrine.ont.messaging

import net.liftweb.json.JsonAST.JValue
import net.liftweb.json._

/**
 * @author Clint Gilbert
 * @date Feb 8, 2012
 */
trait LiftJsonMarshaller extends JsonMarshaller {
  final override def toJsonString: String = compact(render(toJValue))
    
  def toJValue: JValue
}