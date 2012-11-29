package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.{ BroadcastMessage, RunQueryResponse, RunQueryRequest }
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

  override protected[adapter] def translateNetworkToLocal(request: RunQueryRequest): RunQueryRequest = {
    request.mapQueryDefinition(conceptTranslator.translate)
  }
  
  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): RunQueryResponse = {
    if (isLockedOut(identity)) {
      throw new AdapterLockoutException(identity)
    }

    //TODO: Any way to avoid this cast?
    val runQueryReq = message.request.asInstanceOf[RunQueryRequest]
    
    val insertedQueryId = dao.insertQuery(message.requestId, runQueryReq.queryDefinition.name, runQueryReq.authn, runQueryReq.queryDefinition.expr)

    val originalRunQueryResponse = super.processRequest(identity, message).asInstanceOf[RunQueryResponse]
    
    val insertedQueryResultIds = dao.insertQueryResults(insertedQueryId, originalRunQueryResponse)

    //TODO: Revisit optional nature of message.{masterId, instanceId, resultIds}
    //TODO: how to fail if those fields are missing?  The current .get calls will fail quite un-gracefully
    val obfuscatedRunQueryResponse = obfuscateResponse(originalRunQueryResponse)

    storeCountAndErrorResults(originalRunQueryResponse, obfuscatedRunQueryResponse, insertedQueryResultIds)

    def isBreakdown(result: QueryResult) = result.resultType.map(_.isBreakdown).getOrElse(false)
    
    val (breakdownResults, nonBreakDownResults) = obfuscatedRunQueryResponse.results.partition(isBreakdown)

    val attemptsWithBreakDownCounts = attemptToRetrieveBreakdowns(runQueryReq, breakdownResults)

    val (successes, failures) = attemptsWithBreakDownCounts.partition { case (_, t) => t.isSuccess }

    logBreakdownFailures(obfuscatedRunQueryResponse, failures)

    val mergedBreakdowns = mergeAndStoreBreakdowns(successes, insertedQueryResultIds)

    //TODO: Will fail in the case of NO non-breakdown QueryResults.  Can this ever happen, and is it worth protecting against here?
    val resultWithMergedBreakdowns = nonBreakDownResults.head.withBreakdowns(mergedBreakdowns)
    
    obfuscatedRunQueryResponse.withResults(Seq(resultWithMergedBreakdowns))
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

  private[adapter] def storeCountAndErrorResults(response: RunQueryResponse, obfuscated: RunQueryResponse, insertedIds: Map[ResultOutputType, Seq[Int]]) {

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
        //NB: readResultRequest() goes back to the DB to translate the network resultId to a local one suitable for
        //sending to the CRC.  This extra DB hit isn't ideal, but this was a simple way to proceed, and Id-translation
        //will - hopefully - be gone soon, rendering this issue moot.
        val respXml = callCrc(readResultRequest(runQueryReq, origBreakdownResult.resultId))

        val breakdownData = ReadResultResponse.fromI2b2(respXml).data

        origBreakdownResult.withBreakdown(breakdownData)
      })
    }
  }
  
  private[adapter] def logBreakdownFailures(obfuscatedRunQueryResponse: RunQueryResponse, failures: Seq[(QueryResult, Try[QueryResult])]) {
    for {
      (origQueryResult, Failure(e)) <- failures
    } {
      error(e, "Couldn't load breakdown for QueryResult with masterId: " + obfuscatedRunQueryResponse.queryId + ", instanceId: " + origQueryResult.instanceId + ", resultId: " + origQueryResult.resultId + ". Asked for result type: " + origQueryResult.resultType)
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
  
  override protected def parseShrineResponse(nodeSeq: NodeSeq) = RunQueryResponse.fromI2b2(nodeSeq)

  protected def obfuscateResponse(response: RunQueryResponse): RunQueryResponse = {
    import net.shrine.adapter.Obfuscator.obfuscate

    if (doObfuscation) response.withResults(obfuscate(response.results)) else response
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