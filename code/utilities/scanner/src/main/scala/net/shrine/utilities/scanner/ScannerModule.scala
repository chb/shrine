package net.shrine.utilities.scanner

import java.io.FileInputStream
import org.spin.client.Credentials
import org.spin.client.SpinClientConfig
import org.spin.tools.config.DefaultPeerGroups
import org.spin.tools.config.EndpointConfig
import com.typesafe.config.ConfigFactory
import net.shrine.config.FileSystemAdapterMappingsSource
import net.shrine.util.Versions
import net.shrine.utilities.scanner.components.HasSingleThreadExecutionContextComponent
import net.shrine.utilities.scanner.components.HasFileSystemAdapterMappingsSource
import net.shrine.utilities.scanner.components.HasFileSystemShrineSqlOntologyDao
import net.shrine.utilities.scanner.components.HasSpinBroadcastServiceScannerClient
import net.shrine.utilities.scanner.components.HasScannerConfig
import net.shrine.utilities.scanner.components.HasClasspathAndCommandLineScannerConfig
import net.shrine.utilities.scanner.components.HasClasspathAndCommandLineScannerConfig
import net.shrine.utilities.scanner.components.HasCommandLineConfig
import net.shrine.utilities.scanner.components.HasArgs
import net.shrine.utilities.scanner.components.HasReScanTimeoutFromConfig

/**
 * @author clint
 * @date Mar 6, 2013
 */
final class ScannerModule(val args: Seq[String]) extends 
	Scanner with
	HasArgs with
	HasCommandLineConfig with
	HasClasspathAndCommandLineScannerConfig with
	HasReScanTimeoutFromConfig with
	HasFileSystemAdapterMappingsSource with 
	HasFileSystemShrineSqlOntologyDao with 
	HasSpinBroadcastServiceScannerClient

object ScannerModule {
  def printVersionInfo() {
    println(s"Shrine Scanner version: ${ Versions.version}")
    println(s"Built on ${ Versions.buildDate }")
    println(s"SCM branch: ${ Versions.scmBranch }")
    println(s"SCM revision: ${ Versions.scmRevision }")
    println()
  }
  
  def main(args: Array[String]) {
    
    val scanner = new ScannerModule(args)
    
    if(scanner.showVersionToggleEnabled) {
      printVersionInfo()
    }
    
    if(scanner.showHelpToggleEnabled) {
      println("Usage: scanner [options]")
      
      scanner.commandLineProps.printHelp()
      
      System.exit(0)
    }
    
    /*val config = ScannerConfig("testAdapterMappings.xml", 
    						   "testShrineWithSyns.sql",
    						   10.seconds, 
    						   "https://shrine-dev1.chip.org:6060/shrine-cell/soap/aggregate?wsdl", 
    						   "SHRINE", 
    						   AuthenticationInfo("HarvardDemo", "bsimons", Credential("testtest", false)))*/
    
    val scanResults = scanner.scan()
    
    val command = Output.to("foo.csv")
    
    command(scanResults)
  }
}