package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity
import net.shrine.adapter.dao.AdapterDao
import net.shrine.config.HiveCredentials
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.ReadQueryResultRequest
import net.shrine.protocol.ReadQueryResultResponse
import net.shrine.serialization.XmlMarshaller
import net.shrine.protocol.ShrineRequest
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.QueryResult


/**
 * @author clint
 * @date Nov 2, 2012
 * 
 */
abstract class AbstractReadQueryResultAdapter[Req <: ShrineRequest, Rsp <: ShrineResponse](
    dao: AdapterDao, 
    doObfuscation: Boolean, 
    getQueryId: Req => Long,
    toResponse: (Long, Seq[QueryResult]) => Rsp) extends Adapter {

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val req = message.request.asInstanceOf[Req]
    
    StoredQueries.retrieve(dao, doObfuscation, getQueryId(req))(toResponse)
  }
}
