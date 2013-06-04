package net.shrine.adapter.service

import net.shrine.adapter.components.I2b2AdminPreviousQueriesSourceComponent
import net.shrine.adapter.components.PmAuthorizerComponent
import net.shrine.adapter.components.PmAuthorizerComponent.Authorized
import net.shrine.adapter.components.PmAuthorizerComponent.NotAuthorized
import net.shrine.adapter.components.PmHttpClientComponent
import net.shrine.adapter.components.QueryDefinitionSourceComponent
import net.shrine.adapter.dao.AdapterDao
import net.shrine.adapter.dao.I2b2AdminPreviousQueriesDao
import net.shrine.protocol.I2b2AdminRequestHandler
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.ShrineRequest
import net.shrine.protocol.ShrineResponse
import net.shrine.util.HttpClient
import net.shrine.util.Loggable
import net.shrine.i2b2.protocol.pm.User


/**
 * @author clint
 * @date Apr 4, 2013
 */
final class I2b2AdminService(
    override val dao: AdapterDao,
	override val i2b2AdminDao: I2b2AdminPreviousQueriesDao,
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

    val authorized = Pm.authorize(request.projectId, Set(User.Roles.Manager), request.authn) 
    
    authorized match {
      case Authorized(user) => f(request) 
      case na: NotAuthorized => na.toErrorResponse
    }
  }
}
