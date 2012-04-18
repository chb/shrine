package net.shrine.ont.messaging

import net.liftweb.json._

/**
 * @author Clint Gilbert
 * @date Feb 8, 2012
 */
abstract class LiftJsonUnmarshaller[T : Manifest] extends JsonUnmarshaller[T] {
  final override def fromJson(json: String): T = fromJson(parse(json))
  
  def fromJson(json: JValue): T = json.extract[T](DefaultFormats, manifest[T])
}