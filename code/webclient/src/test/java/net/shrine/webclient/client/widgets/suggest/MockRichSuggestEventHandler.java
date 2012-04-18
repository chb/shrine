package net.shrine.webclient.client.widgets.suggest;

/**
 * 
 * @author clint
 * @date Apr 17, 2012
 */
public final class MockRichSuggestEventHandler implements RichSuggestionEventHandler<MockSuggestion> {
	public RichSuggestionEvent<MockSuggestion> lastEventReceived = null;
	
	@Override
	public void onSelectionMade(final RichSuggestionEvent<MockSuggestion> event) {
		lastEventReceived = event;
	}
}
