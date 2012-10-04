package net.shrine.webclient.client.controllers;

import java.util.HashMap;
import java.util.Map;

import net.shrine.webclient.client.services.QueryService;
import net.shrine.webclient.client.state.QueryCompletedEvent;
import net.shrine.webclient.client.state.QueryStartedEvent;
import net.shrine.webclient.client.state.State;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 * 
 */
public final class QueryController extends StatefulController {

    private final QueryService queryService;
    
    private final EventBus eventBus;

    public QueryController(final State state, final QueryService queryService, final EventBus eventBus) {
        super(state);

        Util.requireNotNull(queryService);

        this.queryService = queryService;
        this.eventBus = eventBus;
    }

    public void runQuery() {
        state.getQueryResult().clear();
        
        state.updateQueryExpression();

        state.getQueryResult().clear();

        Log.debug("Query XML: " + state.getQueryExpression());

        eventBus.fireEvent(QueryStartedEvent.Instance);
        
        queryService.performQuery(state.getQueryExpression(), new MethodCallback<MultiInstitutionQueryResult>() {
            @Override
            public void onSuccess(final Method method, final MultiInstitutionQueryResult result) {
                Log.debug("Got query result: " + result);
                
                completeQuery(result.asMap());
            }

            @Override
            public void onFailure(final Method method, final Throwable caught) {
                Log.error("Error making query 'All': " + caught.getMessage(), caught);

                completeQueryWithNoResults();
            }
        });
    }

    public void completeQueryWithNoResults() {
        completeQuery(noResults());
    }
    
    private void completeQuery(final Map<String, SingleInstitutionQueryResult> results) {
        state.completeQuery(results);
        
        eventBus.fireEvent(QueryCompletedEvent.Instance);
    }

    private static Map<String, SingleInstitutionQueryResult> noResults() {
        return new HashMap<String, SingleInstitutionQueryResult>();
    }
}
