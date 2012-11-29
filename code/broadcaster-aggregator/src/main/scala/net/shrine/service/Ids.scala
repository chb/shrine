package net.shrine.service

/**
 * @author clint
 * @date Nov 29, 2012
 */
object Ids {
  private val random = new java.util.Random
  
  def nextLong = random.nextLong.abs
}