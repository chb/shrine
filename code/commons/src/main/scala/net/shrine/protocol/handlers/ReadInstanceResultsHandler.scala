package net.shrine.protocol.handlers

import net.shrine.protocol.ReadInstanceResultsRequest
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait ReadInstanceResultsHandler {
  def readInstanceResults(request: ReadInstanceResultsRequest, shouldBroadcast: Boolean = true): ShrineResponse
}