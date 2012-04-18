package net.shrine.adapter

import dao.AdapterDAO
import xml.NodeSeq
import net.shrine.protocol._
import org.spin.tools.crypto.signature.Identity
import net.shrine.config.HiveCredentials

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
    override protected val crcUrl: String,
    override protected val dao: AdapterDAO,
    override protected val hiveCredentials: HiveCredentials) extends CrcAdapter[DeleteQueryRequest, DeleteQueryResponse](crcUrl, dao, hiveCredentials) {

  protected def parseShrineResponse(nodeSeq: NodeSeq) = DeleteQueryResponse.fromI2b2(nodeSeq)

  override protected def processRequest(identity: Identity, message: BroadcastMessage) = {
    val response = super.processRequest(identity, message).asInstanceOf[DeleteQueryResponse]
    dao.removeMasterDefinitions(response.queryId)
    dao.removeUserToMasterMapping(response.queryId)
    response
  }

  protected def translateLocalToNetwork(response: DeleteQueryResponse) = {
    val networkId = dao.findNetworkMasterID(response.queryId.toString)
    response.withId(networkId.get)
  }

  protected def translateNetworkToLocal(request: DeleteQueryRequest) = {
    val localId = dao.findLocalMasterID(request.queryId).toLong
    request.withId(localId)
  }
}