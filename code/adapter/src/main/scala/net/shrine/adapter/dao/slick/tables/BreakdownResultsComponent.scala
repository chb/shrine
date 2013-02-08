package net.shrine.adapter.dao.slick.tables

import net.shrine.adapter.dao.slick.rows.BreakdownResultRow
import net.shrine.dao.slick.tables.HasDriver

/**
 * @author clint
 * @date Oct 12, 2012
 */
trait BreakdownResultsComponent extends IsSubResult { self: HasDriver with QueryResultsComponent =>
  import self.driver.simple._
  
  object BreakdownResults extends Table[BreakdownResultRow]("BREAKDOWN_RESULT") with HasId with HasResultId {
    def dataKey = column[String]("DATA_KEY", O.NotNull)
    def originalValue = column[Long]("ORIGINAL_VALUE", O.NotNull)
    def obfuscatedValue = column[Long]("OBFUSCATED_VALUE", O.NotNull)

    def withoutId = resultId ~ dataKey ~ originalValue ~ obfuscatedValue

    override def * = id ~: withoutId <> (BreakdownResultRow, BreakdownResultRow.unapply _)
  }
}