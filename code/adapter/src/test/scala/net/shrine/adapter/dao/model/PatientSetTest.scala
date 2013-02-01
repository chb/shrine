package net.shrine.adapter.dao.model

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.adapter.dao.slick.rows.PatientSetRow

/**
 * @author clint
 * @date Nov 1, 2012
 */
final class PatientSetTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testFromRows {
    PatientSet.fromRows(Nil) should be(None)
    
    val rows = Seq(PatientSetRow(1, 123, "foo"), PatientSetRow(2, 123, "bar"), PatientSetRow(3, 123, "nuh"))
    
    //TODO: test handing rows with different result ids?
    
    val Some(patientSet) = PatientSet.fromRows(rows)
    
    patientSet.resultId should equal(123)
    patientSet.patientIds should equal(Set("foo", "bar", "nuh"))
  }
}