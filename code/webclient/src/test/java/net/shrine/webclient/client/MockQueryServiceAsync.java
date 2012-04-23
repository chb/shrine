package net.shrine.webclient.client;

import java.util.HashMap;

import net.shrine.webclient.client.domain.IntWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author clint
 * @date Apr 23, 2012
 */
public final class MockQueryServiceAsync implements QueryServiceAsync {
	public Integer totalToReturn = total;
	
	public HashMap<String, IntWrapper> multiNodeResultsToReturn = multiNodeResults;
	
	public String lastExpr = null; 
	
	static final Integer total = 99;
	
	@SuppressWarnings("serial")
	static final HashMap<String, IntWrapper> multiNodeResults = new HashMap<String, IntWrapper>() {{
		this.put("foo", new IntWrapper(99));
		this.put("bar", new IntWrapper(42));
	}};
	
	@Override
	public void query(final String expr, final AsyncCallback<Integer> callback) {
		lastExpr = expr;
		
		callback.onSuccess(totalToReturn);
	}

	@Override
	public void queryForBreakdown(final String expr, final AsyncCallback<HashMap<String, IntWrapper>> callback) {
		lastExpr = expr;
		
		callback.onSuccess(multiNodeResultsToReturn);
	}
}