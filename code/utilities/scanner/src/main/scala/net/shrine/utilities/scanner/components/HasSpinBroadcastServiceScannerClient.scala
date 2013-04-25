package net.shrine.utilities.scanner.components

import net.shrine.utilities.scanner.Scanner
import org.spin.client.Credentials
import org.spin.tools.config.DefaultPeerGroups
import org.spin.tools.config.EndpointConfig
import org.spin.client.SpinClientConfig
import net.shrine.utilities.scanner.SpinBroadcastServiceScannerClient
import net.shrine.utilities.scanner.ScanResults

/**
 * @author clint
 * @date Mar 28, 2013
 */
trait HasSpinBroadcastServiceScannerClient { self: HasScannerConfig with Scanner =>
  override lazy val client = {
    val spinCredentials = Credentials(config.authorization.domain, config.authorization.username, config.authorization.credential.value)
    
    val peerGroupToQuery = DefaultPeerGroups.LOCAL.name
    
    val entryPoint = EndpointConfig.soap(config.shrineUrl)
    
    val spinClientConfig = SpinClientConfig(peerGroupToQuery, spinCredentials, entryPoint)
    
    new SpinBroadcastServiceScannerClient(config.projectId, config.authorization, spinClientConfig) with HasSingleThreadExecutionContextComponent 
  }
  
  override def scan(): ScanResults = client.shutdownAfter { self.doScan() }
}