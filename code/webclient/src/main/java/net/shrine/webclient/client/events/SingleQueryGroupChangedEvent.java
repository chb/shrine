package net.shrine.webclient.client.events;

import net.shrine.webclient.client.state.ReadOnlyQueryGroup;
import net.shrine.webclient.client.widgets.WidgetUtil;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public final class SingleQueryGroupChangedEvent extends GwtEvent<SingleQueryGroupChangedEventHandler> {
	private static final GwtEvent.Type<SingleQueryGroupChangedEventHandler> TYPE = WidgetUtil.eventType();
	
	private final ReadOnlyQueryGroup changed;
	
	public SingleQueryGroupChangedEvent(final ReadOnlyQueryGroup changed) {
		super();
		
		this.changed = changed;
	}
	
	public ReadOnlyQueryGroup getChanged() {
		return changed;
	}

	public static final GwtEvent.Type<SingleQueryGroupChangedEventHandler> getType() {
		return TYPE;
	}
	
	@Override
	public GwtEvent.Type<SingleQueryGroupChangedEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(final SingleQueryGroupChangedEventHandler handler) {
		handler.handle(this);
	}
}

