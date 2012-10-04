package net.shrine.webclient.client.state;

import java.util.List;

import net.shrine.webclient.client.util.EventUtil;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public final class QueryGroupsChangedEvent extends GwtEvent<QueryGroupsChangedEventHandler> {
    private static final GwtEvent.Type<QueryGroupsChangedEventHandler> TYPE = EventUtil.eventType();

    private final List<ReadOnlyQueryGroup> queryGroups;

    public QueryGroupsChangedEvent(final List<? extends ReadOnlyQueryGroup> queryGroups) {
        super();

        this.queryGroups = Util.makeArrayList(queryGroups);
    }

    public List<ReadOnlyQueryGroup> getQueryGroups() {
        return queryGroups;
    }

    public static GwtEvent.Type<QueryGroupsChangedEventHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<QueryGroupsChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final QueryGroupsChangedEventHandler handler) {
        handler.handle(this);
    }
}
