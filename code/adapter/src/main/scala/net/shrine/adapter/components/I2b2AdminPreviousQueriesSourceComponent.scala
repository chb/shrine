package net.shrine.adapter.components

import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ShrineResponse
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.protocol.ReadPreviousQueriesResponse

/**
 * @author clint
 * @date Apr 4, 2013
 */
trait I2b2AdminPreviousQueriesSourceComponent {
  val dao: AdapterDao

  protected object I2b2AdminPreviousQueries {
    def get(request: ReadI2b2AdminPreviousQueriesRequest): ShrineResponse = {
      val queries = for {
        query <- dao.findQueriesByUserAndDomain(request.authn.domain, request.authn.domain, request.maxResults)
        if request.searchStrategy.isMatch(query.name, request.searchString)
      } yield query
      
      import ReadI2b2AdminPreviousQueriesRequest.SortOrder._
      
      //TODO: Sort by something else?  Date?
      val sortedQueries = queries.sortWith(request.sortOrder match {
        case Ascending => _.name < _.name
        case Descending => _.name > _.name
      })
      
      //TODO: Category BS
      
      ReadPreviousQueriesResponse(request.authn.username, request.authn.domain, sortedQueries.map(_.toQueryMaster))
    }
  }
}