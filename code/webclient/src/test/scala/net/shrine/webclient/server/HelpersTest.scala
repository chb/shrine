package net.shrine.webclient.server

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test

/**
 * @author clint
 * @date May 22, 2012
 */
final class HelpersTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  @Test
  def testIterableToJava {
    val stuff = Seq(1, 2, 3, 42, 5, 6, 7, 9)

    val size = stuff.size

    val javaThings = Helpers.toJavaList(stuff)

    import scala.collection.JavaConverters._

    javaThings.asScala should equal(stuff)

    javaThings.clear

    javaThings.isEmpty should be(true)

    stuff.size should be(size)
  }
  
  @Test
  def testMapTojava {
    val stuff = Map(99 -> "X", 42 -> "Y", 123 -> "Z")
    
    val size = stuff.size

    val javaThings = Helpers.toJavaMap(stuff)

    import scala.collection.JavaConverters._

    javaThings.asScala should equal(stuff)

    javaThings.clear

    javaThings.isEmpty should be(true)

    stuff.size should be(size)
  }
}