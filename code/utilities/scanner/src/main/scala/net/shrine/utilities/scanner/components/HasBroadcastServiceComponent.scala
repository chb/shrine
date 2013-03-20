package net.shrine.utilities.scanner.components

import net.shrine.broadcaster.BroadcastService

/**
 * @author clint
 * @date Mar 20, 2013
 */
trait HasBroadcastServiceComponent {
  val broadcastService: BroadcastService
}