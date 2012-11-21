package net.shrine.adapter

import dao.LegacyAdapterDAO
import xml.{NodeSeq, XML}
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol._
import net.shrine.config.HiveCredentials
import net.shrine.util.HttpClient
import net.shrine.serialization.XmlMarshaller

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
abstract class CrcAdapter[T <: ShrineRequest, V <: ShrineResponse](
    crcUrl: String,
    httpClient: HttpClient,
    override protected val hiveCredentials: HiveCredentials) extends WithHiveCredentialsAdapter(hiveCredentials) {

  protected def parseShrineResponse(nodeSeq: NodeSeq): ShrineResponse

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val i2b2Response = callCrc(message.request)
    
    parseShrineResponse(XML.loadString(i2b2Response))
  }

  protected def callCrc(request: ShrineRequest): String = {
    val crcRequest = request.toI2b2String
    
    debug(String.format("Request to CRC:\r\n%s", crcRequest))

    val start = System.currentTimeMillis
    
    val crcResponse = httpClient.post(crcRequest, crcUrl)
    
    val elapsed = System.currentTimeMillis - start
    
    debug("Calling the CRC took " + elapsed + "ms")
    
    debug(String.format("Response from CRC:\r\n%s", crcResponse))
    
    crcResponse
  }
}