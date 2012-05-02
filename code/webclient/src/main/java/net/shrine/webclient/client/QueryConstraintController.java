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

	public void setNegated(final QueryGroupId queryId, final boolean negated) {
		state.guardQueryIsPresent(queryId);

		state.getQuery(queryId).setNegated(negated);

		Log.info("Query '" + queryId.name + "' " + (negated ? "is " : "is not ") + "negated");
	}

	public void setStartDate(final QueryGroupId queryId, final Date start) {
		state.guardQueryIsPresent(queryId);

		state.getQuery(queryId).setStart(start);

		Log.info("Query '" + queryId.name + "': start date: " + start);
	}

	public void setEndDate(final QueryGroupId queryId, final Date end) {
		state.guardQueryIsPresent(queryId);

		state.getQuery(queryId).setEnd(end);

		Log.info("Query '" + queryId.name + "': end date: " + end);
	}

	public void setMinOccurs(final QueryGroupId queryId, final int minOccurs) {
		state.guardQueryIsPresent(queryId);

		state.getQuery(queryId).setMinOccurances(minOccurs);

		Log.info("Query '" + queryId.name + "': min occurs: " + minOccurs);
	}
}
