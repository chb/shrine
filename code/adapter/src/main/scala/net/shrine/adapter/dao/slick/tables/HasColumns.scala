package net.shrine.adapter.dao.slick.tables

import scala.slick.driver.ExtendedProfile
import javax.xml.datatype.XMLGregorianCalendar
import scala.slick.lifted.ForeignKeyAction
import net.shrine.dao.slick.tables.HasDriver
import net.shrine.dao.slick.tables.DateHelpers

/**
 * @author clint
 * @date Jan 24, 2013
 */
trait HasColumns { self: HasDriver =>

  import self.driver.simple._

  /**
   * @author clint
   * @date Oct 12, 2012
   */
  trait HasId { self: Table[_] =>
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc, O.NotNull)
  }

  /**
   * @author clint
   * @date Oct 12, 2012
   */
  trait HasCreationDate { self: Table[_] =>
    //TODO: How to express default? Do we need to here?
    import DateHelpers.Implicit._

    def creationDate = column[XMLGregorianCalendar]("DATE_CREATED", O.NotNull)
  }

  /**
   * @author clint
   * @date Dec 18, 2012
   */
  trait HasLocalId { self: Table[_] =>
    protected def localIdColumn[T: TypeMapper]: Column[T] = column[T]("LOCAL_ID", O.NotNull)
  }

  /**
   * @author clint
   * @date Oct 15, 2012
   */
  trait HasUsernameAndDomain { self: Table[_] =>
    def username = column[String]("USERNAME", O.NotNull)
    def domain = column[String]("DOMAIN", O.NotNull)
  }
  
  /**
   * @author clint
   * @date Oct 15, 2012
   */
  trait HasUniqueUsernameAndDomain extends HasUsernameAndDomain { self: Table[_] =>
    //NB: Uniqueness constraint on (username, domain)
    def usernameAndDomainIndex = index("usernameAndDomainIndex", username ~ domain, unique = true)
  }
}