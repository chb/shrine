package net.shrine.adapter.service

import net.shrine.protocol.I2b2AdminRequestHandler
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.util.Loggable
import scala.concurrent.duration.Duration
import net.shrine.adapter.dao.AdapterDao
import net.shrine.adapter.components.QueryDefinitionSourceComponent
import net.shrine.adapter.components.I2b2AdminPreviousQueriesSourceComponent
import net.shrine.protocol.ShrineRequest
import net.shrine.protocol.ErrorResponse

/**
 * @author clint
 * @date Apr 4, 2013
 */
final class I2b2AdminService(override val dao: AdapterDao) extends I2b2AdminRequestHandler with QueryDefinitionSourceComponent with I2b2AdminPreviousQueriesSourceComponent with Loggable {

  //NB: shouldBroadcast is ignored; we never broadcast
  override def readQueryDefinition(request: ReadQueryDefinitionRequest, shouldBroadcast: Boolean): ShrineResponse = {
    checkWithPmAndThen(request) {
      QueryDefinitions.get
    }
  }

  //NB: shouldBroadcast is ignored; we never broadcast
  override def readI2b2AdminPreviousQueries(request: ReadI2b2AdminPreviousQueriesRequest, shouldBroadcast: Boolean): ShrineResponse = {
    checkWithPmAndThen(request) {
      I2b2AdminPreviousQueries.get
    }
  }

  def checkWithPm(request: ShrineRequest) {
    //TODO: Should this return something?  Authorized/NotAuthorized, perhaps?
  }

  def checkWithPmAndThen[Req <: ShrineRequest](request: Req)(f: Req => ShrineResponse): ShrineResponse = {
    try {
      checkWithPm(request)

      f(request)
    } catch {
      case e: Exception => ErrorResponse(s"Error communicating with the PM cell: ${e.getMessage}")
    }
  }
}
