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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Scope
import net.shrine.webclient.server.BootstrapInfoSource
import net.shrine.webclient.client.domain.BootstrapInfo


/**
 * 
 * @author clint
 * Jul 31 2012
 */
@Path("/api")
@Produces(Array(MediaType.APPLICATION_JSON))
@Component
@Scope("singleton")
final class ClientApiResource @Autowired()(@Injectable queryService: QueryService, 
                                            @Injectable ontologyService: OntologyService,
                                            @Injectable bootstrapInfoSource: BootstrapInfoSource) {

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
  def queryForBreakdown(expr: String): MultiInstitutionQueryResult = queryService.queryForBreakdown(expr)
  
  @GET
  @Path("bootstrap")
  def getBootstrapInfo: BootstrapInfo = bootstrapInfoSource.bootstrapInfo
}