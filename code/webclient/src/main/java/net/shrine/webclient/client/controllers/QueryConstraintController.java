package net.shrine.webclient.client.controllers;

import java.util.Date;

import net.shrine.webclient.client.state.QueryGroup;
import net.shrine.webclient.client.state.State;

import com.allen_sauer.gwt.log.client.Log;

/**
 * 
 * @author clint
 * @date Mar 27, 2012
 */
public final class QueryConstraintController extends StatefulController {
	public QueryConstraintController(final State state) {
		super(state);
	}

	public void setNegated(final int queryId, final boolean negated) {
		state.guardQueryIsPresent(queryId);

		final QueryGroup queryGroup = state.getQuery(queryId);
		
		queryGroup.setNegated(negated);

		Log.info("Query '" + queryGroup.getId() + "' " + (negated ? "is " : "is not ") + "negated");
	}

	public void setStartDate(final int queryId, final Date start) {
		state.guardQueryIsPresent(queryId);

		final QueryGroup queryGroup = state.getQuery(queryId);
		
		queryGroup.setStart(start);

		Log.info("Query '" + queryGroup.getId() + "': start date: " + start);
	}

	public void setEndDate(final int queryId, final Date end) {
		state.guardQueryIsPresent(queryId);

		final QueryGroup queryGroup = state.getQuery(queryId);
		
		queryGroup.setEnd(end);

		Log.info("Query '" + queryGroup.getId() + "': end date: " + end);
	}

	public void setMinOccurs(final int queryId, final int minOccurs) {
		state.guardQueryIsPresent(queryId);

		final QueryGroup queryGroup = state.getQuery(queryId);
		
		queryGroup.setMinOccurances(minOccurs);

		Log.info("Query '" + queryGroup.getId() + "': min occurs: " + minOccurs);
	}
}
