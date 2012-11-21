package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import java.sql.Date
import javax.xml.datatype.XMLGregorianCalendar

/**
 * @author clint
 * @date Oct 12, 2012
 */
trait HasCreationDate { self: Table[_] =>
  //TODO: How to express default? Do we need to here?
  import DateHelpers.Implicit._
  
  def creationDate = column[XMLGregorianCalendar]("DATE_CREATED", O.NotNull)
}