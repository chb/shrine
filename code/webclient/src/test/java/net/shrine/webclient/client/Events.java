package net.shrine.webclient.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;

/**
 * 
 * @author clint
 * @date Apr 13, 2012
 * 
 * Helper to create common event objects for testing.
 * 
 * NB: Each method returns an anonymous subclass, since each
 * event type has a protected constructor.
 */
public final class Events {
	private Events() {
		super();
	}

	public static ClickEvent click() {
		return new ClickEvent() { };
	}

	public static MouseOutEvent mouseOut() {
		return new MouseOutEvent() { };
	}

	public static MouseOverEvent mouseOver() {
		return new MouseOverEvent() { };
	}
}