package net.shrine.utilities.scanner.csv

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.utilities.scanner.Disposition

/**
 * @author clint
 * @date Mar 25, 2013
 */
final class CsvRowtest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testToArray {
    val term = "foo"
    
    for {
      disp <- Disposition.values
    } {
      val row = CsvRow(disp, term)
      
      val Array(actualDisp: Disposition, actualTerm: String) = row.toArray
      
      actualDisp should equal(disp)
      actualTerm should equal(term)
    }
  }
}