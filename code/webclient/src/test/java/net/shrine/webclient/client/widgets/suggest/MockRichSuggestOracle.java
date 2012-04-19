package net.shrine.webclient.client.widgets.suggest;

import java.util.Collections;
import java.util.List;

import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Apr 17, 2012
 */
public class MockRichSuggestOracle implements RichSuggestOracle<MockSuggestion> {
	public static final String DefaultSuggestionText = "bar";

	public RichSuggestRequest lastRequest = null;
	
	public boolean returnNoSuggestions = false;
	
	public int numToReturn = 1;
	
	@Override
	public void requestSuggestions(final RichSuggestRequest request, final RichSuggestCallback<MockSuggestion> callback) {
		lastRequest = request;
		
		final MockSuggestion termSuggestion = new MockSuggestion(DefaultSuggestionText);
		
		final List<MockSuggestion> toReturn;
		
		if(returnNoSuggestions) {
			toReturn = Collections.emptyList();
		} else {
			toReturn = Util.makeArrayList();
			
			for(int i = 0; i < numToReturn; ++i) {
				toReturn.add(termSuggestion);
			}
		}
		
		callback.onSuggestionsReady(request, RichSuggestResponse.of(toReturn));
	}
}
