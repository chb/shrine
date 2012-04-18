package net.shrine.webclient.client;

import java.util.Date;

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

	public void setNegated(final String queryName, final boolean negated) {
		state.guardQueryNameIsPresent(queryName);

		state.getQueries().get(queryName).setNegated(negated);

		Log.info("Query '" + queryName + "' " + (negated ? "is " : "is not ") + "negated");
	}

	public void setStartDate(final String queryName, final Date start) {
		state.guardQueryNameIsPresent(queryName);

		state.getQueries().get(queryName).setStart(start);

		Log.info("Query '" + queryName + "': start date: " + start);
	}

	public void setEndDate(final String queryName, final Date end) {
		state.guardQueryNameIsPresent(queryName);

		state.getQueries().get(queryName).setEnd(end);

		Log.info("Query '" + queryName + "': end date: " + end);
	}

	public void setMinOccurs(final String queryName, final int minOccurs) {
		state.guardQueryNameIsPresent(queryName);

		state.getQueries().get(queryName).setMinOccurances(minOccurs);

		Log.info("Query '" + queryName + "': min occurs: " + minOccurs);
	}
}
