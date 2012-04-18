package net.shrine.webclient.client.widgets.suggest;

import static java.util.Arrays.asList;

/**
 * 
 * @author clint
 * @date Apr 17, 2012
 */
public class MockRichSuggestOracle implements RichSuggestOracle<MockSuggestion> {
	@Override
	public void requestSuggestions(final RichSuggestRequest request, final RichSuggestCallback<MockSuggestion> callback) {
		final MockSuggestion termSuggestion = new MockSuggestion("foo");
		
		callback.onSuggestionsReady(request, RichSuggestResponse.of(asList(termSuggestion)));
	}
}
