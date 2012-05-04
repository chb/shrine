package net.shrine.webclient.client;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.domain.ReadOnlyQueryGroup;
import net.shrine.webclient.client.domain.Term;

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
	
	public ReadOnlyQueryGroup addNewTerm(final Term term) {
		return state.registerNewQuery(term);
	}
	
	public ReadOnlyQueryGroup addNewTerm(final QueryGroupId id, final Term newTerm) {
		final QueryGroup queryGroup = state.getQuery(id);
		
		final Expression expr = queryGroup.getExpression();
		
		if(expr instanceof Term) {
			if(expr.equals(newTerm)) {
				Log.warn("Attempted to add term to query that already contains that term. Query: '" + id.name + "' Term: " + newTerm);
			} else {
				final Term existing = (Term)expr;
				
				queryGroup.setExpression(new Or(existing, newTerm));
			}
		} else if(expr instanceof Or) {
			final Or withNewTerm = ((Or)expr).with(newTerm);
			
			if(expr.equals(withNewTerm)) {
				Log.warn("Adding term to query '" + id.name + "' has no effect.  Term: " + newTerm);
			} else {
				queryGroup.setExpression(withNewTerm);
			}
		} else {
			throw new IllegalStateException("Query group '" + id.name + "' has illegal expression: " + expr);
		}
		
		return queryGroup;
	}
	
	public void removeAllQueryGroups() {
		state.getQueries().clear();
	}
	
	public void removeQueryGroup(final QueryGroupId id) {
		state.guardQueryIsPresent(id);
		
		state.removeQuery(id);
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
			} else if(withoutTerm.size() == 1) {
				queryGroup.setExpression(withoutTerm.getTerms().iterator().next());
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
