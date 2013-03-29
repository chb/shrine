package net.shrine.protocol.handlers

import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.ReadQueryDefinitionRequest

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait ReadQueryDefinitionHandler {
  def readQueryDefinition(request: ReadQueryDefinitionRequest, shouldBroadcast: Boolean = true): ShrineResponse
}