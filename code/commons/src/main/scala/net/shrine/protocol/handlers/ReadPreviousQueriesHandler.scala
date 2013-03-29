package net.shrine.protocol.handlers

import net.shrine.protocol.ReadPreviousQueriesRequest
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait ReadPreviousQueriesHandler {
  def readPreviousQueries(request: ReadPreviousQueriesRequest, shouldBroadcast: Boolean = true): ShrineResponse
}