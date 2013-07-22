package net.shrine.adapter

import dao.{MasterTuple, UserAndMaster, IDPair, ResultTuple, RequestResponseData, AdapterDAO}
import translators.DefaultConceptTranslator
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.{BroadcastMessage, RunQueryResponse, RunQueryRequest}
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType
import javax.xml.bind.JAXBContext
import java.io.{StringWriter, StringReader}
import net.shrine.adapter.Obfuscator.obfuscate
import net.shrine.config.{ShrineConfig, I2B2HiveCredentials}
import xml.{TopScope, Elem, Node, XML, NodeSeq}
import net.shrine.util.XmlUtil

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
    override protected val hiveCredentials: I2B2HiveCredentials,
    private val conceptTranslator: DefaultConceptTranslator,
    private val config: ShrineConfig,
    private val doObfuscation: Boolean) extends CrcAdapter[RunQueryRequest, RunQueryResponse](crcUrl, dao, hiveCredentials) {

  private val context = JAXBContext.newInstance(classOf[QueryDefinitionType])

  protected def parseShrineResponse(nodeSeq: NodeSeq) = RunQueryResponse.fromI2b2(nodeSeq)

  protected def translateLocalToNetwork(response: RunQueryResponse) = response

  private def translateLocalResultIdsToNetworkIds(partiallyTranslatedResponse: RunQueryResponse, response: RunQueryResponse, resultIds: scala.Seq[Long]): RunQueryResponse = {
    partiallyTranslatedResponse.withResults(response.results.zipWithIndex map {
      case (result, i) => result.withId(resultIds(i))
    })
  }

  private def translateLocalIdsToNetworkIds(response: RunQueryResponse, masterId: Long, instanceId: Long, resultIds: Seq[Long]) = {
    val partiallyTranslatedResponse = response.withId(masterId).withInstanceId(instanceId)
    translateLocalResultIdsToNetworkIds(partiallyTranslatedResponse, response, resultIds)
  }

  private def unmarshalQueryDefinition(queryDefinition: String): QueryDefinitionType = {
    val unmarshaller = context.createUnmarshaller
    unmarshaller.unmarshal(new StringReader(queryDefinition)).asInstanceOf[QueryDefinitionType]
  }

  private def marshalQueryDefinition(queryDef: QueryDefinitionType): String = {
    val marshaller = context.createMarshaller
    val stringWriter = new StringWriter()
    marshaller.marshal(queryDef, stringWriter)
    val newQueryDefString = stringWriter.toString
    newQueryDefString
  }

  private[adapter] def translateQueryDefinition(queryDefinition: String): String = {
    val queryDef = unmarshalQueryDefinition(queryDefinition)
    conceptTranslator.translateQueryDefinition(queryDef)

    //JAXB adds namespacing where we don't want it (this element will wind up
    //being embedded in a parent i2b2 message with its own namespacing)
    XmlUtil.stripNamespace(marshalQueryDefinition(queryDef))
  }

  protected def translateNetworkToLocal(request: RunQueryRequest) = {
    request.withQueryDefinition(translateQueryDefinition(request.queryDefinitionXml))
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
      response.queryName,
      response.createDate.toGregorianCalendar.getTime))

    dao.insertMaster(new MasterTuple(IDPair.of(message.masterId.get, response.queryId.toString), message.request.asInstanceOf[RunQueryRequest].queryDefinitionXml));

    dao.insertInstanceIDPair(IDPair.of(message.instanceId.get, response.queryInstanceId.toString));

    insertResultIds(response, identity, message)
  }

  protected def obfuscateResponse(response: RunQueryResponse): RunQueryResponse = {
    if (doObfuscation) response.withResults(obfuscate(response.results, dao)) else response
  }

  private def isLockedOut(identity: Identity): Boolean = {
    if(config.getAdapterLockoutAttemptsThreshold == 0) {
      false
    }
    else {
      dao.isUserLockedOut(identity, config.getAdapterLockoutAttemptsThreshold)

    }
  }

  override protected def processRequest(identity: Identity, message: BroadcastMessage) = {
    if(isLockedOut(identity)) {
      throw new AdapterLockoutException(identity);
    }

    var response = super.processRequest(identity, message).asInstanceOf[RunQueryResponse]
    createIdMappings(identity, message, response)
    response = translateLocalIdsToNetworkIds(response, message.masterId.get, message.instanceId.get, message.resultIds.get)
    obfuscateResponse(response)
  }
}