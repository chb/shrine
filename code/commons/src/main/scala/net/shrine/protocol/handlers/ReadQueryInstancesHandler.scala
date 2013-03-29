package net.shrine.protocol.handlers

import net.shrine.protocol.ReadQueryInstancesRequest
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait ReadQueryInstancesHandler {
  def readQueryInstances(request: ReadQueryInstancesRequest, shouldBroadcast: Boolean = true): ShrineResponse
}