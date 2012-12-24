package net.shrine.adapter.dao.scalaquery

import java.util.Calendar
import org.scalaquery.MutatingUnitInvoker
import org.scalaquery.ql.Join
import org.scalaquery.ql.Parameters
import org.scalaquery.ql.Query
import org.scalaquery.ql.extended.ExtendedProfile
import org.scalaquery.session.Database
import org.scalaquery.session.Session
import org.spin.tools.NetworkTime
import org.spin.tools.crypto.signature.Identity
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.AdapterDao
import net.shrine.adapter.dao.model.ObfuscatedPair
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.ShrineQueryResult
import net.shrine.adapter.dao.scalaquery.tables.BreakdownResults
import net.shrine.adapter.dao.scalaquery.tables.CountResults
import net.shrine.adapter.dao.scalaquery.tables.ErrorResults
import net.shrine.adapter.dao.scalaquery.tables.PrivilegedUsers
import net.shrine.adapter.dao.scalaquery.tables.QueryResults
import net.shrine.adapter.dao.scalaquery.tables.ShrineQueries
import net.shrine.adapter.dao.scalaquery.tables.DateHelpers
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.QueryResult
import net.shrine.protocol.QueryResult.StatusType
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.query.Expression
import net.shrine.util.Util
import net.shrine.util.Loggable
import net.shrine.protocol.RawCrcRunQueryResponse

/**
 * @author clint
 * @date Oct 15, 2012
 */
final class ScalaQueryAdapterDao(database: Database, driver: ExtendedProfile, sequenceHelper: SequenceHelper) extends AdapterDao with Loggable {
  import driver.Implicit._

  override def findRecentQueries(howMany: Int): Seq[ShrineQuery] = {
    allResults(Queries.queriesForAllUsers.take(howMany))
  }

  override def renameQuery(networkQueryId: Long, newName: String) {
    //TODO: why can't we use a parameterized query here?
    val updateQuery = for {
      query <- ShrineQueries
      if query.networkId === networkQueryId
    } yield query.name

    database.withSession { implicit session: Session =>
      updateQuery.update(newName)
    }
  }

  override def deleteQuery(networkQueryId: Long) {
    database.withSession { implicit session: Session =>
      Queries.queriesByNetworkId(networkQueryId).mutate(_.delete())
    }
  }
  
  def deleteQueryResultsFor(networkQueryId: Long) {
    database.withSession { implicit session: Session =>
      //TODO: Find another way besides .mutate(_.delete()) here;
      //apparently this relies on a slow and potentially fragile
      //JDBC ResultSet API, instead of generating DELETE FROM ...
      //SQL.  However, it appears nothing else works with parameterized
      //queries. :\
      Queries.resultsForQuery(networkQueryId).mutate(_.delete())
    }
  }

  override def isUserLockedOut(id: Identity, defaultThreshold: Int): Boolean = Util.tryOrElse(false) {
    val privilegedUserOption = firstResultOption(Queries.privilegedUsers(id.getUsername, id.getDomain))

    val threshold = privilegedUserOption.map(_.threshold).getOrElse(defaultThreshold.intValue)

    val thirtyDaysInThePast = DateHelpers.daysFromNow(-30)

    val overrideDate = privilegedUserOption.map(_.overrideDate).getOrElse(thirtyDaysInThePast)

    val counts = allResults(Queries.repeatedResults(id.getUsername, id.getDomain, overrideDate)).sorted

    val repeatedResultCount = counts.lastOption.getOrElse(0)

    val result = repeatedResultCount > threshold
    
    debug("User " + id.getDomain + ":" + id.getUsername + " locked out? " + result)
    
    result
  }

  override def insertQuery(localMasterId: String, networkId: Long, name: String, authn: AuthenticationInfo, queryExpr: Expression): Int = {
    database.withSession { implicit session: Session =>
      ShrineQueries.withoutGeneratedColumns.insert(localMasterId, networkId, name, authn.username, authn.domain, queryExpr)

      sequenceHelper.lastInsertedQueryId
    }
  }

