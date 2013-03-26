package net.shrine.utilities.scanner.commands

import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import org.junit.Test

/**
 * @author clint
 * @date Mar 25, 2013
 */
final class CompoundCommandTest extends TestCase with ShouldMatchersForJUnit {

  @Test
  def testApply {
    val longToInt: MockCommand[Long, Int] = MockCommand(_.toInt)

    val intToString: MockCommand[Int, String] = MockCommand(_.toString)

    val longToIntToString: Long >>> String = CompoundCommand(longToInt, intToString)

    val s = longToIntToString(123L)

    s should equal("123")

    longToInt.invoked should be(true)

    intToString.invoked should be(true)
  }
}