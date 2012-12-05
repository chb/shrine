package net.shrine.adapter

import net.shrine.serialization.XmlMarshaller
import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ErrorResponse

/**
 * @author clint
 * @date Nov 6, 2012
 */
object StoredQueries {
  private[adapter] def retrieve(dao: AdapterDao, doObfuscation: Boolean, queryId: Long)(makeResponse: (Long, QueryResult) => XmlMarshaller): XmlMarshaller = {
    val response = for {
      shrineResults <- dao.findResultsFor(queryId)
      queryResult <- shrineResults.toQueryResults(doObfuscation)
    } yield {
      makeResponse(queryId, queryResult)
    }
    
    response.getOrElse(ErrorResponse("Query with id '" + queryId + "' not found"))
  }
}