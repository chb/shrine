package net.shrine.protocol.handlers

import net.shrine.protocol.DeleteQueryRequest
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait DeleteQueryHandler {
  def deleteQuery(request: DeleteQueryRequest, shouldBroadcast: Boolean = true): ShrineResponse
}