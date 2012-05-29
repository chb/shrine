package net.shrine.webclient.client.controllers;

import java.util.HashMap;
import java.util.Map;

import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.services.QueryServiceAsync;
import net.shrine.webclient.client.state.State;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 * 
 */
public final class QueryController extends StatefulController {

	private final QueryServiceAsync queryService;

	public QueryController(final State state, final QueryServiceAsync queryService) {
		super(state);
		
		Util.requireNotNull(queryService);
		
		this.queryService = queryService;
	}
	
	public void runEveryQuery() {
		
		state.getAllResult().clear();
		
		runAllQuery();
	}

	public void runAllQuery() {
		state.updateAllExpression();
		
		state.getAllResult().clear();
		
		Log.debug("Query XML for 'All': " + state.getAllExpression());
		
		queryService.queryForBreakdown(state.getAllExpression(), new AsyncCallback<HashMap<String, IntWrapper>>() {
			@Override
			public void onSuccess(final HashMap<String, IntWrapper> result) {
				state.completeAllQuery(result);
			}

			@Override
			public void onFailure(final Throwable caught) {
				Log.error("Error making query 'All': " + caught.getMessage(), caught);

				completeAllQueryWithNoResults();
			}
		});
	}

	public void completeAllQueryWithNoResults() {
		state.completeAllQuery(noResults());
	}
	
	private static Map<String, IntWrapper> noResults() {
		return new HashMap<String, IntWrapper>();
	}
}
