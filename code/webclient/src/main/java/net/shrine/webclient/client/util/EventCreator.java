package net.shrine.webclient.client.util;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public interface EventCreator<E extends GwtEvent<?>> {
	E create();
}
