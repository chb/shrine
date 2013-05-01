package net.shrine.adapter

import scala.xml.{ NodeSeq, XML }
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol._
import net.shrine.config.HiveCredentials
import net.shrine.util.HttpClient
import net.shrine.serialization.XmlMarshaller
import net.shrine.util.Util

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

  //NB: default is a noop; only RunQueryAdapter needs this for now
  protected[adapter] def translateNetworkToLocal(request: T): T = request
  
  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val i2b2Response = callCrc(translateRequest(message.request))

    parseShrineResponse(XML.loadString(i2b2Response))
  }

  protected def callCrc(request: ShrineRequest): String = {
    val crcRequest = request.toI2b2String

    debug(s"Request to CRC:\r\n$crcRequest")

    val crcResponse = Util.time("Calling the CRC")(debug(_)) {
      httpClient.post(crcRequest, crcUrl)
    }

    debug(s"Response from CRC:\r\n$crcResponse")

    crcResponse
  }

  private[adapter] def translateRequest(request: ShrineRequest): ShrineRequest = request match {
    case transReq: TranslatableRequest[T] => {
      val HiveCredentials(domain, username, password, project) = hiveCredentials

      val authInfo = AuthenticationInfo(domain, username, Credential(password, false))

      translateNetworkToLocal(transReq.withAuthn(authInfo).withProject(project).asRequest)
    }
    case _ => request
  }
}