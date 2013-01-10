package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.{ BroadcastMessage, RunQueryResponse, RunQueryRequest, RawCrcRunQueryResponse }
import xml.NodeSeq
import net.shrine.adapter.translators.QueryDefinitionTranslator
import net.shrine.config.{ HiveCredentials, ShrineConfig }
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ReadResultRequest
import net.shrine.protocol.ReadResultResponse
import net.shrine.protocol.RunQueryRequest
import net.shrine.protocol.RunQueryRequest
import net.shrine.protocol.AsRunQueryRequest
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.util.Try
import net.shrine.util.Failure
import net.shrine.util.Loggable
import net.shrine.util.Success
import net.shrine.util.HttpClient
import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.AuthenticationInfo

/**
 * @author Bill Simons
 * @author clint
 * @date 4/15/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class RunQueryAdapter(
    crcUrl: String,
    httpClient: HttpClient,
    dao: AdapterDao,
    override protected val hiveCredentials: HiveCredentials,
    private[adapter] val conceptTranslator: QueryDefinitionTranslator,
    config: ShrineConfig,
    doObfuscation: Boolean) extends CrcAdapter[RunQueryRequest, RunQueryResponse](crcUrl, httpClient, hiveCredentials) {

  import RunQueryAdapter._

  override protected def parseShrineResponse(nodeSeq: NodeSeq) = RawCrcRunQueryResponse.fromI2b2(nodeSeq)

  override protected[adapter] def translateNetworkToLocal(request: RunQueryRequest): RunQueryRequest = {
    request.mapQueryDefinition(conceptTranslator.translate)
  }

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): RunQueryResponse = {
    if (isLockedOut(identity)) {
      throw new AdapterLockoutException(identity)
    }

    val runQueryReq = message.request.asInstanceOf[RunQueryRequest]

    val rawRunQueryResponse = super.processRequest(identity, message).asInstanceOf[RawCrcRunQueryResponse]

    val obfuscatedQueryResults = obfuscateResults(rawRunQueryResponse.results)

    def isBreakdown(result: QueryResult) = result.resultType.map(_.isBreakdown).getOrElse(false)

    val (breakdownResults, nonBreakDownResults) = obfuscatedQueryResults.partition(isBreakdown)

    val attemptsWithBreakDownCounts = attemptToRetrieveBreakdowns(runQueryReq, breakdownResults)

    val (successes, failures) = attemptsWithBreakDownCounts.partition { case (_, t) => t.isSuccess }

    logBreakdownFailures(rawRunQueryResponse, failures)

    val (mergedBreakdowns, obfuscatedBreakdowns) = {
      val withBreakdownCounts = successes.collect { case (_, Success(queryResultWithBreakdowns)) => queryResultWithBreakdowns }

      val mergedBreakdowns = withBreakdownCounts.map(_.breakdowns).fold(Map.empty)(_ ++ _)

      val obfuscatedBreakdowns = obfuscateBreakdowns(mergedBreakdowns)

      (mergedBreakdowns, obfuscatedBreakdowns)
    }

    storeResults(runQueryReq.authn, rawRunQueryResponse.queryId.toString, runQueryReq.networkQueryId, runQueryReq.queryDefinition, rawRunQueryResponse.results, obfuscatedQueryResults, failures, mergedBreakdowns, obfuscatedBreakdowns)

    //TODO: Will fail in the case of NO non-breakdown QueryResults.  Can this ever happen, and is it worth protecting against here?
    val resultWithMergedBreakdowns = nonBreakDownResults.head.withBreakdowns(mergedBreakdowns)

    rawRunQueryResponse.toRunQueryResponse.withResult(resultWithMergedBreakdowns)
  }

  private[adapter] def storeResults(authn: AuthenticationInfo,
		  							masterId: String,
		  							networkQueryId: Long,
		  							queryDefinition: QueryDefinition,
                                    rawQueryResults: Seq[QueryResult],
                                    obfuscatedQueryResults: Seq[QueryResult],
                                    breakdownFailures: Seq[(QueryResult, Try[QueryResult])],
                                    mergedBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope],
                                    obfuscatedBreakdowns: Map[ResultOutputType, I2b2ResultEnvelope]) {
    dao.inTransaction {
      val insertedQueryId = dao.insertQuery(masterId, networkQueryId, queryDefinition.name, authn, queryDefinition.expr)

      val insertedQueryResultIds = dao.insertQueryResults(insertedQueryId, rawQueryResults)

      storeCountResults(rawQueryResults, obfuscatedQueryResults, insertedQueryResultIds)

      storeErrorResults(rawQueryResults, insertedQueryResultIds)

      storeBreakdownFailures(breakdownFailures, insertedQueryResultIds)

      dao.insertBreakdownResults(insertedQueryResultIds, mergedBreakdowns, obfuscatedBreakdowns)
    }
  }

  private[adapter] def storeCountResults(insertedCountQueryResultId: Int, origQueryResult: QueryResult, obfscQueryResult: QueryResult) {
    dao.insertCountResult(insertedCountQueryResultId, origQueryResult.setSize, obfscQueryResult.setSize)
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
      storeCountResults(insertedCountQueryResultId, notError, obfuscatedNotError)
    }
  }

  private[adapter] def storeErrorResults(results: Seq[QueryResult], insertedIds: Map[ResultOutputType, Seq[Int]]) {
    
    val (errors, _) = results.partition(_.isError)

    val insertedErrorResultIds = insertedIds.get(ResultOutputType.ERROR).getOrElse(Nil)

    for {
      (insertedErrorResultId, errorQueryResult) <- insertedErrorResultIds zip errors
    } {
      dao.insertErrorResult(insertedErrorResultId, errorQueryResult.statusMessage.getOrElse("Unknown failure"))
    }
  }

  private[adapter] def attemptToRetrieveBreakdowns(runQueryReq: RunQueryRequest, breakdownResults: Seq[QueryResult]): Seq[(QueryResult, Try[QueryResult])] = {
    def readResultRequest(runQueryReq: RunQueryRequest, networkResultId: Long) = ReadResultRequest(hiveCredentials.project, runQueryReq.waitTimeMs, hiveCredentials.toAuthenticationInfo, networkResultId.toString)

    breakdownResults.map { origBreakdownResult =>
      (origBreakdownResult, Try {
        val respXml = callCrc(readResultRequest(runQueryReq, origBreakdownResult.resultId))

        val breakdownData = ReadResultResponse.fromI2b2(respXml).data

        origBreakdownResult.withBreakdown(breakdownData)
      })
    }
  }

  private[adapter] def logBreakdownFailures(response: RawCrcRunQueryResponse,
                                            failures: Seq[(QueryResult, Try[QueryResult])]) {
    for {
      (origQueryResult, Failure(e)) <- failures
    } {
      error("Couldn't load breakdown for QueryResult with masterId: " + response.queryId + ", instanceId: " + origQueryResult.instanceId + ", resultId: " + origQueryResult.resultId + ". Asked for result type: " + origQueryResult.resultType, e)
    }
  }

  private[adapter] def storeBreakdownFailures(failures: Seq[(QueryResult, Try[QueryResult])],
                                              insertedIds: Map[ResultOutputType, Seq[Int]]) {
    for {
      (queryResult, _) <- failures
      failedBreakdownType <- queryResult.resultType
      Seq(resultId) <- insertedIds.get(failedBreakdownType)
    } {
      dao.insertErrorResult(resultId, "Couldn't retrieve breakdown of type '" + failedBreakdownType + "'")
    }
  }

  protected def obfuscateResults(results: Seq[QueryResult]): Seq[QueryResult]= {
    import net.shrine.adapter.Obfuscator.obfuscate

    if(doObfuscation) results.map(obfuscate) else results
  }

  private def isLockedOut(identity: Identity): Boolean = {
    config.getAdapterLockoutAttemptsThreshold match {
      case 0 => false
      case _ => dao.isUserLockedOut(identity, config.getAdapterLockoutAttemptsThreshold)
    }
  }
}

object RunQueryAdapter {
  private[adapter] def obfuscateBreakdowns(breakdowns: Map[ResultOutputType, I2b2ResultEnvelope]): Map[ResultOutputType, I2b2ResultEnvelope] = {
    breakdowns.mapValues(_.mapValues(Obfuscator.obfuscate))
  }
}