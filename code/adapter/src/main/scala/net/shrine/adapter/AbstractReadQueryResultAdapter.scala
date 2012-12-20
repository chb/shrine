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
import net.shrine.config.HiveCredentials
import net.shrine.util.HttpClient
import net.shrine.protocol.ReadInstanceResultsRequest
import net.shrine.protocol.ReadInstanceResultsResponse
import scala.xml.NodeSeq
import net.shrine.adapter.dao.model.ShrineQueryResult


/**
 * @author clint
 * @date Nov 2, 2012
 * 
 */
abstract class AbstractReadQueryResultAdapter[Req <: ShrineRequest, Rsp <: ShrineResponse](
    crcUrl: String,
    httpClient: HttpClient,
    hiveCredentials: HiveCredentials,
    dao: AdapterDao, 
    doObfuscation: Boolean, 
    getQueryId: Req => Long,
    toResponse: (Long, QueryResult) => Rsp) extends Adapter {

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val req = message.request.asInstanceOf[Req]
    
    val queryId = getQueryId(req)
    
    //TODO:
    /*StoredQueries.retrieve(dao, queryId) match {
      case Some(shrineQueryResult: ShrineQueryResult) => {
        //shrineQueryResult.
      }
      case None => ErrorResponse("Query with id '" + queryId + "' not found")
    }*/
    
    StoredQueries.retrieveAsQueryResult(dao, doObfuscation, queryId) match {
      case Some(queryResult) => {
        //TODO: Replace QueryResult.statusType with an actual enum
        if(queryResult.statusType.isDone) {
          toResponse(queryId, queryResult)
        } else {
          //TODO: This is WRONG, will not get breakdowns, since we don't have their resultIds here. :(
          //Need to get raw results from AdapterDB to get ALL result ids, for count and breakdown results
          val resultRequest = ReadInstanceResultsRequest(req.projectId, req.waitTimeMs, req.authn, queryResult.resultId)
        
          val response = delegateAdapter.processRequest(identity, BroadcastMessage(resultRequest)).asInstanceOf[ReadInstanceResultsResponse]
          
          toResponse(queryId, response.singleNodeResult)
        }
      }
      case None => ErrorResponse("Query with id '" + queryId + "' not found")
    }
  }
  
  private lazy val delegateAdapter = new CrcAdapter[ReadInstanceResultsRequest, ReadInstanceResultsResponse](crcUrl, httpClient, hiveCredentials) {
    override protected def parseShrineResponse(xml: NodeSeq): ShrineResponse = ReadInstanceResultsResponse.fromI2b2(xml)
  }
}
