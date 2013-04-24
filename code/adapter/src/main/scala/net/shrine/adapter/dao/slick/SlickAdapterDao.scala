package net.shrine.adapter.dao.slick

import scala.slick.jdbc.MutatingUnitInvoker
import scala.slick.lifted.Join
import scala.slick.lifted.Parameters
import scala.slick.lifted.Query
import scala.slick.session.Database
import scala.slick.session.Session

import org.spin.tools.crypto.signature.Identity

import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.AdapterDao
import net.shrine.adapter.dao.model.ObfuscatedPair
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.ShrineQueryResult
import net.shrine.adapter.dao.slick.tables.Tables
import net.shrine.dao.slick.tables.DateHelpers
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.query.Expression
import net.shrine.protocol.query.QueryDefinition
import net.shrine.util.Loggable
import net.shrine.util.Util

/**
 * @author clint
 * @date Oct 15, 2012
 */
final class SlickAdapterDao(database: Database, val tables: Tables) extends AdapterDao with Loggable {
  import tables._
  import driver.Implicit._

  override def inTransaction[T](f: => T): T = database.withTransaction { f }

  override def storeResults(
    authn: AuthenticationInfo,
    masterId: String,
    networkQueryId: Long,
    queryDefinition: QueryDefinition,
    rawQueryResults: Seq[QueryResult],
    obfuscatedQueryResults: Seq[QueryResult],
    failedBreakdownTypes: Seq[ResultOutputType],
    mergedBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope],
    obfuscatedBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope]) {

    val insertedQueryId = insertQuery(masterId, networkQueryId, queryDefinition.name, authn, queryDefinition.expr)

    val insertedQueryResultIds = insertQueryResults(insertedQueryId, rawQueryResults)

    storeCountResults(rawQueryResults, obfuscatedQueryResults, insertedQueryResultIds)

    storeErrorResults(rawQueryResults, insertedQueryResultIds)

    storeBreakdownFailures(failedBreakdownTypes, insertedQueryResultIds)

    insertBreakdownResults(insertedQueryResultIds, mergedBreakdowns, obfuscatedBreakdowns)
  }

  private[adapter] def storeCountResults(raw: Seq[QueryResult], obfuscated: Seq[QueryResult], insertedIds: Map[ResultOutputType, Seq[Int]]) {

    val (errors, notErrors) = raw.partition(_.isError)

    val obfuscatedNotErrors = obfuscated.filter(!_.isError)

    //NB: Take the count/setSize from the FIRST QueryResult, though the same count should be there for all of them, if there are more than one
    for {
      Seq(insertedCountQueryResultId) <- insertedIds.get(ResultOutputType.PATIENT_COUNT_XML)
      notError <- notErrors.headOption
      obfuscatedNotError <- obfuscatedNotErrors.headOption
    } {
      insertCountResult(insertedCountQueryResultId, notError.setSize, obfuscatedNotError.setSize)
    }
  }

  private[adapter] def storeErrorResults(results: Seq[QueryResult], insertedIds: Map[ResultOutputType, Seq[Int]]) {

    val (errors, _) = results.partition(_.isError)

    val insertedErrorResultIds = insertedIds.get(ResultOutputType.ERROR).getOrElse(Nil)

    for {
      (insertedErrorResultId, errorQueryResult) <- insertedErrorResultIds zip errors
    } {
      insertErrorResult(insertedErrorResultId, errorQueryResult.statusMessage.getOrElse("Unknown failure"))
    }
  }

  private[adapter] def storeBreakdownFailures(failures: Seq[ResultOutputType],
                                              insertedIds: Map[ResultOutputType, Seq[Int]]) {
    for {
      failedBreakdownType <- failures
      Seq(resultId) <- insertedIds.get(failedBreakdownType)
    } {
      insertErrorResult(resultId, "Couldn't retrieve breakdown of type '" + failedBreakdownType + "'")
    }
  }

  override def findRecentQueries(howMany: Int): Seq[ShrineQuery] = {
    allResults(Queries.queriesForAllUsers.take(howMany))
  }

  override def renameQuery(networkQueryId: Long, newName: String) {
    withSession { implicit session =>
      Queries.queryNamesByNetworkId(networkQueryId).update(newName)
    }
  }

  override def deleteQuery(networkQueryId: Long) {
    withSession { implicit session =>
      Queries.queriesByNetworkId(networkQueryId).delete
    }
  }

  def deleteQueryResultsFor(networkQueryId: Long) {
    withSession { implicit session =>
      Queries.resultsForQuery(networkQueryId).delete
    }
  }

  override def isUserLockedOut(id: Identity, defaultThreshold: Int): Boolean = Util.tryOrElse(false) {
    val privilegedUserOption = firstResultOption(Queries.privilegedUsers(id.getUsername, id.getDomain))

    val threshold = privilegedUserOption.map(_.threshold).getOrElse(defaultThreshold.intValue)

    val thirtyDaysInThePast = DateHelpers.daysFromNow(-30)

    val overrideDate = privilegedUserOption.flatMap(_.overrideDate).getOrElse(thirtyDaysInThePast)

    val counts = allResults(Queries.repeatedResults(id.getUsername, id.getDomain, overrideDate)).sorted

    val repeatedResultCount = counts.lastOption.getOrElse(0)

    val result = repeatedResultCount > threshold

    debug("User " + id.getDomain + ":" + id.getUsername + " locked out? " + result)

    result
  }

  override def insertQuery(localMasterId: String, networkId: Long, name: String, authn: AuthenticationInfo, queryExpr: Expression): Int = {
    withSession { implicit session =>
      ShrineQueries.inserter.insert(localMasterId, networkId, name, authn.username, authn.domain, queryExpr)
    }
  }

  /**
   * Insert rows into QueryResults, one for each QueryResult in the passed RunQueryResponse
   * Inserted rows are 'children' of the passed ShrineQuery (ie, they are the results of the query)
   */
  override def insertQueryResults(parentQueryId: Int, results: Seq[QueryResult]): Map[ResultOutputType, Seq[Int]] = {
    def execTime(result: QueryResult): Option[Long] = {
      //TODO: How are locales handled here?  Do we care?
      def toMillis(xmlGc: XMLGregorianCalendar) = xmlGc.toGregorianCalendar.getTimeInMillis

      for {
        start <- result.startDate
        end <- result.endDate
      } yield toMillis(end) - toMillis(start)
    }

    import QueryResult.StatusType

    withSession { implicit session =>
      val typeToIdTuples = for {
        result <- results
        resultType = result.resultType.getOrElse(ResultOutputType.ERROR)
        //TODO: under what circumstances can QueryResults NOT have start and end dates set?
        elapsed = execTime(result)
      } yield {
        val lastInsertedQueryResultId = QueryResults.inserter.insert(result.resultId, parentQueryId, resultType, result.statusType, elapsed)

        (resultType, lastInsertedQueryResultId)
      }

      typeToIdTuples.groupBy(_._1).mapValues(_.map(_._2))
    }
  }

  override def insertCountResult(resultId: Int, originalCount: Long, obfuscatedCount: Long) {
    withSession { implicit session =>
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

    withSession { implicit session =>
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
    withSession { implicit session =>
      ErrorResults.withoutId.insert(parentResultId, errorMessage)
    }
  }

  override def findQueryByNetworkId(networkQueryId: Long): Option[ShrineQuery] = {
    firstResultOption(Queries.queriesByNetworkId(networkQueryId))
  }

  override def findQueriesByUserAndDomain(domain: String, username: String, howMany: Int): Seq[ShrineQuery] = {
    allResults(Queries.queriesForUser(howMany)(username, domain))
  }

  override def findResultsFor(networkQueryId: Long): Option[ShrineQueryResult] = {
    val breakdownRowsByType = allResults(Queries.breakdownResults(networkQueryId)).groupBy(_._1).mapValues(_.map { case (_, rows) => rows })

    for {
      queryRow <- firstResultOption(Queries.queriesByNetworkId(networkQueryId))
      shrineQueryResult <- ShrineQueryResult.fromRows(queryRow, allResults(Queries.resultsForQuery(networkQueryId)), firstResultOption(Queries.countResults(networkQueryId)), breakdownRowsByType, allResults(Queries.errorResults(networkQueryId)))
    } yield shrineQueryResult

  }

  private def withSession[T](f: Session => T): T = {
    database.withSession { session: Session => f(session) }
  }

  private def firstResultOption[T](queryToRun: MutatingUnitInvoker[T]): Option[T] = {
    withSession { implicit session => queryToRun.firstOption }
  }

  private def firstResultOption[A, B](queryToRun: Query[A, B]): Option[B] = {
    withSession { implicit session => queryToRun.firstOption }
  }

  private def allResults[T](queryToRun: MutatingUnitInvoker[T]): Seq[T] = {
    withSession { implicit session => queryToRun.list }
  }

  override def transactional: AdapterDao = new AdapterDao {
    private val outer = SlickAdapterDao.this

    override def insertQuery(localMasterId: String, networkId: Long, name: String, authn: AuthenticationInfo, queryExpr: Expression): Int = {
      database.withTransaction(outer.insertQuery(localMasterId, networkId, name, authn, queryExpr))
    }

    override def insertQueryResults(parentQueryId: Int, results: Seq[QueryResult]): Map[ResultOutputType, Seq[Int]] = {
      database.withTransaction(outer.insertQueryResults(parentQueryId, results))
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

    override def findQueriesByUserAndDomain(domain: String, username: String, howMany: Int): Seq[ShrineQuery] = {
      database.withTransaction(outer.findQueriesByUserAndDomain(domain, username, howMany))
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

    override def storeResults(authn: AuthenticationInfo,
                              masterId: String,
                              networkQueryId: Long,
                              queryDefinition: QueryDefinition,
                              rawQueryResults: Seq[QueryResult],
                              obfuscatedQueryResults: Seq[QueryResult],
                              failedBreakdownTypes: Seq[ResultOutputType],
                              mergedBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope],
                              obfuscatedBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope]) {

      database.withTransaction(outer.storeResults(authn, masterId, networkQueryId, queryDefinition, rawQueryResults, obfuscatedQueryResults, failedBreakdownTypes, mergedBreakdowns, obfuscatedBreakdowns))
    }
  }

  /**
   * @author clint
   * @date Nov 19, 2012
   */
  object Queries {
    val privilegedUsers = for {
      (username, domain) <- Parameters[(String, String)]
      row <- PrivilegedUsers
      if row.username === username && row.domain === domain
    } yield row

    val repeatedResults = {
      import DateHelpers.Implicit._

      //Whacky (whackier?) syntax required as of Slick 1.0 when using aggregate functions and parameters :(
      //See https://groups.google.com/forum/?fromgroups=#!searchin/scalaquery/parameters/scalaquery/os770ik5MmU/z9FOS8SQKkkJ
      //And https://groups.google.com/forum/?fromgroups=#!topic/scalaquery/hUz1S_XgkrQ
      Parameters[(String, String, XMLGregorianCalendar)].flatMap {
        case (username, domain, overrideDate) =>
          (for {
            queryRow <- ShrineQueries if queryRow.username === username && queryRow.domain === domain
            resultRow <- QueryResults if resultRow.queryId === queryRow.id
            countRow <- CountResults if countRow.resultId === resultRow.id
            if countRow.originalCount =!= 0L
            if queryRow.creationDate > overrideDate
          } yield countRow.originalCount.count).filter(_ > 1)
      }
    }

    val queriesForAllUsers = {
      import DateHelpers.Implicit._

      Query(ShrineQueries).sortBy(_.creationDate.desc)
    }

    //TODO: Find a way to parameterize on limit, to avoid building the query every time
    def queriesForUser(limit: Int) = {
      Parameters[(String, String)].flatMap {
        case (username, domain) =>
          (for {
            query <- ShrineQueries
            if query.domain === domain
            if query.username === username
          } yield query.*).take(limit)
      }
    }

    def queriesByNetworkId(networkQueryId: Long) = Query(ShrineQueries).filter(_.networkId === networkQueryId)

    def queryNamesByNetworkId(networkQueryId: Long) = queriesByNetworkId(networkQueryId).map(_.name)

    def resultsForQuery(networkQueryId: Long) = {
      val queriesJoinedWithResults = ShrineQueries.innerJoin(QueryResults).on(_.id === _.queryId)

      val joinedRowsWithNetworkQueryId = queriesJoinedWithResults.filter {
        case (shrineQueryRow, _) =>
          shrineQueryRow.networkId === networkQueryId
      }

      joinedRowsWithNetworkQueryId.map { case Join(_, right) => right }
    }

    val countResults = for {
      networkQueryId <- Parameters[Long]
      query <- ShrineQueries if query.networkId === networkQueryId
      qr <- QueryResults if qr.queryId === query.id
      countRow <- CountResults if countRow.resultId === qr.id
    } yield countRow

    val errorResults = for {
      networkQueryId <- Parameters[Long]
      q <- ShrineQueries if q.networkId === networkQueryId
      qr <- QueryResults if qr.queryId === q.id
      errorRow <- ErrorResults if errorRow.resultId === qr.id
    } yield errorRow

    val breakdownResults = for {
      networkQueryId <- Parameters[Long]
      q <- ShrineQueries if q.networkId === networkQueryId
      qr <- QueryResults if qr.queryId === q.id
      breakdownRow <- BreakdownResults if breakdownRow.resultId === qr.id
      //NB: using groupBy here is too much of a pain; do it 'manually' later
    } yield (qr.resultType, breakdownRow)
  }
}
