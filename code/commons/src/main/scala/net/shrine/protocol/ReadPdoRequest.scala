package net.shrine.protocol

import net.shrine.protocol.CRCRequestType.GetPDOFromInputListRequestType
import xml._
import transform.{ RuleTransformer, RewriteRule }
import net.shrine.util.XmlUtil
import net.shrine.serialization.I2b2Unmarshaller
import net.shrine.protocol.handlers.ReadPdoHandler

/**
 * @author Bill Simons
 * @date 3/9/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class ReadPdoRequest(
    override val projectId: String,
    override val waitTimeMs: Long,
    override val authn: AuthenticationInfo,
    val patientSetCollId: String,
    val optionsXml: NodeSeq) extends DoubleDispatchingShrineRequest(projectId, waitTimeMs, authn) with TranslatableRequest[ReadPdoRequest] {

  override val requestType = GetPDOFromInputListRequestType

  override type Handler = ReadPdoHandler

  override def handle(handler: Handler, shouldBroadcast: Boolean) = handler.readPdo(this, shouldBroadcast)

  override def toXml = XmlUtil.stripWhitespace {
    <readPdo>
      { headerFragment }
      <optionsXml>
        { getOptionsXml }
      </optionsXml>
      <patientSetCollId>
        { patientSetCollId }
      </patientSetCollId>
    </readPdo>
  }

  private[protocol] def getOptionsXml = ReadPdoRequest.updateCollId(optionsXml.head, patientSetCollId).toSeq

  protected override def i2b2MessageBody = XmlUtil.stripWhitespace {
    <message_body>
      <ns3:pdoheader>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>180000</estimated_time>
        <request_type>getPDO_fromInputList</request_type>
      </ns3:pdoheader>{ getOptionsXml }
    </message_body>
  }

  override def withAuthn(ai: AuthenticationInfo) = this.copy(authn = ai)

  override def withProject(proj: String) = this.copy(projectId = proj)

  def withPatientSetCollId(newPatientSetCollId: String) = this.copy(patientSetCollId = newPatientSetCollId)
}

object ReadPdoRequest extends I2b2Unmarshaller[ReadPdoRequest] with ShrineRequestUnmarshaller[ReadPdoRequest] {

  override def fromI2b2(nodeSeq: NodeSeq): ReadPdoRequest = {
    new ReadPdoRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_body" \ "request" \ "input_list" \ "patient_list" \ "patient_set_coll_id").text,
      (nodeSeq \ "message_body" \ "request"))
  }

  override def fromXml(nodeSeq: NodeSeq): ReadPdoRequest = {
    new ReadPdoRequest(
      shrineProjectId(nodeSeq),
      shrineWaitTimeMs(nodeSeq),
      shrineAuthenticationInfo(nodeSeq),
      (nodeSeq \ "patientSetCollId").text,
      (nodeSeq \ "optionsXml" \ "request"))

  }

  def updateCollId(nodes: NodeSeq, patientSetCollId: String): Option[Node] = {
    object rule extends RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case Elem(prefix, "patient_set_coll_id", attribs, scope, _*) => {
          Elem(prefix, "patient_set_coll_id", attribs, scope, Text(patientSetCollId))
        }
        case other => other
      }
    }

    val transformed = NodeSeq.Empty ++ (new RuleTransformer(rule)).transform(nodes)

    transformed.headOption
  }
}
