package net.shrine.util

import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import org.junit.Test

/**
 * @author clint
 * @date Oct 30, 2012
 */
final class UtilTest extends TestCase with ShouldMatchersForJUnit{
  @Test
  def testQmarkQmarkQmark {
    intercept[RuntimeException] {
      Util.???
    }
  }
  
  @Test
  def testNow {
    val now = Util.now
    
    now should not be(null)
    
    //TODO: Is there anything more we can do?  
    //Off-by-one and build-server timing issues abound for all the approaches I can think of. :( -Clint 
  }
  
  @Test
  def testTryOrElse {
    import Util.tryOrElse 
    
    tryOrElse(false)(true) should be(true)
    tryOrElse(false)(false) should be(false)
    tryOrElse(false)(1 == 1) should be(true)
    tryOrElse(false)(1 == 2) should be(false)
    
    tryOrElse(false)(Util.???) should be(false)
    tryOrElse(true)(Util.???) should be(true)
  }
}