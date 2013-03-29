package net.shrine.protocol.handlers

import net.shrine.protocol.ReadPdoRequest
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait ReadPdoHandler {
  def readPdo(request: ReadPdoRequest, shouldBroadcast: Boolean = true): ShrineResponse
}