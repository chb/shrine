package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.event.shared.EventHandler;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 */
public interface RichSuggestionEventHandler<S> extends EventHandler {
    void onSelectionMade(final RichSuggestionEvent<S> event);
}
