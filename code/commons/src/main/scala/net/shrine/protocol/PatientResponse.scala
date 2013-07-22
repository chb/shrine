package net.shrine.protocol

import xml.{Utility, NodeSeq}
import net.shrine.util.XmlUtil

/**
 * @author ??
 * @date ??
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * NB: a case class for structural equals() and hashCode()
 */
final case class PatientResponse(
    val patientId: String,
    val params: Seq[ParamResponse]) extends ShrineResponse {

  def i2b2MessageBody = XmlUtil.stripWhitespace(<patient>
    <patient_id>
      {patientId}
    </patient_id>{params map {
      x =>
        x.i2b2MessageBody
    }}
  </patient>)

  def toXml = XmlUtil.stripWhitespace(<patient>
    <patient_id>
      {patientId}
    </patient_id>{params map {
      x =>
        x.toXml
    }}
  </patient>)
}

object PatientResponse extends I2b2Umarshaller[PatientResponse] with XmlUnmarshaller[PatientResponse] {
  def fromXml(nodeSeq: NodeSeq) = {
    new PatientResponse((nodeSeq \ "patient_id").text,
      ((nodeSeq \ "param").map { x => ParamResponse.fromXml(x) })
    )
  }

  def fromI2b2(nodeSeq: NodeSeq) = {
    new PatientResponse((nodeSeq \ "patient_id").text,
      ((nodeSeq \ "param").map { x => ParamResponse.fromI2b2(x) })
    )
  }
}