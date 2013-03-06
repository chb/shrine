package net.shrine.ont.messaging

/**
 * @author Clint Gilbert
 * @date Feb 8, 2012
 */
trait JsonUnmarshaller[T] {
  def fromJson(json: String): T
}