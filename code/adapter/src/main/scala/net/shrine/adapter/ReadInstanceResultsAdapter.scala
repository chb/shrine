package net.shrine.adapter

import dao.LegacyAdapterDAO
import xml.NodeSeq
import org.spin.tools.crypto.signature.Identity
import net.shrine.config.HiveCredentials
import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.ReadInstanceResultsResponse
import net.shrine.protocol.ReadInstanceResultsRequest
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.ErrorResponse
import net.shrine.serialization.XmlMarshaller

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
    dao: AdapterDao,
    private val doObfuscation: Boolean) extends Adapter {

  //TODO: Honor doObfuscation flag?
  //TODO: used passed Identity for something?  Check auth{n,z}?
  
  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val req = message.request.asInstanceOf[ReadInstanceResultsRequest]
    
    val networkQueryId = req.shrineNetworkQueryId
    
    StoredQueries.retrieve(dao, req.shrineNetworkQueryId)(ReadInstanceResultsResponse(_, _))
  }
}

