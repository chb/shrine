package net.shrine.webclient.client;

import java.util.HashMap;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 * 
 */
public final class QueryController extends StatefulController {

	private final QueryServiceAsync queryService = GWT.create(QueryService.class);

	public QueryController(final State state) {
		super(state);
	}
	
	public void runEveryQuery() {
		for(final String queryName : state.getQueries().keySet()) {
			state.getQueries().get(queryName).getResult().clear();
			
			runQuery(queryName);
		}
		
		state.getAllResult().clear();
		
		doAllQuery();
	}
	
	private class AllQueryStrategy {
		final void completeQuery(final String queryName, final HashMap<String, IntWrapper> result) {
			state.completeQuery(queryName, result);

			completeAllQuery(result);
		}

		void completeAllQuery(final HashMap<String, IntWrapper> result) {
			// NOOP by default
		}
	}

	private final AllQueryStrategy CompleteAllQuery = new AllQueryStrategy() {
		@Override
		void completeAllQuery(final HashMap<String, IntWrapper> result) {
			state.completeAllQuery(result);
		}
	};

	private final AllQueryStrategy DoNotCompleteAllQuery = new AllQueryStrategy();

	private AllQueryStrategy determineAllQueryStrategyFromState() {
		return state.numQueryGroups() == 1 ? CompleteAllQuery : DoNotCompleteAllQuery;
	}

	public void runQuery(final String queryName, final Expression expression) {
		Util.require(state.getQueries().containsKey(queryName));

		runQuery(queryName, expression.toXmlString());
	}

	public void runQuery(final String queryName) {
		Util.require(state.getQueries().containsKey(queryName));

		final String queryXml = state.getQueries().get(queryName).toXmlString();

		runQuery(queryName, queryXml);
	}

	void runQuery(final String queryName, final String queryXml) {
		Log.info("Performing query '" + queryName + "'");

		Log.debug("Query XML for '" + queryName + "': " + queryXml);

		final AllQueryStrategy allQueryStrategy = determineAllQueryStrategyFromState();

		doQuery(queryName, queryXml, allQueryStrategy);
	}

	private void doQuery(final String queryName, final String queryXml, final AllQueryStrategy allQueryStrategy) {
		queryService.queryForBreakdown(queryXml, new AsyncCallback<HashMap<String, IntWrapper>>() {
			@Override
			public void onSuccess(final HashMap<String, IntWrapper> result) {
				// TODO: can result be null?
				allQueryStrategy.completeQuery(queryName, result);
			}

			@Override
			public void onFailure(final Throwable caught) {
				Log.error("Error making query '" + queryName + "': " + caught.getMessage(), caught);

				// TODO: Is empty breakdown map appropriate?
				allQueryStrategy.completeQuery(queryName, noResults());
			}
		});
	}

	private void doAllQuery() {
		state.updateAllExpression();
		
		Log.debug("Query XML for '" + QueryGroupNames.All + "': " + state.getAllExpression());
		
		queryService.queryForBreakdown(state.getAllExpression(), new AsyncCallback<HashMap<String, IntWrapper>>() {
			@Override
			public void onSuccess(final HashMap<String, IntWrapper> result) {
				state.completeAllQuery(result);
			}

			@Override
			public void onFailure(final Throwable caught) {
				Log.error("Error making query '" + QueryGroupNames.All + "': " + caught.getMessage(), caught);

				//TODO: Is empty map appropriate here?
				state.completeAllQuery(noResults());
			}
		});
	}

	private static final HashMap<String, IntWrapper> noResults() {
		return new HashMap<String, IntWrapper>();
	}
}
