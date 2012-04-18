package net.shrine.webclient.client;

import net.shrine.webclient.client.domain.Term;

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
		final String name = QueryGroupNames.next();

		state.registerNewQuery(name, term);

		return name;
	}
}
