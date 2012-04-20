package net.shrine.webclient.client;

import java.util.HashMap;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.ExpressionXml;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.ObservableMap;
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
	
	// Query group name => QueryGroup (Expression, integer result (patient set size), negated (t/f), start date, end date, min occurrances )
	private final ObservableMap<String, QueryGroup> queries = ObservableMap.empty();
	
	private final QueryGroupNames queryGroupNames = new QueryGroupNames();

	public void guardQueryNameIsPresent(final String queryName) {
		Util.require(queries.containsKey(queryName));
	}
	
	public int numQueryGroups() {
		return queries.size();
	}
	
	public void completeAllQuery(final HashMap<String, IntWrapper> resultsByInstitution) {
		if(Log.isInfoEnabled()) {
			Log.info("Completing query 'All' with: '" + resultsByInstitution + "'");
			
			for(final String instName : resultsByInstitution.keySet()) {
				Log.info(instName + ": " + resultsByInstitution.get(instName));
			}
		}
		
		allResult.set(resultsByInstitution);
	}
	
	public void completeQuery(final String queryName, final HashMap<String, IntWrapper> result) {
		Util.require(queries.containsKey(queryName), "Couldn't complete uninitialized query '" + queryName + "'; result would have been '" + result + "'");
			
		Log.info("Completing query '" + queryName + "' with '" + result + "'");
		
		queries.get(queryName).getResult().set(result);
	}
	
	private void addNewQuery(final String name, final Expression expr) {
		queries.put(name, new QueryGroup(expr, Observable.<HashMap<String, IntWrapper>>empty()));
	}
	
	public void registerNewQuery(final String name, final Expression expr) {
		Log.info("Adding query group '" + name + "': " + expr);
		
		addNewQuery(name, expr);

		updateAllExpression();
	}

	void updateAllExpression() {
		allExpressionXml = ExpressionXml.fromQueryGroups(queries.values());
	}

	public ObservableMap<String, QueryGroup> getQueries() {
		return queries;
	}

	public String getAllExpression() {
		return allExpressionXml;
	}

	public Observable<HashMap<String, IntWrapper>> getAllResult() {
		return allResult;
	}
	
	QueryGroupNames getQueryGroupNames() {
		return queryGroupNames;
	}
}
