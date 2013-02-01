package net.shrine.adapter.dao.model

import net.shrine.adapter.dao.slick.rows.PatientSetRow

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class PatientSet(resultId: Int, patientIds: Set[String]) extends HasResultId

object PatientSet {
  def fromRows(rows: Seq[PatientSetRow]): Option[PatientSet] = {
    if(rows.isEmpty) {
      None
    } else {
      Some(PatientSet(rows.head.resultId, rows.map(_.patientId).toSet))
    }
  }
}