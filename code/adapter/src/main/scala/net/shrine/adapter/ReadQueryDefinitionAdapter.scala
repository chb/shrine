package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.{ ReadQueryDefinitionResponse, ReadQueryDefinitionRequest, BroadcastMessage }
import org.spin.tools.NetworkTime._
import net.shrine.config.{ HiveCredentials }
import net.shrine.util.HttpClient
import net.shrine.serialization.XmlMarshaller
import net.shrine.util.Util
import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.query.QueryDefinition

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
class ReadQueryDefinitionAdapter(dao: AdapterDao) extends Adapter {

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val newRequest = message.request.asInstanceOf[ReadQueryDefinitionRequest]

    val resultOption = for {
      definition <- dao.findQueryByNetworkId(newRequest.queryId)
    } yield {
      new ReadQueryDefinitionResponse(
        definition.networkId,
        definition.name,
        definition.username,
        definition.dateCreated,
        //TODO: I2b2 or Shrine format?
        QueryDefinition(definition.name, definition.queryExpr).toI2b2String) 
    }
    
    resultOption.getOrElse(ErrorResponse("Couldn't find query with network id: " + newRequest.queryId))
  }
}