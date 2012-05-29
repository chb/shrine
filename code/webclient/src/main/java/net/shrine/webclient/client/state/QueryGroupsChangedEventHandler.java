package net.shrine.webclient.client.state;

import com.google.gwt.event.shared.EventHandler;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public interface QueryGroupsChangedEventHandler extends EventHandler {
	void handle(final QueryGroupsChangedEvent event);
}
