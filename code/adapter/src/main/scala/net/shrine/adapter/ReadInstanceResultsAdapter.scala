package net.shrine.adapter

import dao.AdapterDAO
import xml.NodeSeq
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.{BroadcastMessage, ReadInstanceResultsResponse, ReadInstanceResultsRequest}
import net.shrine.adapter.Obfuscator.obfuscate
import net.shrine.config.HiveCredentials

/**
 * @author Bill Simons
 * @date 4/14/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadInstanceResultsAdapter(
    override protected val crcUrl: String,
    override protected val dao: AdapterDAO,
    override protected val hiveCredentials: HiveCredentials,
    private val doObfuscation: Boolean ) extends CrcAdapter[ReadInstanceResultsRequest, ReadInstanceResultsResponse](crcUrl, dao, hiveCredentials) {

  protected def parseShrineResponse(nodeSeq: NodeSeq) = ReadInstanceResultsResponse.fromI2b2(nodeSeq)

  protected def translateLocalToNetwork(response: ReadInstanceResultsResponse) = {
    val networkId = dao.findNetworkInstanceID(response.queryInstanceId.toString).get
    response.withId(networkId).withResults(response.results map {result =>
      result.withId(dao.findNetworkResultID(result.resultId.toString).get)
    })
  }

  protected def translateNetworkToLocal(request: ReadInstanceResultsRequest) = {
    val localId = dao.findLocalInstanceID(request.instanceId).toLong
    request.withId(localId)
  }

  protected def obfuscateResponse(response: ReadInstanceResultsResponse): ReadInstanceResultsResponse = {
    if (doObfuscation) response.withResults(obfuscate(response.results, dao)) else response
  }

  override protected def processRequest(identity: Identity, message: BroadcastMessage) = {
    val response = super.processRequest(identity, message).asInstanceOf[ReadInstanceResultsResponse]
    obfuscateResponse(response)
  }
}

