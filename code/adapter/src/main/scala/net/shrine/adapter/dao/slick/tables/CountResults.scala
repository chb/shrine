package net.shrine.adapter.dao.slick.tables

import net.shrine.adapter.dao.slick.rows.CountRow
import net.shrine.dao.slick.tables.HasDriver
import net.shrine.dao.slick.tables.ProjectionHelpers

/**
 * @author clint
 * @date Oct 12, 2012
 */
trait CountResultsComponent extends IsSubResult { self: HasDriver with QueryResultsComponent =>
  import self.driver.simple._

  object CountResults extends Table[CountRow]("COUNT_RESULT") with HasId with HasResultId with HasCreationDate {
    def originalCount = column[Long]("ORIGINAL_COUNT", O.NotNull)
    def obfuscatedCount = column[Long]("OBFUSCATED_COUNT", O.NotNull)

    def withoutGeneratedColumns = resultId ~ originalCount ~ obfuscatedCount

    import ProjectionHelpers._

    override def * = id ~~ withoutGeneratedColumns ~ creationDate <> (CountRow, CountRow.unapply _)
  }
}