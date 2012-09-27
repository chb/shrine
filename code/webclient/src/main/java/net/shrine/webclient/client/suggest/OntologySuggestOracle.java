package net.shrine.webclient.client.suggest;

import java.util.List;

import net.shrine.webclient.client.services.OntologyService;
import net.shrine.webclient.client.services.Services;
import net.shrine.webclient.client.widgets.suggest.RichSuggestCallback;
import net.shrine.webclient.client.widgets.suggest.RichSuggestOracle;
import net.shrine.webclient.client.widgets.suggest.RichSuggestRequest;
import net.shrine.webclient.client.widgets.suggest.RichSuggestResponse;
import net.shrine.webclient.shared.domain.TermSuggestion;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.allen_sauer.gwt.log.client.Log;

/**
 * 
 * @author clint
 * @date Mar 30, 2012
 */
public final class OntologySuggestOracle implements RichSuggestOracle<TermSuggestion> {

    private final OntologyService ontologyService = Services.makeOntologyService();

    public OntologySuggestOracle() {
        super();
    }

    @Override
    public void requestSuggestions(final RichSuggestRequest request, final RichSuggestCallback<TermSuggestion> callback) {
        ontologyService.getSuggestions(request.getQuery(), request.getLimit(), new MethodCallback<List<TermSuggestion>>() {
            @Override
            public void onSuccess(final Method method, final List<TermSuggestion> suggestions) {
                callback.onSuggestionsReady(request, RichSuggestResponse.of(suggestions, request.getSequenceNumber()));
            }

            @Override
            public void onFailure(final Method method, final Throwable caught) {
                // Only log errors :/ - We don't want the whole webclient to die
                // if there is a spurious autocomplete failure

                Log.error("Error getting suggestions: " + caught.getMessage(), caught);
            }
        });
    }
}