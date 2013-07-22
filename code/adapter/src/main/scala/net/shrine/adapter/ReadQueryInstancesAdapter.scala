package net.shrine.adapter

import dao.AdapterDAO
import net.shrine.config.I2B2HiveCredentials
import xml.NodeSeq
import net.shrine.protocol.{ReadQueryInstancesResponse, ReadQueryInstancesRequest}

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
    override protected val hiveCredentials: I2B2HiveCredentials) extends CrcAdapter[ReadQueryInstancesRequest, ReadQueryInstancesResponse](crcUrl, dao, hiveCredentials) {

  protected def parseShrineResponse(nodeSeq: NodeSeq) = ReadQueryInstancesResponse.fromI2b2(nodeSeq)

  protected def translateLocalToNetwork(response: ReadQueryInstancesResponse) = {
    val networkId = dao.findNetworkMasterID(response.queryMasterId.toString).longValue
    val result = response.withId(networkId)
    result.queryInstances foreach {instance =>
      instance.setQueryInstanceId(dao.findNetworkInstanceID(instance.getQueryInstanceId.toString).toString)
    }
    result
  }

  protected def translateNetworkToLocal(request: ReadQueryInstancesRequest) = {
    val localId = dao.findLocalMasterID(request.queryId).toLong
    request.withId(localId)
  }
}