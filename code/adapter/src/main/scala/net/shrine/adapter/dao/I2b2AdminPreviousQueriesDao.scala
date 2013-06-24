package net.shrine.adapter.dao

import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest

/**
 * @author clint
 * @date Apr 24, 2013
 */
trait I2b2AdminPreviousQueriesDao {
  import ReadI2b2AdminPreviousQueriesRequest.{ SortOrder, Strategy }

  def findQueriesByUserDomainAndSearchString(username: String, searchString: String, howMany: Int, strategy: Strategy, sortOrder: SortOrder): Seq[ShrineQuery]
}