package net.shrine.adapter

import dao.AdapterDAO
import xml.NodeSeq
import net.shrine.protocol.{ReadQueryInstancesResponse, ReadQueryInstancesRequest}
import net.shrine.config.HiveCredentials

/**
 * @author Bill Simons
 * @date 4/13/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadQueryInstancesAdapter(
    override protected val crcUrl: String,
    override protected val dao: AdapterDAO,
    override protected val hiveCredentials: HiveCredentials) extends CrcAdapter[ReadQueryInstancesRequest, ReadQueryInstancesResponse](crcUrl, dao, hiveCredentials) {

  protected def parseShrineResponse(nodeSeq: NodeSeq) = ReadQueryInstancesResponse.fromI2b2(nodeSeq)

  protected def translateLocalToNetwork(response: ReadQueryInstancesResponse) = {
    val networkId = dao.findNetworkMasterID(response.queryMasterId.toString).get
    val result = response.withId(networkId)
    val translatedInstances = result.queryInstances map { instance =>
      val networkId = dao.findNetworkInstanceID(instance.queryInstanceId.toString).get.toString
      instance.withId(networkId)
    }
    result.withInstances(translatedInstances)
  }

  protected def translateNetworkToLocal(request: ReadQueryInstancesRequest) = {
    val localId = dao.findLocalMasterID(request.queryId).toLong
    request.withId(localId)
  }
}