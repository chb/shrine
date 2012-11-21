package net.shrine.adapter

import scala.xml.NodeSeq
import net.shrine.protocol._
import org.spin.tools.crypto.signature.Identity
import net.shrine.config.HiveCredentials
import net.shrine.util.HttpClient
import net.shrine.serialization.XmlMarshaller
import net.shrine.adapter.dao.AdapterDao

/**
 * @author Bill Simons
 * @date 4/12/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class DeleteQueryAdapter(
    crcUrl: String,
    httpClient: HttpClient,
    dao: AdapterDao,
    override protected val hiveCredentials: HiveCredentials) extends CrcAdapter[DeleteQueryRequest, DeleteQueryResponse](crcUrl, httpClient, hiveCredentials) {

  override protected def parseShrineResponse(nodeSeq: NodeSeq) = DeleteQueryResponse.fromI2b2(nodeSeq)

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val response = super.processRequest(identity, message).asInstanceOf[DeleteQueryResponse]
    
    dao.deleteQuery(response.queryId)
    
    response
  }
}