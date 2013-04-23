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
import net.shrine.adapter.components.PmAuthorizerComponent
import net.shrine.util.HttpClient
import net.shrine.adapter.components.PmHttpClientComponent

/**
 * @author clint
 * @date Apr 4, 2013
 */
final class I2b2AdminService(
	override val dao: AdapterDao,
	override val httpClient: HttpClient,
	override val pmEndpoint: String) extends 
		I2b2AdminRequestHandler with 
		QueryDefinitionSourceComponent with 
		I2b2AdminPreviousQueriesSourceComponent with 
		PmAuthorizerComponent with
		PmHttpClientComponent with
		Loggable {
  
  require(dao != null)
  require(httpClient != null)

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

  def checkWithPmAndThen[Req <: ShrineRequest](request: Req)(f: Req => ShrineResponse): ShrineResponse = {
    import PmAuthorizerComponent._

    Pm.authorize(request.authn) match {
      case Authorized(user) => f(request) //TODO: do something with user; check that user has proper roles 
      case na: NotAuthorized => na.toErrorResponse
    }
  }
}
