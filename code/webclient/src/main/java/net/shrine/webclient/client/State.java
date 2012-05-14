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

	public void guardQueryIsPresent(final int id) {
		Util.require(isQueryIdPresent(id));
	}

	public void guardQueryIsNotPresent(final int id) {
		Util.require(!isQueryIdPresent(id));
	}

	boolean isQueryIdPresent(final int id) {
		for (final QueryGroup group : queries) {
			if (id == group.getId()) {
				return true;
			}
		}

		return false;
	}

	// TODO: HACK
	// Make sure queries are named A,B,C,... Z, in that order, with no gaps,
	// always starting from 'A'
	@Deprecated
	void reNameQueries() {
		final Iterator<String> newIdIter = new QueryNameIterator();

		for(final QueryGroup group : queries) {
			group.setName(newIdIter.next());
		}
	}
	
	void removeQuery(final int id) {
		guardQueryIsPresent(id);

		final QueryGroup query = getQuery(id);

		queries.remove(query);
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

	public QueryGroup getQuery(final int id) {
		for (final QueryGroup query : queries) {
			if (id == query.getId()) {
				return query;
			}
		}

		throw new IllegalArgumentException("No query with id '" + id + "' exists");
	}

	private QueryGroup addNewQuery(final Expression expr) {
		final QueryGroup newQuery = new QueryGroup("NULL", expr);
	
		queries.add(newQuery);
		
		reNameQueries();

		Log.info("Added query group '" + newQuery.getName() + "' (" + newQuery.getId() + "): " + newQuery.getExpression());

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
