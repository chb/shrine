package net.shrine.webclient.client.suggest;

import java.util.List;

import net.shrine.webclient.client.domain.TermSuggestion;
import net.shrine.webclient.client.services.OntologySearchService;
import net.shrine.webclient.client.services.OntologySearchServiceAsync;
import net.shrine.webclient.client.widgets.suggest.RichSuggestCallback;
import net.shrine.webclient.client.widgets.suggest.RichSuggestOracle;
import net.shrine.webclient.client.widgets.suggest.RichSuggestRequest;
import net.shrine.webclient.client.widgets.suggest.RichSuggestResponse;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author clint
 * @date Mar 30, 2012
 */
public final class OntologySuggestOracle implements RichSuggestOracle<TermSuggestion> {

	private final OntologySearchServiceAsync ontologyService = GWT.create(OntologySearchService.class);

	public OntologySuggestOracle() {
		super();
	}

	@Override
	public void requestSuggestions(final RichSuggestRequest request, final RichSuggestCallback<TermSuggestion> callback) {
		ontologyService.getSuggestions(request.getQuery(), request.getLimit(), new AsyncCallback<List<TermSuggestion>>() {
			@Override
			public void onSuccess(final List<TermSuggestion> suggestions) {
				callback.onSuggestionsReady(request, RichSuggestResponse.of(suggestions));
			}

			@Override
			public void onFailure(final Throwable caught) {
				// Only log errors :/ - We don't want the whole webclient to die
				// if there is a spurious autocomplete failure

				Log.error("Error getting suggestions: " + caught.getMessage(), caught);
			}
		});
	}
}