package net.shrine.ont.messaging

import org.scalatest.junit.ShouldMatchersForJUnit

/**
 * @author Clint Gilbert
 * @date Feb 8, 2012
 */
abstract class FromJsonTest[T <: JsonMarshaller](unmarshaller: JsonUnmarshaller[T]) extends ShouldMatchersForJUnit {
  protected def doTestFromJson(thing: T) {
    val json = thing.toJsonString
    
    val unmarshalled = unmarshaller.fromJson(json)
    
    unmarshalled should equal(thing)
  }
}