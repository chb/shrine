package net.shrine.adapter.dao.squeryl

import org.squeryl.Query
import org.squeryl.dsl.ast.BinaryOperatorNodeLogicalBoolean
import org.squeryl.dsl.ast.OrderByArg

import net.shrine.adapter.dao.I2b2AdminPreviousQueriesDao
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.squeryl.SquerylShrineQuery
import net.shrine.adapter.dao.squeryl.tables.Tables
import net.shrine.dao.squeryl.SquerylInitializer
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest.SortOrder
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest.Strategy

/**
 * @author clint
 * @date Apr 24, 2013
 */
final class SquerylI2b2AdminPreviousQueriesDao(initializer: SquerylInitializer, val tables: Tables) extends I2b2AdminPreviousQueriesDao {
  initializer.init
  
  import SquerylEntryPoint._
  
  override def findQueriesByUserDomainAndSearchString(username: String, searchString: String, howMany: Int, strategy: ReadI2b2AdminPreviousQueriesRequest.Strategy, sortOrder: ReadI2b2AdminPreviousQueriesRequest.SortOrder): Seq[ShrineQuery] = {
    inTransaction {
      Queries.queriesForUserAndSearchString(username, searchString, matchFunctionFor(strategy), sortFunctionFor(sortOrder)).take(howMany).toSeq
    }
  }
  
  private def matchFunctionFor(strategy: ReadI2b2AdminPreviousQueriesRequest.Strategy): (String, String) => BinaryOperatorNodeLogicalBoolean = {
    import ReadI2b2AdminPreviousQueriesRequest.Strategy._

    strategy match {
      case Contains => (lhs, rhs) => lhs like s"%$rhs%"
      case Exact => _ === _
      case Left => (lhs, rhs) => lhs like s"$rhs%"
      case Right => (lhs, rhs) => lhs like s"%$rhs"
    }
  }

  private def sortFunctionFor(sortOrder: ReadI2b2AdminPreviousQueriesRequest.SortOrder): SquerylShrineQuery => OrderByArg = {
    import ReadI2b2AdminPreviousQueriesRequest.SortOrder._

    sortOrder match {
      case Ascending => _.name.asc
      case Descending => _.name.desc
    }
  }
  
  private object Queries {
    import ReadI2b2AdminPreviousQueriesRequest.SortOrder
    
    def queriesForUserAndSearchString(username: String, searchString: String, nameMatches: (String, String) => BinaryOperatorNodeLogicalBoolean, ordering: SquerylShrineQuery => OrderByArg): Query[ShrineQuery] = {
      from(tables.shrineQueries) { query =>
        where({
          val nameMatchPredicate = nameMatches(query.name, searchString)
          
          if(username != ReadI2b2AdminPreviousQueriesRequest.allUsers) { 
            query.username === username and nameMatchPredicate 
          } else { nameMatchPredicate }
        }).
        select(query.toShrineQuery).
        orderBy(ordering(query))
      }
    }
  }
}