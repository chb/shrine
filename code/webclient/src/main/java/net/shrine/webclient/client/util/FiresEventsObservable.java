package net.shrine.webclient.client.util;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date May 15, 2012
 * @param <E>
 */
public abstract class FiresEventsObservable<E extends GwtEvent<?>> extends AbstractObservable {
	protected final EventBus eventBus;
	
	private EventCreator<E> eventCreator;

	protected FiresEventsObservable(final EventBus eventBus) {
		super();
		
		Util.requireNotNull(eventBus);
		
		this.eventBus = eventBus;
	}
	
	protected void wireUp(final EventCreator<E> eventCreator) {
		Util.requireNotNull(eventCreator);
		
		this.eventCreator = eventCreator;
	}
	
	@Override
	public void notifyObservers() {
		eventBus.fireEvent(eventCreator.create());
		
		super.notifyObservers();
	}
}
