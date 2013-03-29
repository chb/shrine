package net.shrine.protocol.handlers

import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.ReadApprovedQueryTopicsRequest

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait ReadApprovedTopicsHandler {
  def readApprovedQueryTopics(request: ReadApprovedQueryTopicsRequest, shouldBroadcast: Boolean = true): ShrineResponse
}