package net.shrine.adapter

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
import net.shrine.util.HttpClient

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
    crcUrl: String,
    httpClient: HttpClient,
    hiveCredentials: HiveCredentials,
    dao: AdapterDao, 
    doObfuscation: Boolean) extends 
    	AbstractReadQueryResultAdapter[ReadInstanceResultsRequest, ReadInstanceResultsResponse](
    	    crcUrl,
    	    httpClient,
    	    hiveCredentials,
    		dao,
    		doObfuscation,
    		_.shrineNetworkQueryId,
    		ReadInstanceResultsResponse(_, _))

