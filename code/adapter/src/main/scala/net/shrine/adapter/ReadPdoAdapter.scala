package net.shrine.adapter

import dao.{IDPair, MasterTuple, UserAndMaster, AdapterDAO}
import net.shrine.config.I2B2HiveCredentials
import xml.NodeSeq
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol._

class ReadPdoAdapter(
    crcUrl2: String,
    dao: AdapterDAO,
    hiveCredentials: I2B2HiveCredentials)
    extends CrcAdapter[ReadPdoRequest, ReadPdoResponse](crcUrl2, dao, hiveCredentials) {

  protected def parseShrineResponse(nodeSeq: NodeSeq) = ReadPdoResponse.fromI2b2(nodeSeq)

  protected def translateLocalToNetwork(response: ReadPdoResponse): ReadPdoResponse = response

  protected[adapter] def translateNetworkToLocal(request: ReadPdoRequest) = {
    val localPatientCollId = dao.findLocalResultID(java.lang.Long.parseLong(request.patientSetCollId))
    request.withPatientSetCollId(localPatientCollId)
  }

}
