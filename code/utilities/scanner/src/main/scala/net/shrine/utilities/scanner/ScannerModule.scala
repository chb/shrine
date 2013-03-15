package net.shrine.utilities.scanner

import net.shrine.ont.data.OntologyDAO
import net.shrine.ont.data.ShrineSqlOntologyDAO
import net.shrine.config.AdapterMappingsSource
import net.shrine.config.ClasspathAdapterMappingsSource
import net.shrine.client.JerseyShrineClient
import net.shrine.client.ShrineClient
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import net.shrine.config.ClasspathAdapterMappingsSource
import java.io.FileInputStream
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import org.spin.client.RemoteSpinClient
import org.spin.client.SpinClientConfig
import org.spin.client.Credentials
import org.spin.tools.config.DefaultPeerGroups
import org.spin.tools.config.EndpointConfig
import net.shrine.broadcaster.spin.SpinBroadcastService
import org.spin.tools.crypto.PKITool
import org.spin.node.connector.ConnectorUtils

/**
 * @author clint
 * @date Mar 6, 2013
 */
final class ScannerModule(config: ScannerConfig) extends Scanner {
  
  override val reScanTimeout = config.reScanTimeout
  
  override val adapterMappingsSource = new ClasspathAdapterMappingsSource(config.adapterMappingsFile)
  
  override val ontologyDao = new ShrineSqlOntologyDAO(classpathStream(config.ontologySqlFile))
  
  override val client = {
    val spinCredentials = Credentials(config.authorization.domain, config.authorization.username, config.authorization.credential.value)
    
    val peerGroupToQuery = DefaultPeerGroups.LOCAL.name
    
    val entryPoint = EndpointConfig.soap(config.shrineUrl)
    
    val spinConfig = SpinClientConfig(peerGroupToQuery, spinCredentials, entryPoint)
    
    val spinClient = new RemoteSpinClient(spinConfig)
    
    val broadcastService = new SpinBroadcastService(spinClient)
    
    BroadcastServiceScannerClient(config.projectId, config.authorization, broadcastService)
  }
  
  private def classpathStream(fileName: String) = getClass.getClassLoader.getResourceAsStream(fileName)
}

object ScannerModule {
  def main(args: Array[String]) {
    
    val config = ScannerConfig("testAdapterMappings.xml", 
    						   "testShrineWithSyns.sql",
    						   10.seconds, 
    						   "https://shrine-dev1.chip.org:6060/shrine-cell/soap/aggregate?wsdl", 
    						   "SHRINE", 
    						   AuthenticationInfo("HarvardDemo", "bsimons", Credential("testtest", false)))
    				
    val scanner = new ScannerModule(config)
    
    scanner.scan()
  }
}