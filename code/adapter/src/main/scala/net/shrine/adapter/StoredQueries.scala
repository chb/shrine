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
  private[adapter] def retrieve(dao: AdapterDao, doObfuscation: Boolean, queryId: Long)(makeResponse: (Long, Seq[QueryResult]) => XmlMarshaller): XmlMarshaller = {
    val response = for {
      results <- dao.findResultsFor(queryId)
    } yield {
      makeResponse(queryId, results.toQueryResults(doObfuscation).toSeq)
    }
    
    response.getOrElse(ErrorResponse("Query with id '" + queryId + "' not found"))
  }
}