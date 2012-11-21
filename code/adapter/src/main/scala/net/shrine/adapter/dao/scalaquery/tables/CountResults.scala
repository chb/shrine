package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import java.sql.Date
import net.shrine.adapter.dao.model.Count

/**
 * @author clint
 * @date Oct 12, 2012
 */
object CountResults extends Table[Count]("COUNT_RESULT") with HasId with HasResultId with HasCreationDate {
  def originalCount = column[Long]("ORIGINAL_COUNT", O.NotNull)
  def obfuscatedCount = column[Long]("OBFUSCATED_COUNT", O.NotNull)
  
  def withoutGeneratedColumns = resultId ~ originalCount ~ obfuscatedCount
  
  import ProjectionHelpers._
  
  override def * = id ~~ withoutGeneratedColumns ~ creationDate <> (Count, Count.unapply _)
}