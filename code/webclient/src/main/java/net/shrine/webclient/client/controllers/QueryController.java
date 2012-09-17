package net.shrine.webclient.client.controllers;

import java.util.HashMap;
import java.util.Map;

import net.shrine.webclient.client.services.QueryService;
import net.shrine.webclient.client.state.State;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.allen_sauer.gwt.log.client.Log;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 * 
 */
public final class QueryController extends StatefulController {

    private final QueryService queryService;

    public QueryController(final State state, final QueryService queryService) {
        super(state);

        Util.requireNotNull(queryService);

        this.queryService = queryService;
    }

    public void runEveryQuery() {

        state.getAllResult().clear();

        runAllQuery();
    }
    
    public void runAllQuery() {
        state.updateAllExpression();

        state.getAllResult().clear();

        Log.debug("Query XML: " + state.getAllExpression());

        queryService.performQuery(state.getAllExpression(), new MethodCallback<MultiInstitutionQueryResult>() {
            @Override
            public void onSuccess(final Method method, final MultiInstitutionQueryResult result) {
                Log.debug("Got query result: " + result);
                
                state.completeAllQuery(result.asMap());
            }

            @Override
            public void onFailure(final Method method, final Throwable caught) {
                Log.error("Error making query 'All': " + caught.getMessage(), caught);

                completeAllQueryWithNoResults();
            }
        });
    }

    public void completeAllQueryWithNoResults() {
        state.completeAllQuery(noResults());
    }

    private static Map<String, SingleInstitutionQueryResult> noResults() {
        return new HashMap<String, SingleInstitutionQueryResult>();
    }
}
