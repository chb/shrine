package net.shrine.adapter.dao.squeryl

import net.shrine.adapter.dao.squeryl.tables.Tables
import net.shrine.dao.squeryl.SquerylInitializer
import net.shrine.adapter.dao.AdapterDao
import net.shrine.util.Loggable
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.protocol.query.Expression
import org.spin.tools.crypto.signature.Identity
import net.shrine.util.Util
import net.shrine.dao.DateHelpers
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.model.ObfuscatedPair
import net.shrine.adapter.dao.model.ShrineQueryResult
import net.shrine.adapter.dao.model.PrivilegedUser
import org.squeryl.Query
import org.squeryl.dsl.Measures
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.QueryResultRow
import net.shrine.adapter.dao.model.CountRow
import net.shrine.adapter.dao.model.ShrineError
import net.shrine.adapter.dao.model.BreakdownResultRow
import net.shrine.adapter.dao.model.BreakdownResultRow
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.adapter.dao.model.squeryl.SquerylShrineQuery
import net.shrine.adapter.dao.model.squeryl.SquerylQueryResultRow
import net.shrine.adapter.dao.model.squeryl.SquerylCountRow
import net.shrine.adapter.dao.model.squeryl.SquerylBreakdownResultRow
import net.shrine.adapter.dao.model.squeryl.SquerylShrineError

/**
 * @author clint
 * @date May 22, 2013
 */
final class SquerylAdapterDao(initializer: SquerylInitializer, tables: Tables) extends AdapterDao with Loggable {
  initializer.init

  override def inTransaction[T](f: => T): T = SquerylEntryPoint.inTransaction { f }

  import SquerylEntryPoint._

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

