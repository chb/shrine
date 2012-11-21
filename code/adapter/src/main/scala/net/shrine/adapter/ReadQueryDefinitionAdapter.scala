package net.shrine.adapter

import dao.LegacyAdapterDAO
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.{ReadQueryDefinitionResponse, ReadQueryDefinitionRequest, BroadcastMessage}
import org.spin.tools.NetworkTime._
import net.shrine.config.{HiveCredentials}
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
class ReadQueryDefinitionAdapter(
    dao: LegacyAdapterDAO,
    override protected val hiveCredentials: HiveCredentials) extends WithHiveCredentialsAdapter(hiveCredentials) {

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val newRequest = message.request.asInstanceOf[ReadQueryDefinitionRequest]
    val definition = dao.findMasterQueryDefinition(newRequest.queryId)
    
    new ReadQueryDefinitionResponse(definition.getQueryMasterId.toLong,
      definition.getName,
      definition.getUserId,
      makeXMLGregorianCalendar(definition.getCreateDate),
      definition.getRequestXml)
  }
}