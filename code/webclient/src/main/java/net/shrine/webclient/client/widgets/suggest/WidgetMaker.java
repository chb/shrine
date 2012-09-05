package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 * 
 * Basically a function S => Widget 
 */
public interface WidgetMaker<S> {
	Widget makeWidget(final S suggestionInput);
}
