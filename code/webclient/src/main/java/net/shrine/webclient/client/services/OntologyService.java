package net.shrine.webclient.client.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import net.shrine.webclient.client.domain.OntNode;
import net.shrine.webclient.client.domain.TermSuggestion;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public interface OntologyService extends RestService {
	@GET
	@Path("rest/api/ontology/suggestions")
	void getSuggestions(@QueryParam("typedSoFar") final String typedSoFar, 
						@QueryParam("limit") final int limit,
						final MethodCallback<List<TermSuggestion>> callback);
	
	@GET
	@Path("rest/api/ontology/path-to")
	void getPathTo(@QueryParam("term") final String term, final MethodCallback<List<OntNode>> callback);
	
	@GET
	@Path("rest/api/ontology/children-of")
	void getChildrenFor(@QueryParam("term") final String term, final MethodCallback<List<OntNode>> callback);
}
