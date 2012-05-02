package net.shrine.webclient.client;

import java.util.HashMap;
import java.util.Iterator;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.ExpressionXml;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.ObservableList;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;

/**
 * 
 * @author clint
 * 
 */
public final class State {

	private String allExpressionXml = null;

	private final Observable<HashMap<String, IntWrapper>> allResult = Observable.empty();

	// Query group name => QueryGroup (Expression, integer result (patient set
	// size), negated (t/f), start date, end date, min occurrances )
	private final ObservableList<QueryGroup> queries = ObservableList.empty();

	public void guardQueryIsPresent(final QueryGroupId id) {
		Util.require(isQueryIdPresent(id));
	}

	public void guardQueryIsNotPresent(final QueryGroupId id) {
		Util.require(!isQueryIdPresent(id));
	}

	boolean isQueryIdPresent(final QueryGroupId id) {
		for (final QueryGroup group : queries) {
			if (id.equals(group.getId())) {
				return true;
			}
		}

		return false;
	}

	void removeQuery(final QueryGroupId id) {
		guardQueryIsPresent(id);

		try {
			final QueryGroup query = getQuery(id);

			queries.remove(query);
		} finally {
			reNameQueries();
		}
	}

	// TODO: HACK
	// Make sure queries are named A,B,C,... Z, in that order, with no gaps,
	// always starting from 'A'
	@Deprecated
	void reNameQueries() {
		final Iterator<QueryGroupId> newIdIter = new QueryGroupIdsIterator();

		for(final QueryGroup group : queries) {
			group.setId(newIdIter.next());
		}
	}

	public int numQueryGroups() {
		return queries.size();
	}

	public void completeAllQuery(final HashMap<String, IntWrapper> resultsByInstitution) {
		if (Log.isInfoEnabled()) {
			Log.info("Completing query 'All' with: '" + resultsByInstitution + "'");

			for (final String instName : resultsByInstitution.keySet()) {
				Log.info(instName + ": " + resultsByInstitution.get(instName));
			}
		}

		allResult.set(resultsByInstitution);
	}

	public QueryGroup getQuery(final QueryGroupId id) {
		for (final QueryGroup query : queries) {
			if (id.equals(query.getId())) {
				return query;
			}
		}

		throw new IllegalArgumentException("No query named '" + id.name + "' exists");
	}

	private QueryGroup addNewQuery(final Expression expr) {
		final QueryGroup newQuery = new QueryGroup(QueryGroupId.Null, expr);

		queries.add(newQuery);

		Log.info("Added query group '" + newQuery.getId().name + "': " + newQuery.getExpression());

		reNameQueries();

		return newQuery;
	}

	public QueryGroup registerNewQuery(final Expression expr) {

		final QueryGroup newQuery = addNewQuery(expr);

		updateAllExpression();

		return newQuery;
	}

	void updateAllExpression() {
		allExpressionXml = ExpressionXml.fromQueryGroups(queries);
	}

	public ObservableList<QueryGroup> getQueries() {
		return queries;
	}

	public String getAllExpression() {
		return allExpressionXml;
	}

	public Observable<HashMap<String, IntWrapper>> getAllResult() {
		return allResult;
	}
}
