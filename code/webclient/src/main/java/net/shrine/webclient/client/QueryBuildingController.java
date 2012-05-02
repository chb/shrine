package net.shrine.webclient.client;

import java.util.Iterator;
import java.util.List;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.domain.ReadOnlyQueryGroup;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;

/**
 * 
 * @author clint
 * @date Mar 27, 2012
 */
public final class QueryBuildingController extends StatefulController {
	
	public QueryBuildingController(final State state) {
		super(state);
	}

	public int getNumQueryGroups() {
		return state.numQueryGroups();
	}
	
	public void reNameQueries() {
		final List<QueryGroup> queries = Util.makeArrayList(state.getQueries());
		
		final Iterator<QueryGroup> queryIter = queries.iterator();
		
		final Iterator<QueryGroupId> newIdIter = Util.take(queries.size(), new QueryGroupIdsIterator()).iterator();
		
		while(queryIter.hasNext() && newIdIter.hasNext()) {
			final QueryGroupId id = newIdIter.next();
			
			final QueryGroup group = queryIter.next();
			
			group.setId(id);
		}
	}
	
	public ReadOnlyQueryGroup addNewTerm(final Term term) {
		final QueryGroup newQuery = state.registerNewQuery(QueryGroupId.Null, term);
		
		reNameQueries();
		
		return newQuery;
	}
	
	public void removeAllQueryGroups() {
		//NB: Defensively copy state.getQueries() to avoid ConcurrentModificationException
		for(final QueryGroup group : Util.makeArrayList(state.getQueries())) {
			state.getQueries().remove(group);
		}
	}
	
	public void removeQueryGroup(final QueryGroupId id) {
		state.guardQueryIsPresent(id);
		
		final QueryGroup query = state.getQuery(id);
		
		state.getQueries().remove(query);
		
		state.guardQueryIsNotPresent(id);
	}
	
	public void removeTerm(final QueryGroupId queryId, final Term term) {
		state.guardQueryIsPresent(queryId);
		
		Log.debug("Removing term from query " + queryId + " known queries are: " + state.getQueries());
		
		final QueryGroup queryGroup = state.getQuery(queryId);
		
		final Expression expr = queryGroup.getExpression();
		
		if(expr instanceof Term) {
			if(!expr.equals(term)) {
				Log.warn("Attempted to remove nonexistent term from query '" + queryId.name + "': " + term);
			} else {
				removeQueryGroup(queryId);
			}
		} else if(expr instanceof Or) {
			final Or withoutTerm = ((Or)expr).without(term);
			
			if(expr.equals(withoutTerm)) {
				Log.warn("Removing term from query '" + queryId.name + "' has no effect: " + term);
			} else if(withoutTerm.isEmpty()) {
				removeQueryGroup(queryId);
			} else {
				queryGroup.setExpression(withoutTerm);
			}
		} else {
			throw new IllegalStateException("Query group '" + queryId.name + "' has illegal expression: " + expr);
		}
	}
}
