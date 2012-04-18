package net.shrine.service

import javax.ws.rs.core.{Response, MediaType}
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Scope
import xml.XML
import org.springframework.beans.factory.annotation.Autowired
import javax.ws.rs.{PathParam, POST, Produces, Path}
import net.shrine.protocol.{ErrorResponse, ShrineRequestHandler, ShrineRequest}
import net.shrine.util.Loggable


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
class I2b2Resource @Autowired()(private val shrineRequestHandler: ShrineRequestHandler) extends Loggable {

  @POST @Path("request")
  def doRequest(i2b2Request: String): Response = {
    processI2b2Message(i2b2Request)
  }

  @POST @Path("pdorequest")
  def doPDORequest(i2b2Request: String): Response = {
    processI2b2Message(i2b2Request)
  }

  def processI2b2Message(i2b2Request: String): Response = {
    val shrineRequest = ShrineRequest.fromI2b2(XML.loadString(i2b2Request))
    info("Running request from user: %s of type %s".format(shrineRequest.authn.username,shrineRequest.requestType.toString))
    val shrineResponse = shrineRequest.handle(shrineRequestHandler)
    val responseString = shrineResponse.toI2b2.toString
    Response.ok().entity(responseString).build()
  }

}