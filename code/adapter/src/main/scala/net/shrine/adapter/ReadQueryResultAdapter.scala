package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity
import net.shrine.adapter.dao.AdapterDao
import net.shrine.config.HiveCredentials
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.ReadQueryResultRequest
import net.shrine.protocol.ReadQueryResultResponse
import net.shrine.serialization.XmlMarshaller
import net.shrine.util.HttpClient


/**
 * @author clint
 * @date Nov 2, 2012
 * 
 */
//TODO: TEST!!!
final class ReadQueryResultAdapter(
    crcUrl: String,
    httpClient: HttpClient,
    hiveCredentials: HiveCredentials,
    dao: AdapterDao, 
    doObfuscation: Boolean) extends 
    	AbstractReadQueryResultAdapter[ReadQueryResultRequest, ReadQueryResultResponse](
    	    crcUrl, 
    	    httpClient,
    	    hiveCredentials,
    		dao,
    		doObfuscation,
    		_.queryId,
    		ReadQueryResultResponse(_, _))

