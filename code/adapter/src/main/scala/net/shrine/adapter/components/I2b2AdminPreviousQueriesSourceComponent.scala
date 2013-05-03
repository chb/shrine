package net.shrine.adapter.components

import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ShrineResponse
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.adapter.dao.I2b2AdminPreviousQueriesDao

/**
 * @author clint
 * @date Apr 4, 2013
 */
trait I2b2AdminPreviousQueriesSourceComponent {
  def i2b2AdminDao: I2b2AdminPreviousQueriesDao

  object I2b2AdminPreviousQueries {
    def get(request: ReadI2b2AdminPreviousQueriesRequest): ShrineResponse = {
      val queries = i2b2AdminDao.findQueriesByUserDomainAndSearchString(request.authn.domain, request.authn.username, request.searchString, request.maxResults, request.searchStrategy, request.sortOrder)
      
      //TODO: Category BS
      
      ReadPreviousQueriesResponse(Option(request.authn.username), Option(request.authn.domain), queries.map(_.toQueryMaster(_.localId)))
    }
  }
}