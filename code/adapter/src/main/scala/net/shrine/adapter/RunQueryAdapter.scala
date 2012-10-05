package net.shrine.adapter

import dao.{ MasterTuple, UserAndMaster, IDPair, ResultTuple, RequestResponseData, AdapterDAO }
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.{ BroadcastMessage, RunQueryResponse, RunQueryRequest }
import net.shrine.adapter.Obfuscator.obfuscate
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

/**
 * @author Bill Simons
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
  override protected val dao: AdapterDAO,
  override protected val hiveCredentials: HiveCredentials,
  private[adapter] val conceptTranslator: QueryDefinitionTranslator,
  config: ShrineConfig,
  doObfuscation: Boolean) extends CrcAdapter[RunQueryRequest, RunQueryResponse](crcUrl, httpClient, dao, hiveCredentials) {

  override protected def parseShrineResponse(nodeSeq: NodeSeq) = RunQueryResponse.fromI2b2(nodeSeq)

  override protected def translateLocalToNetwork(response: RunQueryResponse) = response

  override protected def translateNetworkToLocal(request: RunQueryRequest) = {
    request.withQueryDefinition(conceptTranslator.translate(request.queryDefinition))
  }

  private def translateLocalResultIdsToNetworkIds(partiallyTranslatedResponse: RunQueryResponse, response: RunQueryResponse, broadcastResultIds: Seq[Long]): RunQueryResponse = {
    partiallyTranslatedResponse.withResults(response.results.zip(broadcastResultIds).map {
      case (result, broadcastResultId) => result.withId(broadcastResultId)
    })
  }

  private def translateLocalIdsToNetworkIds(response: RunQueryResponse, broadcastMasterId: Long, broadcastInstanceId: Long, broadcastResultIds: Seq[Long]) = {
    val partiallyTranslatedResponse = response.withId(broadcastMasterId).withInstanceId(broadcastInstanceId)

    translateLocalResultIdsToNetworkIds(partiallyTranslatedResponse, response, broadcastResultIds)
  }

  private def insertResultIds(response: RunQueryResponse, identity: Identity, broadcastMessage: BroadcastMessage): Unit = {
    //TODO: What should we do if the broadcast message has no result Ids?
    require(broadcastMessage.resultIds.isDefined)

    val broadcastResultIds = broadcastMessage.resultIds.get

    //TODO: Is this appropriate?
    require(broadcastResultIds.size == response.results.size, "expected same number of result ids, but got: broadcast: " + broadcastResultIds + " local: " + response.results)

    for {
      (result, broadcastResultId) <- response.results.zip(broadcastResultIds)
      localResultId = result.resultId
      broadcastMasterId <- broadcastMessage.masterId
      broadcastInstanceId <- broadcastMessage.instanceId
    } {
      dao.insertRequestResponseData(new RequestResponseData(
        identity.getDomain,
        identity.getUsername,
        broadcastMasterId,
        broadcastInstanceId,
        broadcastResultId,
        result.statusType,
        result.setSize.toInt,
        0L,
        "",
        response.toI2b2String))

      dao.insertResultTuple(new ResultTuple(IDPair.of(broadcastResultId, localResultId.toString)))
    }
  }

  private def createIdMappings(identity: Identity, message: BroadcastMessage, response: RunQueryResponse) = {
    dao.insertUserAndMasterIDMapping(new UserAndMaster(identity.getDomain,
      identity.getUsername,
      message.masterId.get,
      message.request.asInstanceOf[RunQueryRequest].queryDefinition.name,
      response.createDate.toGregorianCalendar.getTime))

    val AsRunQueryRequest(runQueryReq) = message.request
      
    //TODO: Is converting to i2b2 XML appropriate?  It's what we've always stored.
    dao.insertMaster(new MasterTuple(IDPair.of(message.masterId.get, response.queryId.toString), runQueryReq.queryDefinition.toI2b2String))

    dao.insertInstanceIDPair(IDPair.of(message.instanceId.get, response.queryInstanceId.toString))

    insertResultIds(response, identity, message)
  }

  protected def obfuscateResponse(response: RunQueryResponse): RunQueryResponse = {
    if (doObfuscation) response.withResults(obfuscate(response.results, dao)) else response
  }

  private def isLockedOut(identity: Identity): Boolean = {
    config.getAdapterLockoutAttemptsThreshold match {
      case 0 => false
      case _ => dao.isUserLockedOut(identity, config.getAdapterLockoutAttemptsThreshold)
    }
  }

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): RunQueryResponse = {
    if (isLockedOut(identity)) {
      throw new AdapterLockoutException(identity)
    }

    def isBreakdown(result: QueryResult) = result.resultType.map(_.isBreakdown).getOrElse(false)

    def toLocalResultId(networkResultId: Long): String = dao.findLocalResultID(networkResultId)
    
    def readResultRequest(runQueryReq: RunQueryRequest, networkResultId: Long) = ReadResultRequest(hiveCredentials.project, runQueryReq.waitTimeMs, hiveCredentials.toAuthenticationInfo, toLocalResultId(networkResultId))
    
    val obfuscatedRunQueryResponse = {
      val response = super.processRequest(identity, message).asInstanceOf[RunQueryResponse]

      createIdMappings(identity, message, response)
      
      obfuscateResponse(translateLocalIdsToNetworkIds(response, message.masterId.get, message.instanceId.get, message.resultIds.get))
    }

    val (breakdownResults, nonBreakDownResults) = obfuscatedRunQueryResponse.results.partition(isBreakdown)

    val AsRunQueryRequest(runQueryReq) = message.request

    val attemptsWithBreakDownCounts = breakdownResults.map { origBreakdownResult =>
      (origBreakdownResult, Try {
        //NB: readResultRequest() goes back to the DB to translate the network resultId to a local one suitable for
        //sending to the CRC.  This extra DB hit isn't ideal, but this was a simple way to proceed, and Id-translation
        //will - hopefully - be gone soon, rendering this issue moot.
        val respXml = callCrc(readResultRequest(runQueryReq, origBreakdownResult.resultId))

        val breakdownData = ReadResultResponse.fromI2b2(respXml).data

        origBreakdownResult.withBreakdown(breakdownData)
      })
    }

    val (successes, failures) = attemptsWithBreakDownCounts.partition { case (_, t) => t.isSuccess }

    failures.foreach {
      case (origQueryResult, Failure(e)) =>
        error(e, "Couldn't load breakdown for QueryResult with masterId: " + obfuscatedRunQueryResponse.queryId + ", instanceId: " + origQueryResult.instanceId + ", resultId: " + origQueryResult.resultId + ". Asked for result type: " + origQueryResult.resultType)
    }

    //TODO: Will fail in the case of NO non-breakdown QueryResults.  Can this ever happen, and is it worth protecting against here?
    val resultWithMergedBreakdowns = {
      val withBreakdownCounts = successes.collect { case (_, Success(queryResultWithBreakdowns)) => queryResultWithBreakdowns }
      
      val mergedBreakdowns = withBreakdownCounts.map(_.breakdowns).fold(Map.empty)(_ ++ _)
      
      nonBreakDownResults.head.withBreakdowns(mergedBreakdowns)
    }
     
    obfuscatedRunQueryResponse.withResults(Seq(resultWithMergedBreakdowns))
  }
}