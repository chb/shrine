package net.shrine.adapter

import dao.AdapterDAO
import xml.NodeSeq
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol._
import net.shrine.config.I2B2HiveCredentials

/**
 * @author Bill Simons
 * @date 4/11/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class RenameQueryAdapter(
    override protected val crcUrl: String,
    override protected val dao: AdapterDAO,
    override protected val hiveCredentials: I2B2HiveCredentials) extends CrcAdapter[RenameQueryRequest, RenameQueryResponse](crcUrl, dao, hiveCredentials) {

  protected def parseShrineResponse(nodeSeq: NodeSeq) = RenameQueryResponse.fromI2b2(nodeSeq)

  override protected def processRequest(identity: Identity, message: BroadcastMessage) = {
    val response = super.processRequest(identity, message).asInstanceOf[RenameQueryResponse]
    dao.updateUsersToMasterQueryName(response.queryId, response.queryName)
    response
  }

  protected def translateLocalToNetwork(response: RenameQueryResponse) = {
    val networkId = dao.findNetworkMasterID(response.queryId.toString).longValue
    response.withId(networkId)
  }

  protected def translateNetworkToLocal(request: RenameQueryRequest) = {
    val localId = dao.findLocalMasterID(request.queryId).toLong
    request.withId(localId)
  }
}