package net.shrine.webclient.client.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */

public interface QueryService extends RestService {
	@POST
	@Path("rest/api/query/submit")
	void performQuery(final String expr, final MethodCallback<MultiInstitutionQueryResult> callback);
}

