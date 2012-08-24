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
  override protected val crcUrl: String,
  override protected val dao: AdapterDAO,
  override protected val hiveCredentials: HiveCredentials,
  private[adapter] val conceptTranslator: QueryDefinitionTranslator,
  config: ShrineConfig,
  doObfuscation: Boolean) extends CrcAdapter[RunQueryRequest, RunQueryResponse](crcUrl, dao, hiveCredentials) {

  override protected def parseShrineResponse(nodeSeq: NodeSeq) = RunQueryResponse.fromI2b2(nodeSeq)

  override protected def translateLocalToNetwork(response: RunQueryResponse) = response

  override protected def translateNetworkToLocal(request: RunQueryRequest) = {
    request.withQueryDefinition(conceptTranslator.translate(request.queryDefinition))
  }

  private def translateLocalResultIdsToNetworkIds(partiallyTranslatedResponse: RunQueryResponse, response: RunQueryResponse, resultIds: scala.Seq[Long]): RunQueryResponse = {
    partiallyTranslatedResponse.withResults(response.results.zipWithIndex.map {
      case (result, i) => result.withId(resultIds(i))
    })
  }

  private def translateLocalIdsToNetworkIds(response: RunQueryResponse, masterId: Long, instanceId: Long, resultIds: Seq[Long]) = {
    val partiallyTranslatedResponse = response.withId(masterId).withInstanceId(instanceId)
    
    translateLocalResultIdsToNetworkIds(partiallyTranslatedResponse, response, resultIds)
  }

  private def insertResultIds(response: RunQueryResponse, identity: Identity, message: BroadcastMessage): Unit = {
    response.results.zipWithIndex foreach {
      case (result, i) =>
        val result = response.results(i)
        //TODO - real elapsed time and spin query id needed?
        dao.insertRequestResponseData(new RequestResponseData(
          identity.getDomain,
          identity.getUsername,
          message.masterId.get,
          message.instanceId.get,
          message.resultIds.get(i),
          result.statusType,
          result.setSize.toInt,
          0L,
          "",
          response.toI2b2.toString))

        dao.insertResultTuple(new ResultTuple(IDPair.of(message.resultIds.get(i), result.resultId.toString)));
    }
  }

  private def createIdMappings(identity: Identity, message: BroadcastMessage, response: RunQueryResponse) = {
    dao.insertUserAndMasterIDMapping(new UserAndMaster(identity.getDomain,
      identity.getUsername,
      message.masterId.get,
      message.request.asInstanceOf[RunQueryRequest].queryDefinition.name,
      response.createDate.toGregorianCalendar.getTime))

    //TODO: Is converting to i2b2 XML appropriate?  It's what we've always stored.
    dao.insertMaster(new MasterTuple(IDPair.of(message.masterId.get, response.queryId.toString), message.request.asInstanceOf[RunQueryRequest].queryDefinition.toI2b2.toString))

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

  override protected def processRequest(identity: Identity, message: BroadcastMessage) = {
    if (isLockedOut(identity)) {
      throw new AdapterLockoutException(identity)
    }

    val response = super.processRequest(identity, message).asInstanceOf[RunQueryResponse]

    createIdMappings(identity, message, response)

    val obfuscated = obfuscateResponse(translateLocalIdsToNetworkIds(response, message.masterId.get, message.instanceId.get, message.resultIds.get))

    def isBreakdown(result: QueryResult) = result.resultType.map(_.isBreakdown).getOrElse(false)

    val (breakdownResults, nonBreakDownResults) = response.results.partition(isBreakdown)

    def readResultRequest(runQueryReq: RunQueryRequest, resultId: Long) = ReadResultRequest(runQueryReq.projectId, runQueryReq.waitTimeMs, runQueryReq.authn, resultId)

    val AsRunQueryRequest(runQueryReq) = message.request
    
    val withBreakDownCounts = breakdownResults.map { breakdownResult =>
      val respXml = callCrc(readResultRequest(runQueryReq, breakdownResult.resultId))

      breakdownResult.withBreakdown(ReadResultResponse.fromI2b2(respXml).data)
    }

    obfuscated.withResults(nonBreakDownResults ++ withBreakDownCounts)
  }
}