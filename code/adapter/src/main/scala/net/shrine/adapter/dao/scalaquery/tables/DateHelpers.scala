package net.shrine.adapter.dao.scalaquery.tables

import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime
import org.scalaquery.ql.MappedTypeMapper
import org.scalaquery.ql.TypeMapper
import java.sql.Date
import org.scalaquery.ql.BaseTypeMapper
import java.util.Calendar

/**
 * @author clint
 * @date Oct 16, 2012
 */
object DateHelpers {
  def toSqlDate(xmlGc: XMLGregorianCalendar) = {
    if(xmlGc == null) null
    else new java.sql.Date(xmlGc.toGregorianCalendar.getTime.getTime)
  }

  def toXmlGc(date: java.sql.Date): XMLGregorianCalendar = {
    if(date == null) null
    else NetworkTime.makeXMLGregorianCalendar(new java.util.Date(date.getTime))
  }

  def daysFromNow(days: Int): XMLGregorianCalendar = {
    val cal = Calendar.getInstance
    
    cal.add(Calendar.DAY_OF_MONTH, days)
    
    NetworkTime.makeXMLGregorianCalendar(cal.getTime)
  }
  
  object Implicit {
    //NB: this val's type must be BaseTypeMapper[T] instead of just TypeMapper[T] to allow
    //using mapped date columns with ===, =!=, <, >, etc in for-comprehensions.
    implicit val xmlGregorianCalendar2JavaSqlDateMapper: BaseTypeMapper[XMLGregorianCalendar] =
      MappedTypeMapper.base[XMLGregorianCalendar, Date](toSqlDate, toXmlGc)
  }
}