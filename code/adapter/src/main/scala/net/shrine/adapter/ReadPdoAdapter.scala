package net.shrine.adapter

import dao.LegacyAdapterDAO
import xml.NodeSeq
import net.shrine.protocol._
import net.shrine.config.HiveCredentials
import net.shrine.util.HttpClient

class ReadPdoAdapter(
    crcUrl: String,
    httpClient: HttpClient,
    dao: LegacyAdapterDAO,
    hiveCredentials: HiveCredentials)
    extends CrcAdapter[ReadPdoRequest, ReadPdoResponse](crcUrl, httpClient, hiveCredentials) {

  override protected def parseShrineResponse(nodeSeq: NodeSeq) = ReadPdoResponse.fromI2b2(nodeSeq)
}