  /**
   * Insert rows into QueryResults, one for each QueryResult in the passed RunQueryResponse
   * Inserted rows are 'children' of the passed ShrineQuery (ie, they are the results of the query)
   */
  override def insertQueryResults(parentQueryId: Int, response: RawCrcRunQueryResponse): Map[ResultOutputType, Seq[Int]] = {
    //TODO: Is there a better way?  Is the elapsed time available somewhere?
    def execTime(result: QueryResult): Option[Long] = {
      //TODO: How are locales handled here?  Do we care?
      def toMillis(xmlGc: XMLGregorianCalendar) = xmlGc.toGregorianCalendar.getTimeInMillis

      for {
        start <- result.startDate
        end <- result.endDate
      } yield toMillis(end) - toMillis(start)
    }

    import QueryResult.StatusType

    database.withSession { implicit session: Session =>
      val typeToIdTuples = for {
        result <- response.results
        resultType = result.resultType.getOrElse(ResultOutputType.ERROR)
        //TODO: under what circumstances can QueryResults NOT have start and end dates set?
        elapsed = execTime(result)
      } yield {
        QueryResults.withoutGeneratedColumns.insert(result.resultId, parentQueryId, resultType, result.statusType, elapsed)

        (resultType, sequenceHelper.lastInsertedQueryResultId)
      }

      typeToIdTuples.groupBy(_._1).mapValues(_.map(_._2))
    }
  }

  override def insertCountResult(resultId: Int, originalCount: Long, obfuscatedCount: Long) {
    database.withSession { implicit session: Session =>
      CountResults.withoutGeneratedColumns.insert(resultId, originalCount, obfuscatedCount)
    }
  }

