package net.shrine.spring

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

/**
 * @author clint
 * @date Mar 4, 2013
 */
final class ToNullShimTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testOrNull {
    val o: Option[String] = None

    ToNullShim.orNull(o) should be(null)

    val s = "ksalhdklasjd"

    ToNullShim.orNull(Some(s)) should be(s)
  }
}