package net.shrine.webclient.client.state;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.ObservableList;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.QueryNameIterator;
import net.shrine.webclient.client.util.SimpleObserver;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;

/**
 * 
 * @author clint
 * @date Mar 30, 2012
 */
public final class State {

	private final EventBus eventBus;
	
	private String allExpressionXml = null;

	private final Observable<Map<String, Integer>> allResult = Observable.empty();
	
	// Query group name => QueryGroup (Expression, integer result (patient set
	// size), negated (t/f), start date, end date, min occurrances )
	private final ObservableList<QueryGroup> queries = ObservableList.empty();

	//React to changes in query list by renaming queries (to preserve A ... Z naming)
	@SuppressWarnings("unused")
	private final Observer queryRenamer = new SimpleObserver(queries) {
		@Override
		public void inform() {
			reNameQueries();
		}
	};
	
	//React to changes in query list by firing events
	@SuppressWarnings("unused")
	private final Observer queryGroupListChangeEventForwarder = new SimpleObserver(queries) {
		@Override
		public void inform() {
			fireQueryGroupsChangedEvent();
		}
	};
	
	public State(final EventBus eventBus) {
		super();
		
		Util.requireNotNull(eventBus);
		
		this.eventBus = eventBus;
		
		this.eventBus.addHandler(SingleQueryGroupChangedEvent.getType(), new SingleQueryGroupChangedEventHandler() {
			@Override
			public void handle(final SingleQueryGroupChangedEvent event) {
				fireQueryGroupsChangedEvent();
			}
		});
	}

	public void guardQueryIsPresent(final int id) {
		Util.require(isQueryIdPresent(id));
	}

	public void guardQueryIsNotPresent(final int id) {
		Util.require(!isQueryIdPresent(id));
	}

	public boolean isQueryIdPresent(final int id) {
		for (final QueryGroup group : queries) {
			if (id == group.getId()) {
				return true;
			}
		}

		return false;
	}

	// Make sure queries are named A,B,C,... Z, in that order, with no gaps,
	// always starting from 'A'
	private void reNameQueries() {
		final Iterator<String> newIdIter = new QueryNameIterator();

		for(final QueryGroup group : queries) {
			group.setName(newIdIter.next());
		}
	}
	
	public void removeQuery(final int id) {
		guardQueryIsPresent(id);

		final QueryGroup query = getQuery(id);

		queries.remove(query);
	}

	public int numQueryGroups() {
		return queries.size();
	}

	public void completeAllQuery(final Map<String, Integer> resultsByInstitution) {
		if (Log.isInfoEnabled()) {
			Log.info("Completing query 'All' with: '" + resultsByInstitution + "'");

			for (final Entry<String, Integer> entry : resultsByInstitution.entrySet()) {
				Log.info(entry.getKey() + ": " + entry.getValue());
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
		final QueryGroup newQuery = new QueryGroup(eventBus, "NULL", expr);
	
		queries.add(newQuery);
		
		Log.info("Added query group '" + newQuery.getName() + "' (" + newQuery.getId() + "): " + newQuery.getExpression());

		return newQuery;
	}

	public QueryGroup registerNewQuery(final Expression expr) {

		final QueryGroup newQuery = addNewQuery(expr);

		updateAllExpression();

		return newQuery;
	}

	public void updateAllExpression() {
		allExpressionXml = ExpressionXml.fromQueryGroups(queries);
	}

	public ObservableList<QueryGroup> getQueries() {
		return queries;
	}

	public String getAllExpression() {
		return allExpressionXml;
	}

	public Observable<Map<String, Integer>> getAllResult() {
		return allResult;
	}

	void fireQueryGroupsChangedEvent() {
		State.this.eventBus.fireEvent(new QueryGroupsChangedEvent(queries));
	}
}
