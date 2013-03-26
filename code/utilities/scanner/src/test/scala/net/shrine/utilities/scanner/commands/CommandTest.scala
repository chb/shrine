package net.shrine.utilities.scanner.commands

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

/**
 * @author clint
 * @date Mar 25, 2013
 */
final class CommandTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testAndThen {
    val f: Int >>> String = MockCommand(_.toString)
    val g: String >>> Unit = MockCommand(println)
    
    val CompoundCommand(actualF, actualG) = f andThen g
    
    actualF should be(f)
    actualG should be(g)
  }
}