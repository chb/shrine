package net.shrine.adapter.dao.scalaquery.tables

import org.junit.Test
import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import org.spin.tools.NetworkTime

/**
 * @author clint
 * @date Nov 1, 2012
 */
final class DateHelpersTest extends TestCase with ShouldMatchersForJUnit {
  val now = new java.util.Date
    
  val sqlNow = new java.sql.Date(now.getTime)
  
  @Test
  def testToXmlGc {
    val xmlNow = DateHelpers.toXmlGc(sqlNow)
    
    xmlNow.toGregorianCalendar.getTime.getTime should equal(sqlNow.getTime)
  }
  
  @Test
  def testToSqlDate {
    val xmlNow = NetworkTime.makeXMLGregorianCalendar(now)
    
    DateHelpers.toSqlDate(xmlNow) should equal(sqlNow)
  }
  
  @Test
  def testSqlDateXmlGcRoundTrip {
    val xmlNow = NetworkTime.makeXMLGregorianCalendar(now)
    
    DateHelpers.toXmlGc(DateHelpers.toSqlDate(xmlNow)) should equal(xmlNow)
  }
}