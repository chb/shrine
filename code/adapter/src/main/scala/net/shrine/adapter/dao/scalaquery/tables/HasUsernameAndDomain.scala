package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

/**
 * @author clint
 * @date Oct 15, 2012
 */
trait HasUsernameAndDomain { self: Table[_] =>
  def username = column[String]("USERNAME", O.NotNull)
  def domain = column[String]("DOMAIN", O.NotNull)
}