package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import net.shrine.adapter.dao.model.ShrineError

/**
 * @author clint
 * @date Oct 15, 2012
 */
object ErrorResults extends Table[ShrineError]("ERROR_RESULT") with HasId with HasResultId {
  def message = column[String]("MESSAGE", O.NotNull)
  
  def withoutId = resultId ~ message
  
  import ProjectionHelpers._
  
  override def * = id ~~ withoutId <> (ShrineError, ShrineError.unapply _)
}