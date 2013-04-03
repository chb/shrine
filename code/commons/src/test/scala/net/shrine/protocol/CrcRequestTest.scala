package net.shrine.protocol

import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import org.junit.Test

/**
 * @author clint
 * @date Mar 29, 2013
 */
final class CrcRequestTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testFromI2b2ThrowsOnBadInput {
    intercept[Exception] {
      CrcRequest.fromI2b2("jksahdjkashdjkashdjkashdjksad")
    }
  }
}