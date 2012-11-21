package net.shrine.protocol

import net.shrine.protocol.CRCRequestType.GetPDOFromInputListRequestType
import xml._
import transform.{RuleTransformer, RewriteRule}
import net.shrine.util.XmlUtil
import net.shrine.serialization.I2b2Unmarshaller

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
    val optionsXml: NodeSeq) extends ShrineRequest(projectId, waitTimeMs, authn) {

  val requestType = GetPDOFromInputListRequestType

  def toXml = XmlUtil.stripWhitespace(
    <readPdo>
      {headerFragment}
      <optionsXml>
        {getOptionsXml}
      </optionsXml>
      <patientSetCollId>
        {patientSetCollId}
      </patientSetCollId>
    </readPdo>)

  def handle(handler: ShrineRequestHandler) = {
    handler.readPdo(this)
  }
  
  private[protocol] def getOptionsXml = ReadPdoRequest.updateCollId(optionsXml.head, patientSetCollId).toSeq

  protected def i2b2MessageBody =
    XmlUtil.stripWhitespace(
      <message_body>
        <ns3:pdoheader>
          <patient_set_limit>0</patient_set_limit>
          <estimated_time>180000</estimated_time>
          <request_type>getPDO_fromInputList</request_type>
        </ns3:pdoheader>{getOptionsXml}
      </message_body>)

  def withAuthn(ai: AuthenticationInfo) = this.copy(authn = ai)

  def withProject(proj: String) = this.copy(projectId = proj)

  def withPatientSetCollId(newPatientSetCollId: String) = this.copy(patientSetCollId = newPatientSetCollId)
}

object ReadPdoRequest extends I2b2Unmarshaller[ReadPdoRequest] with ShrineRequestUnmarshaller[ReadPdoRequest] {

  def fromI2b2(nodeSeq: NodeSeq): ReadPdoRequest = {
    new ReadPdoRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_body" \ "request" \ "input_list" \ "patient_list" \ "patient_set_coll_id").text,
      (nodeSeq \ "message_body" \ "request"))
  }

  def fromXml(nodeSeq: NodeSeq): ReadPdoRequest = {
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
