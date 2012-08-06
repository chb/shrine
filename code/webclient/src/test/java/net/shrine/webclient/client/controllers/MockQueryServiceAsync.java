package net.shrine.webclient.client.controllers;

import java.util.HashMap;
import java.util.Map;

import net.shrine.webclient.client.services.QueryService;

import org.fusesource.restygwt.client.MethodCallback;

/**
 * 
 * @author clint
 * @date Apr 23, 2012
 */
public final class MockQueryServiceAsync implements QueryService {
	public Integer totalToReturn = total;
	
	public Map<String, Integer> multiNodeResultsToReturn = multiNodeResults;
	
	public String lastExpr = null; 
	
	static final Integer total = 99;
	
	@SuppressWarnings("serial")
	static final HashMap<String, Integer> multiNodeResults = new HashMap<String, Integer>() {{
		this.put("foo", Integer.valueOf(99));
		this.put("bar", Integer.valueOf(42));
	}};
	
	@Override
	public void queryForBreakdown(final String expr, final MethodCallback<Map<String, Integer>> callback) {
		lastExpr = expr;
		
		callback.onSuccess(null, multiNodeResultsToReturn);
	}
}