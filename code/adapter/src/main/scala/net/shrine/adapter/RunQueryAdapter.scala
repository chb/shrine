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

  override protected def parseShrineResponse(nodeSeq: NodeSeq) = RawCrcRunQueryResponse.fromI2b2(nodeSeq)

  override protected[adapter] def translateNetworkToLocal(request: RunQueryRequest): RunQueryRequest = {
    request.mapQueryDefinition(conceptTranslator.translate)
  }

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): RunQueryResponse = {
    if (isLockedOut(identity)) {
      throw new AdapterLockoutException(identity)
    }

    //TODO: Any way to avoid this cast?
    val runQueryReq = message.request.asInstanceOf[RunQueryRequest]

    val rawRunQueryResponse = super.processRequest(identity, message).asInstanceOf[RawCrcRunQueryResponse]
    
    val insertedQueryId = dao.insertQuery(rawRunQueryResponse.queryId.toString, runQueryReq.networkQueryId, runQueryReq.queryDefinition.name, runQueryReq.authn, runQueryReq.queryDefinition.expr)

    val insertedQueryResultIds = dao.insertQueryResults(insertedQueryId, rawRunQueryResponse)

    //val originalRunQueryResponse = rawRunQueryResponse.toRunQueryResponse

    val obfuscatedRunQueryResponse = obfuscateResponse(rawRunQueryResponse)

    storeCountAndErrorResults(rawRunQueryResponse, obfuscatedRunQueryResponse, insertedQueryResultIds)

    def isBreakdown(result: QueryResult) = result.resultType.map(_.isBreakdown).getOrElse(false)

    val (breakdownResults, nonBreakDownResults) = obfuscatedRunQueryResponse.results.partition(isBreakdown)

    val attemptsWithBreakDownCounts = attemptToRetrieveBreakdowns(runQueryReq, breakdownResults)

    val (successes, failures) = attemptsWithBreakDownCounts.partition { case (_, t) => t.isSuccess }

    logBreakdownFailures(rawRunQueryResponse, successes, failures, insertedQueryResultIds)

    val mergedBreakdowns = mergeAndStoreBreakdowns(successes, insertedQueryResultIds)

    //TODO: Will fail in the case of NO non-breakdown QueryResults.  Can this ever happen, and is it worth protecting against here?
    val resultWithMergedBreakdowns = nonBreakDownResults.head.withBreakdowns(mergedBreakdowns)

    obfuscatedRunQueryResponse.toRunQueryResponse.withResult(resultWithMergedBreakdowns)
  }

  private[adapter] def storeCountResults(insertedIds: Map[ResultOutputType, Seq[Int]], notErrors: Seq[QueryResult], obfuscatedNotErrors: Seq[QueryResult]) {
    for {
      Seq(insertedCountQueryResultId) <- insertedIds.get(ResultOutputType.PATIENT_COUNT_XML)
      //NB: Take the count/setSize from the FIRST QueryResult, though the same count should be there for all of them, if there are more than one
      origQueryResult <- notErrors.headOption
      obfscQueryResult <- obfuscatedNotErrors.headOption
    } {
      dao.insertCountResult(insertedCountQueryResultId, origQueryResult.setSize, obfscQueryResult.setSize)
    }
  }

  private[adapter] def storeErrorResults(insertedIds: Map[ResultOutputType, Seq[Int]], errors: Seq[QueryResult]) {
    val errorResultIds = insertedIds.filter { case (resultType, _) => resultType.isError }.values

    for {
      (Seq(insertedErrorResultId), errorQueryResult) <- errorResultIds zip errors
    } {
      dao.insertErrorResult(insertedErrorResultId, errorQueryResult.statusMessage.getOrElse("Unknown failure"))
    }
  }

  private[adapter] def storeCountAndErrorResults(response: RawCrcRunQueryResponse, obfuscated: RawCrcRunQueryResponse, insertedIds: Map[ResultOutputType, Seq[Int]]) {

    val (errors, notErrors) = response.results.partition(_.isError)

    val obfuscatedNotErrors = obfuscated.results.filter(!_.isError)

    storeCountResults(insertedIds, notErrors, obfuscatedNotErrors)

    //Store errors, if present
    storeErrorResults(insertedIds, errors)
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
                                            breakdownSuccesses: Seq[(QueryResult, Try[QueryResult])],
                                            failures: Seq[(QueryResult, Try[QueryResult])],
                                            insertedIds: Map[ResultOutputType, Seq[Int]]) {
    val successfulBreakdownTypes = (for {
      (_, Success(queryResult)) <- breakdownSuccesses
      resultType <- queryResult.resultType
    } yield resultType).toSet

    val failedBreakdownTypes = ResultOutputType.breakdownTypes.toSet -- successfulBreakdownTypes

    for {
      failedBreakdownType <- failedBreakdownTypes
      Seq(resultId) <- insertedIds.get(failedBreakdownType)
    } {
      dao.insertErrorResult(resultId, "Couldn't retrieve breakdown of type '" + failedBreakdownType + "'")
    }

    for {
      (origQueryResult, Failure(e)) <- failures
    } {
      error("Couldn't load breakdown for QueryResult with masterId: " + response.queryId + ", instanceId: " + origQueryResult.instanceId + ", resultId: " + origQueryResult.resultId + ". Asked for result type: " + origQueryResult.resultType, e)
    }
  }

  private[adapter] def mergeAndStoreBreakdowns(breakdownSuccesses: Seq[(QueryResult, Try[QueryResult])], insertedQueryResultIds: Map[ResultOutputType, Seq[Int]]) = {
    val withBreakdownCounts = breakdownSuccesses.collect { case (_, Success(queryResultWithBreakdowns)) => queryResultWithBreakdowns }

    val mergedBreakdowns = withBreakdownCounts.map(_.breakdowns).fold(Map.empty)(_ ++ _)

    val obfuscatedBreakdowns = RunQueryAdapter.obfuscateBreakdowns(mergedBreakdowns)

    //Store breakdowns (plain and obfuscated) in the DB
    dao.insertBreakdownResults(insertedQueryResultIds, mergedBreakdowns, obfuscatedBreakdowns)

    mergedBreakdowns
  }

  protected def obfuscateResponse(response: RawCrcRunQueryResponse): RawCrcRunQueryResponse = {
    import net.shrine.adapter.Obfuscator.obfuscate

    doObfuscation match {
      case true => response.withResults(response.singleNodeResults.values.flatMap(_.map(obfuscate)))
      case false => response
    }
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