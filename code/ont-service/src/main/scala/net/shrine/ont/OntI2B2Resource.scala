package net.shrine.ont

import javax.ws.rs.core.{Response, MediaType}
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import scala.xml.XML
import javax.ws.rs.{QueryParam, GET, Path, POST, Produces}

/**
 * @author Justin Quan
 * @date 9/1/11
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
@Path("/")
@Produces(Array(MediaType.APPLICATION_XML))
@Component
@Scope("singleton")
final class OntI2B2Resource @Autowired()(private val ontRequestHandler: OntRequestHandler) {
  @GET
  @Path("search")
  @Produces(Array("application/json"))
  def searchJson(@QueryParam("q") searchString: String) = {
    val responseString = ontRequestHandler.search(searchString).toJsonString
    
    Response.ok.entity(responseString).build()
  }
}