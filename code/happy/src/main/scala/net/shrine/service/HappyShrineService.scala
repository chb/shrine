package net.shrine.service

import java.io.StringWriter
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaSet
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.xml.NodeSeq
import scala.xml.XML
import org.spin.client.SpinAgent
import org.spin.message.Failure
import org.spin.message.QueryInfo
import org.spin.message.Result
import org.spin.message.ResultSet
import org.spin.node.actions.discovery.DiscoveryResult
import org.spin.node.actions.discovery.DiscoveryCriteria
import org.spin.node.DefaultQueries
import org.spin.tools.config.RoutingTableConfig
import org.spin.tools.config.ConfigTool
import org.spin.tools.config.EndpointConfig
import org.spin.tools.crypto.signature.CertID
import org.spin.tools.crypto.signature.Identity
import org.spin.tools.crypto.PKCryptor
import org.spin.tools.crypto.PKITool
import org.spin.tools.crypto.XMLSignatureUtil
import org.spin.tools.JAXBUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.JAXBContext
import net.shrine.adapter.dao.AdapterDAO
import net.shrine.broadcaster.dao.AuditDAO
import net.shrine.config.ShrineConfig
import net.shrine.config.HiveCredentials
import net.shrine.i2b2.protocol.pm.GetUserConfigurationRequest
import net.shrine.i2b2.protocol.pm.HiveConfig
import net.shrine.protocol.query.OccuranceLimited
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.CRCRequestType
import net.shrine.protocol.Credential
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.RunQueryRequest
import net.shrine.util.HTTPClient
import net.shrine.util.XmlUtil
import net.shrine.util.Versions

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
class HappyShrineService @Autowired() (
  private val shrineConfig: ShrineConfig,
  private val hiveCredentials: HiveCredentials,
  private val pmEndpoint: String,
  private val spinClient: SpinAgent,
  private val auditDao: AuditDAO,
  private val adapterDao: AdapterDAO) extends HappyShrineRequestHandler {

  //TODO - maybe make this a spring bean since its used in shrine service too?
  private lazy val endpointConfig = EndpointConfig.soap(shrineConfig.getAggregatorEndpoint)

  override def keystoreReport: String = {
    val keystoreConfig = ConfigTool.loadKeyStoreConfig
    val pki: PKITool = PKITool.getInstance
    val certId: CertID = pki.getMyX509.getCertID
    XmlUtil.stripWhitespace(
      <keystoreReport>
        <configFile>{ keystoreConfig.getFile.toURI }</configFile>
        <certId>
          <name>{ certId.getName }</name>
          <serial>{ certId.getSerial }</serial>
        </certId>
        <importedCerts>
          {
            pki.getImportedCertsCopy map { cert =>
              <cert>
                <name>{ cert.getCertID.getName }</name>
                <serial>{ cert.getCertID.getSerial.toString }</serial>
              </cert>
            }
          }
        </importedCerts>
      </keystoreReport>).toString
  }

  override def shrineConfigReport: String = {
    val marshaller = JAXBContext.newInstance(classOf[ShrineConfig]).createMarshaller
    marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", false);
    val sWriter = new StringWriter
    marshaller.marshal(shrineConfig, sWriter)
    sWriter.toString
  }

  override def routingReport: String = {
    val marshaller = JAXBContext.newInstance(classOf[RoutingTableConfig]).createMarshaller
    marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", false);
    val sWriter = new StringWriter
    marshaller.marshal(ConfigTool.loadRoutingTableConfig, sWriter)
    sWriter.toString
  }

  override def hiveReport: String = {
    val pmRequest = new GetUserConfigurationRequest(hiveCredentials.domain, hiveCredentials.username, hiveCredentials.password)
    val responseXml: String = HTTPClient.post(pmRequest.toI2b2String, pmEndpoint)

    HiveConfig.fromI2b2(responseXml).toXmlString
  }

  private def invalidSpinConfig = XmlUtil.stripWhitespace(
    <spin>
      <properlyConnected>false</properlyConnected>
      <error>peer group to query is not defined in routing table</error>
    </spin>).toString

  private[service] def partitionSpinResults(resultSet: ResultSet): (Seq[Result], Seq[Failure]) = {
    val results = resultSet.getResults.toSeq

    val failures = resultSet.getFailures.toSeq

    (results, failures)
  }

  private[service] def generateSpinReport(routingTable: RoutingTableConfig): String = {
    val peerGroupOption = routingTable.getPeerGroups find (_.getGroupName == shrineConfig.getBroadcasterPeerGroupToQuery)

    if (!peerGroupOption.isDefined) {
      return invalidSpinConfig
    }

    val identity: Identity = XMLSignatureUtil.getDefaultInstance.sign(new Identity("happy", "happy"))
    val queryInfo: QueryInfo = new QueryInfo(shrineConfig.getBroadcasterPeerGroupToQuery, identity, DefaultQueries.Discovery.queryType, endpointConfig)
    val ackNack = spinClient.send(queryInfo, DiscoveryCriteria.Instance)
    val resultSet = spinClient.receive(ackNack.getQueryId, identity)
    val expectedCount = peerGroupOption.get.getChildren.size + 1 //add one to include self
    val (results, failures) = partitionSpinResults(resultSet)

    XmlUtil.stripWhitespace(
      <spin>
        <properlyConnected>{ !resultSet.getFailures.isEmpty }</properlyConnected>
        <expectedNodeCount>{ expectedCount }</expectedNodeCount>
        <actualNodeCount>{ resultSet.getResults.size }</actualNodeCount>
        <failureCount>{ resultSet.getFailures.size }</failureCount>
        {
          results.map { spinResult =>
            val xmlString = (new PKCryptor).decrypt(spinResult.getPayload)
            val discoveryResult = JAXBUtils.unmarshal(xmlString, classOf[DiscoveryResult]) //TODO fixme - use scala xpath instead of JAXB?
            <node>
              <name>{ discoveryResult.getNodeConfig.getNodeName }</name>
              <url>{ discoveryResult.getNodeURL }</url>
            </node>
          }
        }
        {
          failures.map(toXml)
        }
      </spin>).toString
  }

  override def spinReport: String = {
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

  override def adapterReport: String = {
    if (!shrineConfig.isAdapter) {
      XmlUtil.stripWhitespace(
        <adapter>
          <isAdapter>false</isAdapter>
        </adapter>).toString
    } else {
      val identity: Identity = XMLSignatureUtil.getDefaultInstance.sign(new Identity("happy", "happy"))
      val queryInfo: QueryInfo = new QueryInfo("LOCAL", identity, CRCRequestType.QueryDefinitionRequestType.name, endpointConfig)
      val message = BroadcastMessage(newRunQueryRequest)

      val ackNack = spinClient.send(queryInfo, message, BroadcastMessage.serializer)
      val resultSet = spinClient.receive(ackNack.getQueryId, identity)

      val (results, failures) = partitionSpinResults(resultSet)

      XmlUtil.stripWhitespace(
        <adapter>
          <isAdapter>true</isAdapter>
          {
            results.map { result =>
              <result>
                <description>{ result.getDescription }</description>
                <payload>
                  {
                    val decryptedPayload = (new PKCryptor).decrypt(result.getPayload)
                    XML.loadString(decryptedPayload)
                  }
                </payload>
              </result>
            }
          }
          {
            failures.map(toXml)
          }
        </adapter>).toString
    }
  }

  private[service] def toXml(failure: Failure): NodeSeq = {
    <failure>
      <origin>{ failure.getDescription }</origin>
      <description>{ failure.getDescription }</description>
    </failure>
  }

  @Transactional(readOnly = true)
  override def auditReport: String = {
    val recentEntries = auditDao.findRecentEntries(10)
    XmlUtil.stripWhitespace(
      <recentAuditEntries>
        {
          recentEntries map { entry =>
            <entry>
              <id>{ entry.getAuditEntryId }</id>
              <time>{ entry.getTime }</time>
              <username>{ entry.getUsername }</username>
            </entry>
          }
        }
      </recentAuditEntries>).toString
  }

  @Transactional(readOnly = true)
  override def queryReport: String = {
    val recentQueries = adapterDao.findRecentQueries(10)
    XmlUtil.stripWhitespace(
      <recentQueries>
        {
          recentQueries map { query =>
            <query>
              <id>{ query.getNetworkMasterID }</id>
              <date>{ query.getMasterCreateDate }</date>
              <name>{ query.getMasterName }</name>
            </query>
          }
        }
      </recentQueries>).toString
  }

  override def versionReport: String = {
    XmlUtil.stripWhitespace(
      <versionInfo>
        <shrineVersion>{ Versions.version }</shrineVersion>
        <scmRevision>{ Versions.scmRevision }</scmRevision>
        <scmBranch>{ Versions.scmBranch }</scmBranch>
        <buildDate>{ Versions.buildDate }</buildDate>
      </versionInfo>).toString
  }

  @Transactional(readOnly = true)
  override def all: String = {
    new StringBuilder("<all>")
      .append(versionReport)
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
