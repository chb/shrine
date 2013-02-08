package net.shrine.adapter.dao.slick.tables

import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.model.PrivilegedUser
import net.shrine.dao.slick.tables.HasDriver
import net.shrine.dao.slick.tables.DateHelpers

/**
 * @author clint
 * @date Oct 15, 2012
 */
trait PrivilegedUsersComponent extends HasColumns { self: HasDriver =>
  import self.driver.simple._

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

    override def * = id ~: withoutId <> (PrivilegedUser, PrivilegedUser.unapply _)
  }
}