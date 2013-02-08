package net.shrine.adapter.dao.slick.tables

import net.shrine.adapter.dao.slick.rows.PatientSetRow
import net.shrine.dao.slick.tables.HasDriver

/**
 * @author clint
 * @date Oct 12, 2012
 */
trait PatientSetsComponent extends IsSubResult { self: HasDriver with QueryResultsComponent =>
  import self.driver.simple._

  object PatientSets extends Table[PatientSetRow]("PATIENT_SET") with HasId with HasResultId {
    def patientId = column[String]("PATIENT_NUM", O.NotNull)

    def withoutId = resultId ~ patientId

    def * = id ~: withoutId <> (PatientSetRow, PatientSetRow.unapply _)
  }
}