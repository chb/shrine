package net.shrine.adapter.service

import javax.ws.rs.Path
import javax.ws.rs.Produces
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Scope
import javax.ws.rs.core.MediaType
import org.springframework.beans.factory.annotation.Autowired
import net.shrine.util.Loggable
import net.shrine.protocol.I2b2AdminRequestHandler
import javax.ws.rs.POST
import javax.ws.rs.core.Response
import scala.util.Try
import net.shrine.protocol.DoubleDispatchingShrineRequest
import net.shrine.protocol.HandledByI2b2AdminRequestHandler
import net.shrine.service.annotation.RequestHandler
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.handlers.ReadI2b2AdminPreviousQueriesHandler
import net.shrine.protocol.ReadQueryDefinitionRequest

/**
 * @author clint
 * @date Apr 3, 2013
 */
@Path("/i2b2/admin")
@Produces(Array(MediaType.APPLICATION_XML))
@Component
@Scope("singleton")
class I2b2AdminResource @Autowired() (@RequestHandler i2b2AdminRequestHandler: I2b2AdminRequestHandler) extends Loggable {
  //NB: Never broadcast when receiving requests from the legacy i2b2/Shrine webclient, since we can't retrofit it to 
  //Say whether broadcasting is desired for a praticular query/operation
  val shouldBroadcast = false
  
  @POST
  @Path("request")
  final def doRequest(i2b2Request: String): Response = {
    Try {
      DoubleDispatchingShrineRequest.fromI2b2(i2b2Request)
    }.toOption.collect { 
      //TODO: VERY Ugly. :(
      case req: ReadI2b2AdminPreviousQueriesRequest => req
      case req: ReadQueryDefinitionRequest => req
    }.map {
      shrineRequest =>
        info("Running request from user: %s of type %s".format(shrineRequest.authn.username, shrineRequest.requestType.toString))

        val shrineResponse = shrineRequest match {
          case req: ReadI2b2AdminPreviousQueriesRequest => req.handle(i2b2AdminRequestHandler, shouldBroadcast)
          case req: ReadQueryDefinitionRequest => req.handle(i2b2AdminRequestHandler, shouldBroadcast)
        }

        val responseString = shrineResponse.toI2b2String

        Response.ok.entity(responseString).build()
    }.getOrElse { 
      //TODO: I'm not sure if this is right; need to see what the legacy client expects to be returned in case of an error
      Response.status(400).build()
    }
  }
}