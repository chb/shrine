package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import net.shrine.adapter.dao.scalaquery.rows.BreakdownResultRow

/**
 * @author clint
 * @date Oct 12, 2012
 */
object BreakdownResults extends Table[BreakdownResultRow]("BREAKDOWN_RESULT") with HasId with HasResultId {
  def dataKey = column[String]("DATA_KEY", O.NotNull)
  def originalValue = column[Long]("ORIGINAL_VALUE", O.NotNull)
  def obfuscatedValue = column[Long]("OBFUSCATED_VALUE", O.NotNull)
  
  def withoutId = resultId ~ dataKey ~ originalValue ~ obfuscatedValue
  
  import ProjectionHelpers._
  
  override def * = id ~~ withoutId <> (BreakdownResultRow, BreakdownResultRow.unapply _)
}