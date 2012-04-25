package net.shrine.webclient.client;

import java.util.HashMap;

import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.domain.QueryGroup;
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
		for(final String queryName : state.getQueries().keySet()) {
			state.getQueries().get(queryName).getResult().clear();
			
			runQuery(queryName);
		}
		
		state.getAllResult().clear();
		
		if(state.numQueryGroups() == 1) {
			final QueryGroup onlyGroup = findOnlyQueryGroup();
			
			if(onlyGroup.getResult().isDefined()) {
				state.completeAllQuery(onlyGroup.getResult().get());
			}
		} else {
			runAllQuery();
		}
	}

	void runQuery(final String queryName) {
		Util.require(state.getQueries().containsKey(queryName));

		final String queryXml = state.getQueries().get(queryName).toXmlString();

		Log.info("Performing query '" + queryName + "'");

		Log.debug("Query XML for '" + queryName + "': " + queryXml);

		doQuery(queryName, queryXml);
	}

	private void doQuery(final String queryName, final String queryXml) {
		queryService.queryForBreakdown(queryXml, new AsyncCallback<HashMap<String, IntWrapper>>() {
			@Override
			public void onSuccess(final HashMap<String, IntWrapper> result) {
				// TODO: can result be null?
				state.completeQuery(queryName, result);
			}

			@Override
			public void onFailure(final Throwable caught) {
				Log.error("Error making query '" + queryName + "': " + caught.getMessage(), caught);

				// TODO: Is empty breakdown map appropriate?
				state.completeQuery(queryName, noResults());
			}
		});
	}

	private QueryGroup findOnlyQueryGroup() {
		Util.require(state.numQueryGroups() == 1);
		
		return state.getQueries().entrySet().iterator().next().getValue();
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

				//TODO: Is empty map appropriate here?
				state.completeAllQuery(noResults());
			}
		});
	}

	private static final HashMap<String, IntWrapper> noResults() {
		return new HashMap<String, IntWrapper>();
	}
}
