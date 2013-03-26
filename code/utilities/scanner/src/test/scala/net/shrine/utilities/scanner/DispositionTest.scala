package net.shrine.utilities.scanner

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

/**
 * @author clint
 * @date Mar 25, 2013
 */
final class DispositionTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testNames {
    import Disposition._
    
    ShouldNotHaveBeenMapped.name should equal("ShouldNotHaveBeenMapped")
    ShouldNotHaveBeenMapped.toString should equal("ShouldNotHaveBeenMapped")
    
    ShouldHaveBeenMapped.name should equal("ShouldHaveBeenMapped")
    ShouldHaveBeenMapped.toString should equal("ShouldHaveBeenMapped")

    NeverFinished.name should equal("NeverFinished")
    NeverFinished.toString should equal("NeverFinished")
  }
}