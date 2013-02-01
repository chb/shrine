package net.shrine.adapter.dao.slick.tables

import net.shrine.adapter.dao.model.ShrineError
import net.shrine.dao.slick.tables.HasDriver
import net.shrine.dao.slick.tables.ProjectionHelpers

/**
 * @author clint
 * @date Oct 15, 2012
 */
trait ErrorResultsComponent extends IsSubResult { self: HasDriver with QueryResultsComponent =>
  import self.driver.simple._

  object ErrorResults extends Table[ShrineError]("ERROR_RESULT") with HasId with HasResultId {
    def message = column[String]("MESSAGE", O.NotNull)

    def withoutId = resultId ~ message

    import ProjectionHelpers._

    override def * = id ~~ withoutId <> (ShrineError, ShrineError.unapply _)
  }
}