    inTransaction {
      val insertedQueryId = insertQuery(masterId, networkQueryId, queryDefinition.name, authn, queryDefinition.expr)

      val insertedQueryResultIds = insertQueryResults(insertedQueryId, rawQueryResults)

      storeCountResults(rawQueryResults, obfuscatedQueryResults, insertedQueryResultIds)

      storeErrorResults(rawQueryResults, insertedQueryResultIds)

      storeBreakdownFailures(failedBreakdownTypes, insertedQueryResultIds)

      insertBreakdownResults(insertedQueryResultIds, mergedBreakdowns, obfuscatedBreakdowns)
    }
  }

  private[adapter] def storeCountResults(raw: Seq[QueryResult], obfuscated: Seq[QueryResult], insertedIds: Map[ResultOutputType, Seq[Int]]) {

    val (errors, notErrors) = raw.partition(_.isError)

    val obfuscatedNotErrors = obfuscated.filter(!_.isError)

    inTransaction {
      //NB: Take the count/setSize from the FIRST QueryResult, though the same count should be there for all of them, if there are more than one
      for {
        Seq(insertedCountQueryResultId) <- insertedIds.get(ResultOutputType.PATIENT_COUNT_XML)
        notError <- notErrors.headOption
        obfuscatedNotError <- obfuscatedNotErrors.headOption
      } {
        insertCountResult(insertedCountQueryResultId, notError.setSize, obfuscatedNotError.setSize)
      }
    }
  }

  private[adapter] def storeErrorResults(results: Seq[QueryResult], insertedIds: Map[ResultOutputType, Seq[Int]]) {

    val (errors, _) = results.partition(_.isError)

    val insertedErrorResultIds = insertedIds.get(ResultOutputType.ERROR).getOrElse(Nil)

    inTransaction {
      for {
        (insertedErrorResultId, errorQueryResult) <- insertedErrorResultIds zip errors
      } {
        insertErrorResult(insertedErrorResultId, errorQueryResult.statusMessage.getOrElse("Unknown failure"))
      }
    }
  }

  private[adapter] def storeBreakdownFailures(failures: Seq[ResultOutputType],
                                              insertedIds: Map[ResultOutputType, Seq[Int]]) {
    inTransaction {
      for {
        failedBreakdownType <- failures
        Seq(resultId) <- insertedIds.get(failedBreakdownType)
      } {
        insertErrorResult(resultId, "Couldn't retrieve breakdown of type '" + failedBreakdownType + "'")
      }
    }
  }

  override def findRecentQueries(howMany: Int): Seq[ShrineQuery] = {
    inTransaction {
      Queries.queriesForAllUsers.take(howMany).map(_.toShrineQuery).toSeq
    }
  }

  override def renameQuery(networkQueryId: Long, newName: String) {
    inTransaction {
      update(tables.shrineQueries) { queryRow =>
        where(queryRow.networkId === networkQueryId).
          set(queryRow.name := newName)
      }
    }
  }

  override def deleteQuery(networkQueryId: Long) {
    inTransaction {
      tables.shrineQueries.deleteWhere(_.networkId === networkQueryId)
    }
  }

  def deleteQueryResultsFor(networkQueryId: Long) {
    inTransaction {
      val resultsForNetworkQueryId = join(tables.shrineQueries, tables.queryResults) { (queryRow, resultRow) =>
        where(queryRow.networkId === networkQueryId).
          select(resultRow).
          on(queryRow.id === resultRow.queryId)
      }

      tables.queryResults.delete(resultsForNetworkQueryId)
    }
  }

  override def isUserLockedOut(id: Identity, defaultThreshold: Int): Boolean = Util.tryOrElse(false) {
    inTransaction {
      val privilegedUserOption = Queries.privilegedUsers(id.getUsername, id.getDomain).singleOption

      val threshold = privilegedUserOption.map(_.threshold).getOrElse(defaultThreshold.intValue)

      val thirtyDaysInThePast = DateHelpers.daysFromNow(-30)

      val overrideDate = privilegedUserOption.flatMap(_.overrideDate).getOrElse(thirtyDaysInThePast)

      val counts = Queries.repeatedResults(id.getUsername, id.getDomain, overrideDate).toSeq.sorted

      val repeatedResultCount = counts.lastOption.getOrElse(0L)

      val result = repeatedResultCount > threshold

      debug(s"User ${id.getDomain}:${id.getUsername} locked out? $result")

      result
    }
  }

  override def insertQuery(localMasterId: String, networkId: Long, name: String, authn: AuthenticationInfo, queryExpr: Expression): Int = {
    inTransaction {
      val inserted = tables.shrineQueries.insert(new SquerylShrineQuery(0, localMasterId, networkId, name, authn.username, authn.domain, queryExpr, Util.now))

      inserted.id
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

    val typeToIdTuples = inTransaction {
      for {
        result <- results
        resultType = result.resultType.getOrElse(ResultOutputType.ERROR)
        //TODO: under what circumstances can QueryResults NOT have start and end dates set?
        elapsed = execTime(result)
      } yield {
        val lastInsertedQueryResultRow = tables.queryResults.insert(new SquerylQueryResultRow(0, result.resultId, parentQueryId, resultType, result.statusType, elapsed, Util.now))

        (resultType, lastInsertedQueryResultRow.id)
      }
    }

    inTransaction {
      typeToIdTuples.groupBy(_._1).mapValues(_.map(_._2))
    }
  }

  override def insertCountResult(resultId: Int, originalCount: Long, obfuscatedCount: Long) {
    //NB: Squeryl steers us toward inserting with dummy ids :(
    inTransaction {
      tables.countResults.insert(new SquerylCountRow(0, resultId, originalCount, obfuscatedCount, Util.now))
    }
  }

  override def insertBreakdownResults(parentResultIds: Map[ResultOutputType, Seq[Int]], originalBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope], obfuscatedBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope]) {
    def merge(original: I2b2ResultEnvelope, obfuscated: I2b2ResultEnvelope): Map[String, ObfuscatedPair] = {
      Map.empty ++ (for {
        (key, originalValue) <- original.data
        obfuscatedValue <- obfuscated.data.get(key)
      } yield (key, ObfuscatedPair(originalValue, obfuscatedValue)))
    }

    inTransaction {
      for {
        (resultType, Seq(resultId)) <- parentResultIds if resultType.isBreakdown
        originalBreakdown <- originalBreakdowns.get(resultType)
        obfuscatedBreakdown <- obfuscatedBreakdowns.get(resultType)
        (key, ObfuscatedPair(original, obfuscated)) <- merge(originalBreakdown, obfuscatedBreakdown)
      } {
        tables.breakdownResults.insert(SquerylBreakdownResultRow(0, resultId, key, original, obfuscated))
      }
    }
  }

  override def insertErrorResult(parentResultId: Int, errorMessage: String) {
    //NB: Squeryl steers us toward inserting with dummy ids :(
    inTransaction {
      tables.errorResults.insert(SquerylShrineError(0, parentResultId, errorMessage))
    }
  }

  override def findQueryByNetworkId(networkQueryId: Long): Option[ShrineQuery] = {
    inTransaction {
      Queries.queriesByNetworkId(networkQueryId).headOption
    }
  }

  override def findQueriesByUserAndDomain(domain: String, username: String, howMany: Int): Seq[ShrineQuery] = {
    inTransaction {
      Queries.queriesForUser(username, domain).take(howMany).toSeq
    }
  }

  override def findResultsFor(networkQueryId: Long): Option[ShrineQueryResult] = {
    inTransaction {
      val breakdownRowsByType = Queries.breakdownResults(networkQueryId).toSeq.groupBy(_._1).mapValues(_.map { case (_, rows) => rows })

      for {
        queryRow <- Queries.queriesByNetworkId(networkQueryId).headOption
        shrineQueryResult <- ShrineQueryResult.fromRows(queryRow, Queries.resultsForQuery(networkQueryId).toSeq, Queries.countResults(networkQueryId).headOption, breakdownRowsByType, Queries.errorResults(networkQueryId).toSeq)
      } yield shrineQueryResult
    }
  }

  //TODO: Remove
  @Deprecated
  override def transactional: AdapterDao = this

  /**
   * @author clint
   * @date Nov 19, 2012
   */
  object Queries {
    def privilegedUsers(username: String, domain: String): Query[PrivilegedUser] = {
      from(tables.privilegedUsers) { user =>
        where(user.username === username and user.domain === domain).select(user.toPrivilegedUser)
      }
    }

    def repeatedResults(username: String, domain: String, overrideDate: XMLGregorianCalendar): Query[Long] = {
      val counts = join(tables.shrineQueries, tables.queryResults, tables.countResults) { (queryRow, resultRow, countRow) =>
        where(queryRow.username === username and queryRow.domain === domain and (countRow.originalValue <> 0L) and queryRow.dateCreated > DateHelpers.toTimestamp(overrideDate)).
          compute(count(countRow.originalValue)).
          on(queryRow.id === resultRow.queryId, resultRow.id === countRow.resultId)
      }

      //Filter for result counts > 1
      from(counts) { cnt =>
        where(cnt.measures gt 1).select(cnt.measures)
      }
    }

    val queriesForAllUsers = {
      from(tables.shrineQueries) { queryRow =>
        select(queryRow).orderBy(queryRow.dateCreated.desc)
      }
    }

    //TODO: Find a way to parameterize on limit, to avoid building the query every time
    //TODO: limit
    def queriesForUser(username: String, domain: String): Query[ShrineQuery] = {
      from(tables.shrineQueries) { queryRow =>
        where(queryRow.domain === domain and queryRow.username === username).select(queryRow.toShrineQuery)
      }
    }

    def queriesByNetworkId(networkQueryId: Long): Query[ShrineQuery] = {
      from(tables.shrineQueries) { queryRow =>
        where(queryRow.networkId === networkQueryId).select(queryRow.toShrineQuery)
      }
    }

    //TODO: Find out how to compose queries, to re-use queriesByNetworkId
    def queryNamesByNetworkId(networkQueryId: Long): Query[String] = {
      from(tables.shrineQueries) { queryRow =>
        where(queryRow.networkId === networkQueryId).select(queryRow.name)
      }
    }

    def resultsForQuery(networkQueryId: Long): Query[QueryResultRow] = {
      val resultsForNetworkQueryId = join(tables.shrineQueries, tables.queryResults) { (queryRow, resultRow) =>
        where(queryRow.networkId === networkQueryId).
          select(resultRow).
          on(queryRow.id === resultRow.queryId)
      }

      from(resultsForNetworkQueryId)(row => select(row.toQueryResultRow))
    }

    def countResults(networkQueryId: Long): Query[CountRow] = {
      join(tables.shrineQueries, tables.queryResults, tables.countResults) { (queryRow, resultRow, countRow) =>
        where(queryRow.networkId === networkQueryId).
          select(countRow.toCountRow).
          on(queryRow.id === resultRow.queryId, resultRow.id === countRow.resultId)
      }
    }

    def errorResults(networkQueryId: Long): Query[ShrineError] = {
      join(tables.shrineQueries, tables.queryResults, tables.errorResults) { (queryRow, resultRow, errorRow) =>
        where(queryRow.networkId === networkQueryId).
          select(errorRow.toShrineError).
          on(queryRow.id === resultRow.queryId, resultRow.id === errorRow.resultId)
      }
    }

    //NB: using groupBy here is too much of a pain; do it 'manually' later
    def breakdownResults(networkQueryId: Long): Query[(ResultOutputType, BreakdownResultRow)] = {
      join(tables.shrineQueries, tables.queryResults, tables.breakdownResults) { (queryRow, resultRow, breakdownRow) =>
        where(queryRow.networkId === networkQueryId).
          select((resultRow.toQueryResultRow.resultType, breakdownRow.toBreakdownResultRow)).
          on(queryRow.id === resultRow.queryId, resultRow.id === breakdownRow.resultId)
      }
    }
  }
}