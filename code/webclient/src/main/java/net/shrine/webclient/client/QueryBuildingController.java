package net.shrine.webclient.client;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.QueryGroup;
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

	public String addNewTerm(final Term term) {
		final String name = state.getQueryGroupNames().next();

		state.registerNewQuery(name, term);

		return name;
	}
	
	public void removeQueryGroup(final String queryName) {
		state.guardQueryNameIsPresent(queryName);
		
		state.getQueries().remove(queryName);
	}
	
	public void removeTerm(final String queryName, final Term term) {
		state.guardQueryNameIsPresent(queryName);
		
		final QueryGroup queryGroup = state.getQueries().get(queryName);
		
		final Expression expr = queryGroup.getExpression();
		
		if(expr instanceof Term) {
			if(!expr.equals(term)) {
				Log.warn("Attempted to remove nonexistent term from query '" + queryName + "': " + term);
			} else {
				removeQueryGroup(queryName);
			}
		} else if(expr instanceof Or) {
			final Or withoutTerm = ((Or)expr).without(term);
			
			if(expr.equals(withoutTerm)) {
				Log.warn("Removing term from query '" + queryName + "' has no effect: " + term);
			} else if(withoutTerm.isEmpty()) {
				removeQueryGroup(queryName);
			} else {
				queryGroup.setExpression(withoutTerm);
			}
		} else {
			throw new IllegalStateException("Query group '" + queryName + "' has illegal expression: " + expr);
		}
	}
}
