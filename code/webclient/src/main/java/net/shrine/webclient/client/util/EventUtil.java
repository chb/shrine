package net.shrine.webclient.client.util;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date May 29, 2012
 */
public final class EventUtil {
	private EventUtil() {
		super();
	}
	
	public static <H extends EventHandler> GwtEvent.Type<H> eventType() {
		return new GwtEvent.Type<H>();
	}
}
