package net.shrine.utilities.scanner

import net.shrine.utilities.scanner.components.HasSpinBroadcastServiceComponent
import net.shrine.utilities.scanner.components.HasExecutionContextComponent
import net.shrine.utilities.scanner.components.HasRemoteSpinClientComponent
import net.shrine.utilities.scanner.components.HasSpinClientConfigComponent
import org.spin.client.SpinClientConfig
import net.shrine.protocol.AuthenticationInfo

/**
 * @author clint
 * @date Mar 21, 2013
 */
abstract class SpinBroadcastServiceScannerClient(
    override val projectId: String,
    override val authn: AuthenticationInfo,
    override val spinClientConfig: SpinClientConfig) extends 
      	BroadcastServiceScannerClient(projectId, authn) with 
      	HasSpinBroadcastServiceComponent with 
      	HasExecutionContextComponent with
      	HasRemoteSpinClientComponent with 
      	HasSpinClientConfigComponent