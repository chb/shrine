package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 17, 2012
 */
public final class MockWidgetMaker implements WidgetMaker<MockSuggestion> {
	@Override
	public Widget makeWidget(final MockSuggestion suggestionInput) {
		return new Label(suggestionInput.suggestion);
	}
}
