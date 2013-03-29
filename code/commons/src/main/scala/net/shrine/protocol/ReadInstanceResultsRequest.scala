package net.shrine.protocol

import xml.NodeSeq
import net.shrine.protocol.CRCRequestType.InstanceRequestType
import net.shrine.util.XmlUtil
import net.shrine.serialization.I2b2Unmarshaller
import net.shrine.protocol.handlers.ReadInstanceResultsHandler

/**
 * @author Bill Simons
 * @date 3/17/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 * 
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * NOTE: Now that the adapter caches/stores results from the CRC, Instead of an
 * i2b2 instance id, this class now contains the Shrine-generated, network-wide 
 * id of a query, which is used to obtain results previously obtained from the 
 * CRC from Shrine's datastore.
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */
final case class ReadInstanceResultsRequest(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo,
  /*
   * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   * NOTE: Now that the adapter caches/stores results from the CRC, Instead of an
   * i2b2 instance id, this class now contains the Shrine-generated, network-wide 
   * id of a query, which is used to obtain results previously obtained from the 
   * CRC from Shrine's datastore.
   * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   */ 
  val shrineNetworkQueryId: Long) extends ShrineRequest(projectId, waitTimeMs, authn) with CrcRequest {

  override val requestType = InstanceRequestType

  override type Handler = ReadInstanceResultsHandler
  
  override def handle(handler: Handler, shouldBroadcast: Boolean) = handler.readInstanceResults(this, shouldBroadcast)
  
  override def toXml = XmlUtil.stripWhitespace {
    <readInstanceResults>
      { headerFragment }
      <shrineNetworkQueryId>{ shrineNetworkQueryId }</shrineNetworkQueryId>
    </readInstanceResults>
  }

  def withId(id: Long) = this.copy(shrineNetworkQueryId = id)

  def withProject(proj: String) = this.copy(projectId = proj)

  def withAuthn(ai: AuthenticationInfo) = this.copy(authn = ai)

  protected override def i2b2MessageBody = XmlUtil.stripWhitespace {
    <message_body>
      { i2b2PsmHeader }
      <ns4:request xsi:type="ns4:instance_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <query_instance_id>{ shrineNetworkQueryId }</query_instance_id>
      </ns4:request>
    </message_body>
  }
}

object ReadInstanceResultsRequest extends I2b2Unmarshaller[ReadInstanceResultsRequest] with ShrineRequestUnmarshaller[ReadInstanceResultsRequest] {

  override def fromI2b2(nodeSeq: NodeSeq): ReadInstanceResultsRequest = {
    new ReadInstanceResultsRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_body" \ "request" \ "query_instance_id").text.toLong)
  }

  override def fromXml(nodeSeq: NodeSeq): ReadInstanceResultsRequest = {
    new ReadInstanceResultsRequest(
      shrineProjectId(nodeSeq),
      shrineWaitTimeMs(nodeSeq),
      shrineAuthenticationInfo(nodeSeq),
      (nodeSeq \ "shrineNetworkQueryId").text.toLong)
  }
}