package net.shrine.dao.slick.workarounds

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import scala.slick.driver.MySQLDriver
import scala.slick.driver.H2Driver

/**
 * @author clint
 * @date Jan 25, 2013
 */
final class SpringWorkaroundsTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testDriver {
    (new SpringWorkarounds("mysql").driver eq MySQLDriver) should be(true)
    
    (new SpringWorkarounds("h2").driver eq H2Driver) should be(true)
    
    intercept[RuntimeException] {
      new SpringWorkarounds(null).driver
    }
    
    intercept[RuntimeException] {
      new SpringWorkarounds("").driver
    }
    
    intercept[RuntimeException] {
      new SpringWorkarounds("MySQL").driver
    }
    
    intercept[RuntimeException] {
      new SpringWorkarounds("H2").driver
    }
    
    intercept[RuntimeException] {
      new SpringWorkarounds("foo").driver
    }
  }
}