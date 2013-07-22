package net.shrine.adapter

import dao.AdapterDAO
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.{ReadQueryDefinitionResponse, ReadQueryDefinitionRequest, BroadcastMessage}
import net.shrine.config.I2B2HiveCredentials
import org.spin.tools.NetworkTime._

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
    override protected val crcUrl: String,
    override protected val dao: AdapterDAO,
    override protected val hiveCredentials: I2B2HiveCredentials) extends Adapter(crcUrl, dao, hiveCredentials) {

  protected def processRequest(identity: Identity, message: BroadcastMessage) = {
    val newRequest = message.request.asInstanceOf[ReadQueryDefinitionRequest]
    val definition = dao.findMasterQueryDefinition(newRequest.queryId)
    new ReadQueryDefinitionResponse(definition.getQueryMasterId.toLong,
      definition.getName,
      definition.getUserId,
      makeXMLGregorianCalendar(definition.getCreateDate),
      definition.getRequestXml)
  }
}