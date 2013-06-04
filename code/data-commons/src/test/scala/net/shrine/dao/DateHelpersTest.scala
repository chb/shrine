package net.shrine.dao

import org.junit.Test
import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import org.spin.tools.NetworkTime
import javax.xml.datatype.XMLGregorianCalendar

/**
 * @author clint
 * @date Nov 1, 2012
 */
final class DateHelpersTest extends TestCase with ShouldMatchersForJUnit {
  val now = new java.util.Date
    
  val sqlNow = new java.sql.Timestamp(now.getTime)
  
  @Test
  def testToXmlGc {
    val xmlNow = DateHelpers.toXmlGc(sqlNow)
    
    xmlNow.toGregorianCalendar.getTime.getTime should equal(sqlNow.getTime)
  }
  
  @Test
  def testToTimestamp {
    val xmlNow = NetworkTime.makeXMLGregorianCalendar(now)
    
    DateHelpers.toTimestamp(xmlNow) should equal(sqlNow)
  }
  
  @Test
  def testTimestampXmlGcRoundTrip {
    val xmlNow = NetworkTime.makeXMLGregorianCalendar(now)
    
    val roundTripped = DateHelpers.toXmlGc(DateHelpers.toTimestamp(xmlNow)) 
    
    roundTripped should equal(xmlNow)
    
    def millis(xmlGc: XMLGregorianCalendar) = xmlGc.toGregorianCalendar.getTime.getTime 
    
    millis(roundTripped) should equal(millis(xmlNow)) 
  }
}