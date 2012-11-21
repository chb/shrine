package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

/**
 * @author clint
 * @date Oct 15, 2012
 */
trait HasUniqueUsernameAndDomain extends HasUsernameAndDomain { self: Table[_] =>
  //NB: Uniqueness constraint on (username, domain)
  def usernameAndDomainIndex = index("usernameAndDomainIndex", username ~ domain, unique = true) 
}