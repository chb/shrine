package net.shrine.webclient.client.state;

import net.shrine.webclient.client.util.EventUtil;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public final class SingleQueryGroupChangedEvent extends GwtEvent<SingleQueryGroupChangedEventHandler> {
    private static final GwtEvent.Type<SingleQueryGroupChangedEventHandler> TYPE = EventUtil.eventType();

    private final ReadOnlyQueryGroup changed;

    public SingleQueryGroupChangedEvent(final ReadOnlyQueryGroup changed) {
        super();

        this.changed = changed;
    }

    public ReadOnlyQueryGroup getChanged() {
        return changed;
    }

    public static GwtEvent.Type<SingleQueryGroupChangedEventHandler> getType() {
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
