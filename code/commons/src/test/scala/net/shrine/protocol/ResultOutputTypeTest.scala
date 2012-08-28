package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test

/**
 * @author clint
 * @date Aug 28, 2012
 */
final class ResultOutputTypeTest extends TestCase with ShouldMatchers with AssertionsForJUnit {
  import ResultOutputType._
  
  @Test
  def testBreakdownFlag {
    PATIENT_AGE_COUNT_XML.isBreakdown should be(true)
    PATIENT_RACE_COUNT_XML.isBreakdown should be(true)
    PATIENT_VITALSTATUS_COUNT_XML.isBreakdown should be(true)
    PATIENT_GENDER_COUNT_XML.isBreakdown should be(true)
    PATIENTSET.isBreakdown should be(false)
    PATIENT_COUNT_XML.isBreakdown should be(false)
    ERROR.isBreakdown should be(false)
  }
  
  @Test
  def testBreakdownTypes {
    ResultOutputType.breakdownTypes.toSeq should equal(Seq(PATIENT_AGE_COUNT_XML, 
                                                           PATIENT_RACE_COUNT_XML, 
                                                           PATIENT_VITALSTATUS_COUNT_XML, 
                                                           PATIENT_GENDER_COUNT_XML))
  }
  
  @Test
  def testNonBreakdownTypes {
    ResultOutputType.nonBreakdownTypes.toSeq should equal(Seq(PATIENTSET,
                                                              PATIENT_COUNT_XML, 
                                                              ERROR))
  }
}