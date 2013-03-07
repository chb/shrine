package net.shrine.util

import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import org.junit.Test
import scala.util.Success
import scala.util.Failure
import scala.util.Try

/**
 * @author clint
 * @date Oct 30, 2012
 */
final class UtilTest extends TestCase with ShouldMatchersForJUnit{
  
  @Test
  def testNow {
    val now = Util.now
    
    now should not be(null)
    
    //TODO: Is there anything more we can do?  
    //Off-by-one and build-server timing issues abound for all the approaches I can think of. :( -Clint 
  }
  
  @Test
  def testTime {
    //verify values are passed through
    val x = 123
    
    val noop: String => Unit = _ => ()
    
    import Util.time
    
    time("Identity")(noop)(x) should be(x)
  }
  
  @Test
  def testTryOrElse {
    import Util.tryOrElse 
    
    tryOrElse(false)(true) should be(true)
    tryOrElse(false)(false) should be(false)
    tryOrElse(false)(1 == 1) should be(true)
    tryOrElse(false)(1 == 2) should be(false)
    
    tryOrElse(false)(1 / 0 == 0) should be(false)
    tryOrElse(true)(1 / 0 == 0) should be(true)
  }
  
  private val x = 123
  
  private val e = new Exception with scala.util.control.NoStackTrace
  
  @Test
  def testOptionTryImplicits {
    import Util.Tries.Implicits
    
    Implicits.option2Try(Some(x)) should equal(Success(x))
    Implicits.option2Try(None).isFailure should be(true) 
    
    Implicits.try2Option(Success(x)) should equal(Some(x))
    Implicits.try2Option(Failure(e)) should equal(None)
  }
  
  @Test
  def testSequenceOption {
    import Util.Tries.sequence
    
    sequence(Some(Success(x))) should equal(Success(Some(x)))
    sequence(Some(Failure(e))) should equal(Failure(e))
    
    sequence(None) should be(Success(None))
  }
  
  @Test
  def testSequenceTraversable {
    import Util.Tries.sequence
    
    val y = 234
    val z = 43985
    
    sequence(List(Success(x))) should equal(Success(List(x)))
    sequence(Seq(Failure(e))) should equal(Failure(e))
    
    sequence(Vector(Success(x), Try(y), Try(z))) should equal(Success(Vector(x, y, z)))
    
    sequence(Vector(Failure(e), Try(y), Try(z))) should equal(Failure(e))
    sequence(Vector(Try(x), Failure(e), Try(z))) should equal(Failure(e))
    sequence(Vector(Try(x), Try(y), Failure(e))) should equal(Failure(e))
    
    val f = new Exception with scala.util.control.NoStackTrace
    
    sequence(Seq(Try(x), Failure(e), Failure(f))) should equal(Failure(e))
    sequence(Seq(Failure(f), Failure(e))) should equal(Failure(f))
    
    sequence(Nil) should be(Success(Nil))
  }
}