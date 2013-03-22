package net.shrine.utilities.scanner.components

import org.spin.client.RemoteSpinClient

/**
 * @author clint
 * @date Mar 21, 2013
 */
trait HasRemoteSpinClientComponent extends HasSpinClientComponent { self: HasExecutionContextComponent with HasSpinClientConfigComponent =>

  override lazy val spinClient = new RemoteSpinClient(spinClientConfig, executionContext)
}