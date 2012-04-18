package net.shrine.ont.messaging

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/**
 * @author Clint Gilbert
 * @date Feb 8, 2012
 */
final case class Blarg(x: Int, y: Double) extends LiftJsonMarshaller {
  override def toJValue: JValue = ("x" -> x) ~ ("y" -> y)
}
  
final case class Foo(bar: Int, baz: String, blargs: Seq[Blarg]) extends LiftJsonMarshaller {
  override def toJValue: JValue = {
    def blargsArray: JValue = JArray(blargs.map(_.toJValue).toList)
    
    ("bar" -> bar) ~ ("baz" -> baz) ~ ("blargs" -> blargsArray)
  }
}