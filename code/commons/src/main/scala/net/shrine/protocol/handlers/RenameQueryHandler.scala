package net.shrine.protocol.handlers

import net.shrine.protocol.RenameQueryRequest
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait RenameQueryHandler {
  def renameQuery(request: RenameQueryRequest, shouldBroadcast: Boolean = true): ShrineResponse
}