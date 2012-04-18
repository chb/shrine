package net.shrine.service

import org.springframework.beans.factory.annotation.Autowired
import scala.collection.JavaConversions._
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import org.springframework.transaction.annotation.Transactional
import org.spin.query.message.agent.SpinAgent
import org.spin.query.message.headers.QueryInfo
import org.spin.node.DefaultQueries
import org.spin.tools.crypto.signature.{XMLSignatureUtil, Identity, CertID}
import org.spin.tools.{JAXBUtils, PKITool}
import org.spin.tools.crypto.PKCryptor
import org.spin.node.actions.discovery.{DiscoveryResult, DiscoveryCriteria}
import net.shrine.protocol._
import xml.XML
import org.spin.tools.config.{RoutingTableConfig, EndpointType, EndpointConfig, ConfigTool}
import java.lang.StringBuilder
import net.shrine.broadcaster.dao.AuditDAO
import net.shrine.adapter.dao.AdapterDAO
import org.springframework.stereotype.Service
import net.shrine.util.XmlUtil
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.OccuranceLimited
import net.shrine.protocol.query.Term
import net.shrine.serializers.HTTPClient
import net.shrine.i2b2.protocol.pm.{HiveConfig, GetUserConfigurationRequest}
import net.shrine.config.{HiveCredentials, ShrineConfig}

