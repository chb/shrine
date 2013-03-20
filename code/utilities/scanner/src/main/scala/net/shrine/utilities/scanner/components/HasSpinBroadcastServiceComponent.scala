package net.shrine.utilities.scanner.components

import net.shrine.broadcaster.spin.SpinBroadcastService
import org.spin.client.SpinClient

/**
 * @author clint
 * @date Mar 20, 2013
 */
trait HasSpinBroadcastServiceComponent extends HasBroadcastServiceComponent {
  val spinClient: SpinClient
  
  override lazy val broadcastService = new SpinBroadcastService(spinClient) 
}