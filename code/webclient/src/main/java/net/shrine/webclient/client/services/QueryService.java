package net.shrine.webclient.client.services;

import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

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
	void queryForBreakdown(final String expr, final MethodCallback<Map<String, Integer>> callback);
}

