package net.shrine.adapter.dao.slick.tables

import scala.slick.lifted.ForeignKeyAction
import net.shrine.dao.slick.tables.HasDriver

/**
 * @author clint
 * @date Jan 24, 2013
 */
trait IsSubResult extends HasColumns { self: HasDriver with QueryResultsComponent =>
  import self.driver.simple._

  /**
   * @author clint
   * @date Oct 12, 2012
   */
  trait HasResultId { self: Table[_] =>
    def resultId = column[Int]("RESULT_ID", O.NotNull)

    import ForeignKeyAction.{ NoAction, Cascade }

    def resultIdFk = foreignKey("ResultId_FK", resultId, QueryResults)(targetColumns = _.id, onUpdate = NoAction, onDelete = Cascade)
  }
}