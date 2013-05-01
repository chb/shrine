package net.shrine.service

import scala.util.Try
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response
import net.shrine.util.Loggable
import javax.ws.rs.core.MediaType
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ShrineRequestHandler
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.HandleableShrineRequest

/**
 * @author Bill Simons
 * @date 3/10/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
@Path("/i2b2")
@Produces(Array(MediaType.APPLICATION_XML))
@Component
@Scope("singleton")
final class I2b2BroadcastResource @Autowired() (private val shrineRequestHandler: ShrineRequestHandler) extends Loggable {

  //NB: Always broadcast when receiving requests from the legacy i2b2/Shrine webclient, since we can't retrofit it to 
  //Say whether broadcasting is desired for a praticular query/operation
  val shouldBroadcast = true
  
  @POST
  @Path("request")
  def doRequest(i2b2Request: String): Response = processI2b2Message(i2b2Request)

  @POST
  @Path("pdorequest")
  def doPDORequest(i2b2Request: String): Response = processI2b2Message(i2b2Request)

  def processI2b2Message(i2b2Request: String): Response = {
    val builder = Try {
      HandleableShrineRequest.fromI2b2(i2b2Request)
    }.map {
      shrineRequest =>
        info("Running request from user: %s of type %s".format(shrineRequest.authn.username, shrineRequest.requestType.toString))

        val shrineResponse = shrineRequest.handle(shrineRequestHandler, shouldBroadcast)

        val responseString = shrineResponse.toI2b2String

        Response.ok.entity(responseString)
    }.getOrElse { 
      //TODO: I'm not sure if this is right; need to see what the legacy client expects to be returned in case of an error
      Response.status(400)
    }
    
    builder.build()
  }
}