package net.shrine.webclient.client.state;

import net.shrine.webclient.client.util.EventUtil;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public final class QueryCompletedEvent extends GwtEvent<QueryCompletedEventHandler> {
    private static final GwtEvent.Type<QueryCompletedEventHandler> TYPE = EventUtil.eventType();

    public static final QueryCompletedEvent Instance = new QueryCompletedEvent();
    
    private QueryCompletedEvent() {
        super();
    }

    public static GwtEvent.Type<QueryCompletedEventHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<QueryCompletedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final QueryCompletedEventHandler handler) {
        handler.handle(this);
    }
}

