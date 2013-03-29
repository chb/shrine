package net.shrine.protocol.handlers

import net.shrine.protocol.RunQueryRequest
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait RunQueryHandler {
  def runQuery(request: RunQueryRequest, shouldBroadcast: Boolean = true): ShrineResponse
}