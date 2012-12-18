package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.ExtendedTable

/**
 * @author clint
 * @date Dec 18, 2012
 */
trait HasLocalId { self: ExtendedTable[_] =>
  protected def localIdColumn[T : TypeMapper]: NamedColumn[T] = column[T]("LOCAL_ID", O.NotNull)
}