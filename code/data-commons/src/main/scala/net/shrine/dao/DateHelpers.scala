package net.shrine.dao

import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime
import java.util.Calendar

/**
 * @author clint
 * @date Oct 16, 2012
 */
object DateHelpers {
  def toTimestamp(xmlGc: XMLGregorianCalendar) = {
    new java.sql.Timestamp(xmlGc.toGregorianCalendar.getTime.getTime)
  }

  def toXmlGc(date: java.sql.Timestamp): XMLGregorianCalendar = {
    NetworkTime.makeXMLGregorianCalendar(new java.util.Date(date.getTime))
  }

  def daysFromNow(days: Int): XMLGregorianCalendar = {
    val cal = Calendar.getInstance
    
    cal.add(Calendar.DAY_OF_MONTH, days)
    
    NetworkTime.makeXMLGregorianCalendar(cal.getTime)
  }
}