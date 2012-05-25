package net.shrine.webclient.client.controllers;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.state.QueryGroup;
import net.shrine.webclient.client.state.ReadOnlyQueryGroup;
import net.shrine.webclient.client.state.State;

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
	
	public void moveTerm(final Term term, final int fromQueryId, final int toQueryId) {
		state.guardQueryIsPresent(fromQueryId);
		
		//'moving' a term to the group it already belongs to should have no effect  
		if(fromQueryId == toQueryId) {
			Log.debug("'moving' a term to the group it already belongs to has no effect");
			
			//TODO: HACK, needed to redraw widget if it's dropped on the group it already belongs to;
			//this would normally be done by gwt-dnd, but it's UI restoration mechanism doesn't kick in 
			//when something is dropped on a legitimate drop target.
			state.getQueries().notifyObservers();
			
			return;
		}
		
		if(toQueryId != QueryGroup.NullId) {
			state.guardQueryIsPresent(toQueryId);
			
			addNewTerm(toQueryId, term);
			
			removeTerm(fromQueryId, term);
		} else {
			final ReadOnlyQueryGroup fromQueryGroup = state.getQuery(fromQueryId);
			
			if(fromQueryGroup.getExpression().getTerms().size() > 1) {
				addNewTerm(term);
				
				removeTerm(fromQueryId, term);
			} else {
				state.getQueries().notifyObservers();
			}
		}
	}
	
	public ReadOnlyQueryGroup addNewTerm(final int id, final Term newTerm) {
		final QueryGroup queryGroup = state.getQuery(id);
		
		final Expression expr = queryGroup.getExpression();
		
		if(expr instanceof Term) {
			if(expr.equals(newTerm)) {
				Log.warn("Attempted to add term to query that already contains that term. Query: '" + queryGroup.getId() + "' Term: " + newTerm);
			} else {
				final Term existing = (Term)expr;
				
				queryGroup.setExpression(new Or(existing, newTerm));
			}
		} else if(expr instanceof Or) {
			final Or withNewTerm = ((Or)expr).with(newTerm);
			
			if(expr.equals(withNewTerm)) {
				Log.warn("Adding term to query '" + queryGroup.getId() + "' has no effect.  Term: " + newTerm);
			} else {
				queryGroup.setExpression(withNewTerm);
			}
		} else {
			throw new IllegalStateException("Query group '" + queryGroup.getId() + "' has illegal expression: " + expr);
		}
		
		return queryGroup;
	}
	
	public void removeAllQueryGroups() {
		state.getQueries().clear();
	}
	
	public void removeQueryGroup(final int id) {
		state.guardQueryIsPresent(id);
		
		state.removeQuery(id);
	}
	
	public void removeTerm(final int queryId, final Term term) {
		state.guardQueryIsPresent(queryId);
		
		Log.debug("Removing term from query " + queryId + " known queries are: " + state.getQueries());
		
		final QueryGroup queryGroup = state.getQuery(queryId);
		
		final Expression expr = queryGroup.getExpression();
		
		if(expr instanceof Term) {
			if(!expr.equals(term)) {
				Log.warn("Attempted to remove nonexistent term from query '" + queryGroup.getId() + "': " + term);
			} else {
				removeQueryGroup(queryId);
			}
		} else if(expr instanceof Or) {
			final Or withoutTerm = ((Or)expr).without(term);
			
			if(expr.equals(withoutTerm)) {
				Log.warn("Removing term from query '" + queryId + "' has no effect: " + term);
			} else if(withoutTerm.size() == 1) {
				queryGroup.setExpression(withoutTerm.getTerms().iterator().next());
			} else if(withoutTerm.isEmpty()) {
				removeQueryGroup(queryId);
			} else {
				queryGroup.setExpression(withoutTerm);
			}
		} else {
			throw new IllegalStateException("Query group '" + queryGroup.getId() + "' has illegal expression: " + expr);
		}
	}
}
