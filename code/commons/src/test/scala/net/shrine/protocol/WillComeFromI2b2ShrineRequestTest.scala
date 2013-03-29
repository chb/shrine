package net.shrine.protocol

import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import org.junit.Test

/**
 * @author clint
 * @date Mar 29, 2013
 */
final class WillComeFromI2b2ShrineRequestTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testFromI2b2ThrowsOnBadInput {
    intercept[Exception] {
      WillComeFromI2b2ShrineRequest.fromI2b2("jksahdjkashdjkashdjkashdjksad")
    }
  }
}