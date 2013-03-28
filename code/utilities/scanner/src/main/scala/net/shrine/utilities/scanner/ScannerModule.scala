package net.shrine.utilities.scanner

import net.shrine.ont.data.OntologyDAO
import net.shrine.ont.data.ShrineSqlOntologyDAO
import net.shrine.config.AdapterMappingsSource
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
import scala.concurrent.Await
import net.shrine.utilities.scanner.components.HasSpinBroadcastServiceComponent
import net.shrine.utilities.scanner.components.HasSingleThreadExecutionContextComponent
import net.shrine.utilities.scanner.components.HasSpinClientComponent
import net.shrine.utilities.scanner.components.HasSpinClientConfigComponent
import net.shrine.utilities.scanner.components.HasRemoteSpinClientComponent
import net.shrine.utilities.scanner.components.HasSingleThreadExecutionContextComponent
import com.typesafe.config.ConfigFactory
import net.shrine.util.Versions

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
    
    val spinClientConfig = SpinClientConfig(peerGroupToQuery, spinCredentials, entryPoint)
    
    new SpinBroadcastServiceScannerClient(config.projectId, config.authorization, spinClientConfig) with HasSingleThreadExecutionContextComponent 
  }
  
  override def scan() = client.shutdownAfter { super.scan() }
  
  private def classpathStream(fileName: String) = getClass.getClassLoader.getResourceAsStream(fileName)
}

object ScannerModule {
  def printVersionInfo() {
    println(s"Shrine Scanner version: ${ Versions.version} built on ${ Versions.buildDate }")
    println(s"SCM branch: ${ Versions.scmBranch } SCM revision: ${ Versions.scmRevision })")
    println()
  }
  
  def main(args: Array[String]) {
    
    val argsToUse = if(args.isEmpty) Seq("--help") else args.toSeq
    
    val commandLineProps = CommandLineScannerConfig(argsToUse)
    
    if(commandLineProps.showVersionToggle.isSupplied) {
      printVersionInfo()
    }
    
    if(commandLineProps.showHelpToggle.isSupplied) {
      println("Usage: scanner [options]")
      
      commandLineProps.printHelp()
      
      System.exit(0)
    }
    
    /*val config = ScannerConfig("testAdapterMappings.xml", 
    						   "testShrineWithSyns.sql",
    						   10.seconds, 
    						   "https://shrine-dev1.chip.org:6060/shrine-cell/soap/aggregate?wsdl", 
    						   "SHRINE", 
    						   AuthenticationInfo("HarvardDemo", "bsimons", Credential("testtest", false)))*/
    val config = {
      val configFileProps = ConfigFactory.load
    
      val commandLineProps = CommandLineScannerConfig(args).toTypesafeConfig
      
      ScannerConfig(commandLineProps.withFallback(configFileProps))
    }
    				
    val scanner = new ScannerModule(config)
    
    val scanResults = scanner.scan()
    
    val command = Output.to("foo.csv")
    
    command(scanResults)
  }
}