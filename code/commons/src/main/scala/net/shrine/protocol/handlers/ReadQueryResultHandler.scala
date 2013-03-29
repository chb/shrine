package net.shrine.protocol.handlers

import net.shrine.protocol.ReadQueryResultRequest
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait ReadQueryResultHandler {
  def readQueryResult(request: ReadQueryResultRequest, shouldBroadcast: Boolean = true): ShrineResponse
}