  override def insertBreakdownResults(parentResultIds: Map[ResultOutputType, Seq[Int]], originalBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope], obfuscatedBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope]) {
    def merge(original: I2b2ResultEnvelope, obfuscated: I2b2ResultEnvelope): Map[String, ObfuscatedPair] = {
      Map.empty ++ (for {
        (key, originalValue) <- original.data
        obfuscatedValue <- obfuscated.data.get(key)
      } yield (key, ObfuscatedPair(originalValue, obfuscatedValue)))
    }

    database.withSession { implicit session: Session =>
      for {
        (resultType, Seq(resultId)) <- parentResultIds if resultType.isBreakdown
        originalBreakdown <- originalBreakdowns.get(resultType)
        obfuscatedBreakdown <- obfuscatedBreakdowns.get(resultType)
        (key, ObfuscatedPair(original, obfuscated)) <- merge(originalBreakdown, obfuscatedBreakdown)
      } {
        BreakdownResults.withoutId.insert(resultId, key, original, obfuscated)
      }
    }
  }

  override def insertErrorResult(parentResultId: Int, errorMessage: String) {
    database.withSession { implicit session: Session =>
      ErrorResults.withoutId.insert(parentResultId, errorMessage)
    }
  }

  override def findQueryByNetworkId(networkQueryId: Long): Option[ShrineQuery] = {
    firstResultOption(Queries.queriesByNetworkId(networkQueryId))
  }

  override def findQueriesByUserAndDomain(domain: String, username: String): Seq[ShrineQuery] = {
    allResults(Queries.queriesForUser(username, domain))
  }

  override def findResultsFor(networkQueryId: Long): Option[ShrineQueryResult] = {
    val breakdownRowsByType = allResults(Queries.breakdownResults(networkQueryId)).groupBy(_._1).mapValues(_.map { case (_, rows) => rows })

    for { 
      queryRow <- firstResultOption(Queries.queriesByNetworkId(networkQueryId))
      shrineQueryResult <- ShrineQueryResult.fromRows(queryRow, allResults(Queries.resultsForQuery(networkQueryId)), firstResultOption(Queries.countResults(networkQueryId)), breakdownRowsByType, allResults(Queries.errorResults(networkQueryId)))
    } yield shrineQueryResult
  }

  private def firstResultOption[T](queryToRun: MutatingUnitInvoker[T]): Option[T] = {
    database.withSession { implicit session: Session => queryToRun.firstOption }
  }

  private def allResults[T](queryToRun: MutatingUnitInvoker[T]): Seq[T] = {
    database.withSession { implicit session: Session => queryToRun.list }
  }

  def transactional: AdapterDao = new AdapterDao {
    private val outer = ScalaQueryAdapterDao.this

    override def insertQuery(localMasterId: String, networkId: Long, name: String, authn: AuthenticationInfo, queryExpr: Expression): Int = {
      database.withTransaction(outer.insertQuery(localMasterId, networkId, name, authn, queryExpr))
    }

    override def insertQueryResults(parentQueryId: Int, response: RawCrcRunQueryResponse): Map[ResultOutputType, Seq[Int]] = {
      database.withTransaction(outer.insertQueryResults(parentQueryId, response))
    }

    override def insertCountResult(resultId: Int, originalCount: Long, obfuscatedCount: Long) {
      database.withTransaction(outer.insertCountResult(resultId, originalCount, obfuscatedCount))
    }

    override def insertBreakdownResults(parentResultIds: Map[ResultOutputType, Seq[Int]], originalBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope], obfuscatedBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope]) {
      database.withTransaction(outer.insertBreakdownResults(parentResultIds, originalBreakdowns, obfuscatedBreakdowns))
    }

    override def insertErrorResult(parentResultId: Int, errorMessage: String) {
      database.withTransaction(outer.insertErrorResult(parentResultId, errorMessage))
    }

    override def findQueriesByUserAndDomain(domain: String, username: String): Seq[ShrineQuery] = {
      database.withTransaction(outer.findQueriesByUserAndDomain(domain, username))
    }

    override def findQueryByNetworkId(networkQueryId: Long): Option[ShrineQuery] = {
      database.withTransaction(outer.findQueryByNetworkId(networkQueryId))
    }

    override def findResultsFor(networkQueryId: Long): Option[ShrineQueryResult] = {
      database.withTransaction(outer.findResultsFor(networkQueryId))
    }

    override def isUserLockedOut(id: Identity, defaultThreshold: Int): Boolean = {
      database.withTransaction(outer.isUserLockedOut(id, defaultThreshold))
    }

    override def renameQuery(networkQueryId: Long, newName: String) {
      database.withTransaction(outer.renameQuery(networkQueryId, newName))
    }

    override def deleteQuery(networkQueryId: Long) {
      database.withTransaction(outer.deleteQuery(networkQueryId))
    }

    override def findRecentQueries(howMany: Int): Seq[ShrineQuery] = {
      database.withTransaction(outer.findRecentQueries(howMany))
    }
  }

  /**
   * @author clint
   * @date Nov 19, 2012
   */
  object Queries {
    import org.scalaquery.ql._

    val privilegedUsers = for {
      username ~ domain <- Parameters[String, String]
      row <- PrivilegedUsers
      if row.username === username && row.domain === domain
    } yield row.*

    val repeatedResults = {
      import DateHelpers.Implicit._

      for {
        username ~ domain ~ overrideDate <- Parameters[String, String, XMLGregorianCalendar]
        queryRow <- ShrineQueries if queryRow.username === username && queryRow.domain === domain
        resultRow <- QueryResults if resultRow.queryId === queryRow.id
        countRow <- CountResults if countRow.resultId === resultRow.id
        _ <- Query groupBy countRow.originalCount
        if countRow.originalCount =!= 0L
        if queryRow.creationDate > overrideDate
      } yield countRow.originalCount.count
    }

    val queriesForAllUsers = {
      import DateHelpers.Implicit._
      
      for {
        query <- ShrineQueries
        _ <- Query.orderBy(query.creationDate.desc)
      } yield query.*
    }

    val queriesForUser = for {
      username ~ domain <- Parameters[String, String]
      query <- ShrineQueries
      if query.domain === domain //NB: Note triple-equals
      if query.username === username //NB: Note triple-equals
    } yield query.*

    val queriesByNetworkId = for {
      networkQueryId <- Parameters[Long]
      query <- ShrineQueries
      if query.networkId === networkQueryId //NB: Note triple-equals
    } yield query.*

    val queryNamesByNetworkId = for {
      networkQueryId <- Parameters[Long]
      query <- ShrineQueries
      if query.networkId === networkQueryId //NB: Note triple-equals
    } yield query.name

    val resultsForQuery = for {
      networkQueryId <- Parameters[Long]
      Join(_, right) <- ShrineQueries.innerJoin(QueryResults).on(_.id === _.queryId).filter(_.left.networkId === networkQueryId)
    } yield right.*

    val countResults = for {
      networkQueryId <- Parameters[Long]
      query <- ShrineQueries if query.networkId === networkQueryId
      qr <- QueryResults if qr.queryId === query.id
      countRow <- CountResults if countRow.resultId === qr.id
    } yield countRow.*

    val errorResults = for {
      networkQueryId <- Parameters[Long]
      q <- ShrineQueries if q.networkId === networkQueryId
      qr <- QueryResults if qr.queryId === q.id
      errorRow <- ErrorResults if errorRow.resultId === qr.id
    } yield errorRow.*

    val breakdownResults = for {
      networkQueryId <- Parameters[Long]
      q <- ShrineQueries if q.networkId === networkQueryId
      qr <- QueryResults if qr.queryId === q.id
      breakdownRow <- BreakdownResults if breakdownRow.resultId === qr.id
      //_ <- Query groupBy qr.resultType
    } yield (qr.resultType, breakdownRow.*) //TODO: Way to say 'group by' here?
  }
}
