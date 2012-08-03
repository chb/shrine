package net.shrine.webclient.server.api

import java.util.{List => JList}
import java.util.{HashMap => JHashMap}
import net.shrine.webclient.client.services.QueryService
import net.shrine.webclient.client.services.OntologySearchService
import net.shrine.webclient.server.OntologySearchServiceImpl
import net.shrine.webclient.server.QueryServiceImpl
import javax.ws.rs.{GET, POST}
import javax.ws.rs.Path
import javax.ws.rs.QueryParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import net.shrine.webclient.client.domain.OntNode
import net.shrine.webclient.client.domain.TermSuggestion
import net.shrine.webclient.client.domain.IntWrapper


/**
 * 
 * @author clint
 * Jul 31 2012
 */
@Path("/api")
@Produces(Array(MediaType.APPLICATION_JSON))
final class ClientApiResource(val queryService: QueryService, val ontologyService: OntologySearchService) {
  //NB: Needed so Jersey can instantiate this class :(
  def this() = this(new QueryServiceImpl, new OntologySearchServiceImpl)
  
  @GET
  @Path("ontology/suggestions")
  def getSuggestions(
    @QueryParam("typedSoFar") typedSoFar: String,
    @QueryParam("limit") limit: Int): JList[TermSuggestion] = ontologyService.getSuggestions(typedSoFar, limit)

  @GET
  @Path("ontology/path-to")
  def getPathTo(@QueryParam("term") term: String): JList[OntNode] = ontologyService.getPathTo(term)

  @GET
  @Path("ontology/children-of")
  def getChildrenFor(@QueryParam("term") term: String): JList[OntNode] = ontologyService.getChildrenFor(term)
  
  @POST
  @Path("query/submit")
  def queryForBreakdown(@QueryParam("expr") expr: String): JHashMap[String, IntWrapper] = queryService.queryForBreakdown(expr)
}