package net.shrine.adapter.dao.slick

import scala.slick.jdbc.MutatingUnitInvoker
import scala.slick.lifted.Column
import scala.slick.lifted.ColumnOrdered
import scala.slick.lifted.Parameters
import scala.slick.session.Database
import scala.slick.session.Session

import net.shrine.adapter.dao.I2b2AdminPreviousQueriesDao
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.slick.tables.Tables
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest

/**
 * @author clint
 * @date Apr 24, 2013
 */
final class SlickI2b2AdminPreviousQueriesDao(database: Database, val tables: Tables) extends I2b2AdminPreviousQueriesDao {
  import tables._
  import driver.Implicit._
  
  override def findQueriesByUserDomainAndSearchString(domain: String, username: String, searchString: String, howMany: Int, strategy: ReadI2b2AdminPreviousQueriesRequest.Strategy, sortOrder: ReadI2b2AdminPreviousQueriesRequest.SortOrder): Seq[ShrineQuery] = {
    allResults(Queries.queriesForUserAndSearchString(howMany, searchString, matchFunctionFor(strategy), sortFunctionFor(sortOrder))(username, domain))
  }
  
  private def withSession[T](f: Session => T): T = {
    database.withSession { session: Session => f(session) }
  }

  private def allResults[T](queryToRun: MutatingUnitInvoker[T]): Seq[T] = {
    withSession { implicit session => queryToRun.list }
  }
  
  private def matchFunctionFor(strategy: ReadI2b2AdminPreviousQueriesRequest.Strategy): (Column[String], String) => Column[Boolean] = {
    import ReadI2b2AdminPreviousQueriesRequest.Strategy._

    strategy match {
      case Contains => (lhs, rhs) => lhs like s"%$rhs%"
      case Exact => _ === _
      case Left => _ startsWith _
      case Right => _ endsWith _
    }
  }

  private def sortFunctionFor(sortOrder: ReadI2b2AdminPreviousQueriesRequest.SortOrder): ShrineQueries.type => ColumnOrdered[String] = {
    import ReadI2b2AdminPreviousQueriesRequest.SortOrder._

    sortOrder match {
      case Ascending => _.name.asc
      case Descending => _.name.desc
    }
  }
  
  private object Queries {
    //TODO: Find a way to parameterize on limit, to avoid building the query every time
    def queriesForUserAndSearchString(howMany: Int, searchString: String, nameMatches: (Column[String], String) => Column[Boolean], ordering: ShrineQueries.type => ColumnOrdered[String]) = {
      Parameters[(String, String)].flatMap {
        case (username, domain) =>
          (for {
            query <- ShrineQueries
            if query.domain === domain
            if query.username === username
            if nameMatches(query.name, searchString)
          } yield query).sortBy(ordering).take(howMany)
      }
    }
  }
}