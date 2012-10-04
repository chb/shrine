package net.shrine.webclient.client.state;

import com.google.gwt.event.shared.EventHandler;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public interface QueryCompletedEventHandler extends EventHandler {
    void handle(final QueryCompletedEvent event);
}