/**
 * @author Bill Simons
 * @date 6/20/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
@Service
class HappyShrineService @Autowired()(
    private val shrineConfig: ShrineConfig,
    private val hiveCredentials: HiveCredentials,
    private val pmEndpoint: String,
    private val spinClient: SpinAgent,
    private val auditDao: AuditDAO,
    private val adapterDao: AdapterDAO) extends HappyShrineRequestHandler {

  //TODO - maybe make this a spring bean since its used in shrine service too?
  private lazy val endpointConfig = new EndpointConfig(EndpointType.SOAP, shrineConfig.getAggregatorEndpoint)

  def keystoreReport: String = {
    val keystoreConfig = ConfigTool.loadKeyStoreConfig()
    val pki: PKITool = PKITool.getInstance
    val certId: CertID = pki.getMyX509.getCertID
    XmlUtil.stripWhitespace(
      <keystoreReport>
        <configFile>{keystoreConfig.getFile.toURI}</configFile>
        <certId>
          <name>{certId.getName}</name>
          <serial>{certId.getSerial}</serial>
        </certId>
        <importedCerts>
        {
          pki.getImportedCertsCopy map {cert =>
            <cert>
              <name>{cert.getCertID().getName}</name>
              <serial>{cert.getCertID().getSerial.toString}</serial>
            </cert>
          }
        }
        </importedCerts>
      </keystoreReport>).toString()
  }

  def shrineConfigReport: String = {
    val marshaller = JAXBContext.newInstance(classOf[ShrineConfig]).createMarshaller()
    marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", false);
    val sWriter = new StringWriter()
    marshaller.marshal(shrineConfig, sWriter)
    sWriter.toString
  }

  def routingReport: String = {
    val marshaller = JAXBContext.newInstance(classOf[RoutingTableConfig]).createMarshaller()
    marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", false);
    val sWriter = new StringWriter()
    marshaller.marshal(ConfigTool.loadRoutingTableConfig, sWriter)
    sWriter.toString
  }

  def hiveReport: String = {
    val pmRequest = new GetUserConfigurationRequest(hiveCredentials.domain, hiveCredentials.username, hiveCredentials.password)
    val responseXml: String = HTTPClient.post(pmRequest.toI2b2String, pmEndpoint)

    HiveConfig.fromI2b2(responseXml).toXmlString
  }

  private def invalidSpinConfig = XmlUtil.stripWhitespace(
    <spin>
      <properlyConnected>false</properlyConnected>
      <error>peer group to query is not defined in routing table</error>
    </spin>
  ).toString()

  private[service] def generateSpinReport(routingTable: RoutingTableConfig): String = {
    val peerGroupOption = routingTable.getPeerGroups find {x => x.getGroupName.equals(shrineConfig.getBroadcasterPeerGroupToQuery)}

    if(!peerGroupOption.isDefined) {
      return invalidSpinConfig
    }

    val identity: Identity = XMLSignatureUtil.getDefaultInstance.sign(new Identity("happy", "happy"))
    val queryInfo: QueryInfo = new QueryInfo(shrineConfig.getBroadcasterPeerGroupToQuery, identity, DefaultQueries.Discovery.queryType, endpointConfig)
    val ackNack = spinClient.send(queryInfo, new DiscoveryCriteria())
    val results = spinClient.receive(ackNack.getQueryID, identity)
    val expectedCount = peerGroupOption.get.getChildren.size() + 1 //add one to include self
    XmlUtil.stripWhitespace(
      <spin>
        <properlyConnected>{expectedCount == results.size()}</properlyConnected>
        <expectedNodeCount>{expectedCount}</expectedNodeCount>
        <actualNodeCount>{results.size()}</actualNodeCount>
        {
          results map {spinResult =>
            val xmlString = new PKCryptor().decrypt(spinResult.getPayload())
            val discoveryResult = JAXBUtils.unmarshal(xmlString, classOf[DiscoveryResult]) //TODO fixme - use scala xpath instead of JAXB?
            <node>
              <name>{discoveryResult.getNodeConfig.getNodeName}</name>
              <url>{discoveryResult.getNodeURL}</url>
            </node>
          }
        }
      </spin>).toString()
  }

  def spinReport: String = {
    val routingTable: RoutingTableConfig = ConfigTool.loadRoutingTableConfig
    generateSpinReport(routingTable)
  }

  private def newRunQueryRequest: RunQueryRequest = {
    val queryDefinition = QueryDefinition("PDD", OccuranceLimited(1, Term(shrineConfig.getAdapterStatusQuery)))

    new RunQueryRequest(
      "happyProject",
      180000,
      new AuthenticationInfo("happyDomain", "happy", new Credential("", false)),
      "",
      Set(ResultOutputType.PATIENT_COUNT_XML),
      queryDefinition)
  }

  def adapterReport: String = {
    if(!shrineConfig.isAdapter) {
      XmlUtil.stripWhitespace(
        <adapter>
          <isAdapter>false</isAdapter>
        </adapter>).toString()
    } else {
      val identity: Identity = XMLSignatureUtil.getDefaultInstance.sign(new Identity("happy", "happy"))
      val queryInfo: QueryInfo = new QueryInfo("LOCAL", identity, CRCRequestType.QueryDefinitionRequestType.name(), endpointConfig)
      val message = BroadcastMessage(newRunQueryRequest)

      val ackNack = spinClient.send(queryInfo, message, BroadcastMessage.serializer)
      val resultSet = spinClient.receive(ackNack.getQueryID, identity)

      XmlUtil.stripWhitespace(
        <adapter>
          <isAdapter>true</isAdapter>
          {
            resultSet map {result =>
              <result>
                <description>{result.getDescription}</description>
                <payload>
                  {
                    val decryptedPayload = new PKCryptor().decrypt(result.getPayload())
                    XML.loadString(decryptedPayload)
                  }
                </payload>
              </result>
            }
          }
        </adapter>).toString()
    }
  }

  @Transactional(readOnly = true)
  def auditReport: String = {
    val recentEntries = auditDao.findRecentEntries(10)
    XmlUtil.stripWhitespace(
      <recentAuditEntries>
        {
          recentEntries map {entry =>
            <entry>
              <id>{entry.getAuditEntryId}</id>
              <time>{entry.getTime}</time>
              <username>{entry.getUsername}</username>
            </entry>
          }
        }
      </recentAuditEntries>
    ).toString
  }

  @Transactional(readOnly = true)
  def queryReport: String = {
    val recentQueries = adapterDao.findRecentQueries(10)
    XmlUtil.stripWhitespace(
      <recentQueries>
        {
          recentQueries map {query =>
            <query>
              <id>{query.getNetworkMasterID}</id>
              <date>{query.getMasterCreateDate}</date>
              <name>{query.getMasterName}</name>
            </query>
          }
        }
      </recentQueries>
    ).toString
  }

  @Transactional(readOnly = true)
  def all: String = {
    new StringBuilder("<all>")
        .append(keystoreReport)
        .append(shrineConfigReport)
        .append(routingReport)
        .append(hiveReport)
        .append(spinReport)
        .append(adapterReport)
        .append(auditReport)
        .append(queryReport)
        .append("</all>").toString
  }
}
