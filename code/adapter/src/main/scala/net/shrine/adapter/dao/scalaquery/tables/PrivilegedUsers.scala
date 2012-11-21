package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import java.sql.Date
import net.shrine.adapter.dao.model.PrivilegedUser
import javax.xml.datatype.XMLGregorianCalendar

/**
 * @author clint
 * @date Oct 15, 2012
 */
object PrivilegedUsers extends Table[PrivilegedUser]("PRIVILEGED_USER") with HasId with HasUniqueUsernameAndDomain {
  def threshold = column[Int]("THRESHOLD", O.NotNull)
  
  def overrideDate = {
    import DateHelpers.Implicit._
    
    //TODO: Needs to be nullable and non-Optional to work 
    //https://github.com/slick/slick/issues/54
    //until we can upgrade to Scala 2.10/Slick 
    column[XMLGregorianCalendar]("OVERRIDE_DATE", O.NotNull)
  }
  
  def withoutId = username ~ domain ~ threshold ~ overrideDate
  
  import ProjectionHelpers._
  
  override def * = id ~~ withoutId <> (PrivilegedUser, PrivilegedUser.unapply _)
}