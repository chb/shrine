package net.shrine.utilities.scanner

import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import org.junit.Test

/**
 * @author clint
 * @date Mar 7, 2013
 */
final class ReScanResultsTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testEmpty {
    ReScanResults.empty.shouldHaveBeenMapped.isEmpty should be(true)
    ReScanResults.empty.stillNotFinished.isEmpty should be(true)
  }
}