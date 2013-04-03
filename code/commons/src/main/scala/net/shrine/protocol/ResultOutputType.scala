package net.shrine.protocol

import net.shrine.util.SEnum

/**
 * @author Bill Simons
 * @author clint
 * @date 8/30/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
sealed class ResultOutputType private[ResultOutputType] (override val name: String, val isBreakdown: Boolean) extends ResultOutputType.Value {
  def isError: Boolean = this == ResultOutputType.ERROR
}

object ResultOutputType extends SEnum[ResultOutputType] {
  val PATIENTSET = new ResultOutputType("PATIENTSET", false)
  val PATIENT_COUNT_XML = new ResultOutputType("PATIENT_COUNT_XML", false)
  val PATIENT_AGE_COUNT_XML = new ResultOutputType("PATIENT_AGE_COUNT_XML", true)
  val PATIENT_RACE_COUNT_XML = new ResultOutputType("PATIENT_RACE_COUNT_XML", true)
  val PATIENT_VITALSTATUS_COUNT_XML = new ResultOutputType("PATIENT_VITALSTATUS_COUNT_XML", true)
  val PATIENT_GENDER_COUNT_XML = new ResultOutputType("PATIENT_GENDER_COUNT_XML", true)
  val ERROR = new ResultOutputType("ERROR", false)

  def breakdownTypes: Seq[ResultOutputType] = values.filter(_.isBreakdown)

  def nonBreakdownTypes: Seq[ResultOutputType] = values.filterNot(_.isBreakdown)
}
