package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import net.shrine.adapter.dao.scalaquery.rows.PatientSetRow

/**
 * @author clint
 * @date Oct 12, 2012
 */
object PatientSets extends Table[PatientSetRow]("PATIENT_SET") with HasId with HasResultId {
  def patientId = column[String]("PATIENT_NUM", O.NotNull)

  def withoutId = resultId ~ patientId
  
  import ProjectionHelpers._
  
  def * = id ~~ withoutId <> (PatientSetRow, PatientSetRow.unapply _)
}