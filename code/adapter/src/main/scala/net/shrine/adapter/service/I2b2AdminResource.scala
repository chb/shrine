package net.shrine.adapter.service

import scala.util.Try
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import javax.ws.rs.core.MediaType
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response
import net.shrine.protocol.HandleableAdminShrineRequest
import net.shrine.protocol.I2b2AdminRequestHandler
import net.shrine.service.annotation.RequestHandler
import net.shrine.util.Loggable
import scala.util.Failure

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
    val builder = Try {
      HandleableAdminShrineRequest.fromI2b2(i2b2Request)
    }.map {
      shrineRequest =>
        info("Running request from user: %s of type %s".format(shrineRequest.authn.username, shrineRequest.requestType.toString))

        val shrineResponse = shrineRequest.handleAdmin(i2b2AdminRequestHandler, shouldBroadcast)

        val responseString = shrineResponse.toI2b2String

        Response.ok.entity(responseString)
    }.recoverWith {
      case e: Exception => { warn("Error handling request", e) ; Failure(e) }
    }.getOrElse {
      //TODO: I'm not sure if this is right; need to see what the legacy client expects to be returned in case of an error
      Response.status(400)
    }
    
    builder.build()
  }
}