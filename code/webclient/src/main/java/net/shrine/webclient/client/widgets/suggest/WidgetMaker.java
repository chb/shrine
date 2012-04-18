package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 * 
 * Basically a function S => Widget 
 */
public interface WidgetMaker<S extends IsSerializable> {
	Widget makeWidget(final S suggestionInput);
}
