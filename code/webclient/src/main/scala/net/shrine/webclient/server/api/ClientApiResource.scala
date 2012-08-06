package net.shrine.webclient.server.api

import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import net.shrine.webclient.client.domain.OntNode
import net.shrine.webclient.client.domain.TermSuggestion
import net.shrine.webclient.server.MultiInstitutionQueryResult
import net.shrine.webclient.server.OntologyService
import net.shrine.webclient.server.OntologyServiceImpl
import net.shrine.webclient.server.QueryService
import net.shrine.webclient.server.QueryServiceImpl


/**
 * 
 * @author clint
 * Jul 31 2012
 */
@Path("/api")
@Produces(Array(MediaType.APPLICATION_JSON))
final class ClientApiResource(queryService: QueryService, ontologyService: OntologyService) {
  //NB: Needed so Jersey can instantiate this class :(
  def this() = this(ClientApiResource.defaultQueryService, ClientApiResource.defaultOntologyService)
  
  println("Instantiated ClientApiResource")
  
  @GET
  @Path("ontology/suggestions")
  def getSuggestions(
    @QueryParam("typedSoFar") typedSoFar: String,
    @QueryParam("limit") limit: Int): Seq[TermSuggestion] = ontologyService.getSuggestions(typedSoFar, limit)

  @GET
  @Path("ontology/path-to")
  def getPathTo(@QueryParam("term") term: String): Seq[OntNode] = ontologyService.getPathTo(term)

  @GET
  @Path("ontology/children-of")
  def getChildrenFor(@QueryParam("term") term: String): Seq[OntNode] = ontologyService.getChildrenFor(term)
  
  @POST
  @Path("query/submit")
  def queryForBreakdown(@QueryParam("expr") expr: String): MultiInstitutionQueryResult = MultiInstitutionQueryResult(Map("foo" -> java.lang.Integer.valueOf(123), "bar" -> java.lang.Integer.valueOf(987654321)))//queryService.queryForBreakdown(expr)
}

object ClientApiResource {
  //Ugh, done to prevent re-instantiation of these classes on every incoming HTTP request
  private lazy val defaultQueryService: QueryService = new QueryServiceImpl 
  
  private lazy val defaultOntologyService: OntologyService = new OntologyServiceImpl 
}