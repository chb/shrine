package net.shrine.webclient.client.state;

import net.shrine.webclient.client.util.EventUtil;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public final class QueryStartedEvent extends GwtEvent<QueryStartedEventHandler> {
    private static final GwtEvent.Type<QueryStartedEventHandler> TYPE = EventUtil.eventType();

    public static final QueryStartedEvent Instance = new QueryStartedEvent();
    
    private QueryStartedEvent() {
        super();
    }

    public static GwtEvent.Type<QueryStartedEventHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<QueryStartedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final QueryStartedEventHandler handler) {
        handler.handle(this);
    }
}

