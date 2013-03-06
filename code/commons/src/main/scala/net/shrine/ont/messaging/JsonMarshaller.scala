package net.shrine.ont.messaging

/**
 * @author Clint Gilbert
 * @date Feb 8, 2012
 */
trait JsonMarshaller {
  def toJsonString: String 
}