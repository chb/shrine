package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

/**
 * @author clint
 * @date Oct 12, 2012
 */
trait HasId { self: Table[_] =>
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc, O.NotNull)
}