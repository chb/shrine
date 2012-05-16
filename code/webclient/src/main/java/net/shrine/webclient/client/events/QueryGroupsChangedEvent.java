package net.shrine.webclient.client.events;

import java.util.List;

import net.shrine.webclient.client.domain.ReadOnlyQueryGroup;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.client.widgets.WidgetUtil;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public final class QueryGroupsChangedEvent extends GwtEvent<QueryGroupsChangedEventHandler> {
	private static final GwtEvent.Type<QueryGroupsChangedEventHandler> TYPE = WidgetUtil.eventType();
	
	private final List<ReadOnlyQueryGroup> queryGroups;
	
	public QueryGroupsChangedEvent(final List<? extends ReadOnlyQueryGroup> queryGroups) {
		super();
		
		this.queryGroups = Util.makeArrayList(queryGroups);
	}
	
	public List<ReadOnlyQueryGroup> getQueryGroups() {
		return queryGroups;
	}

	public static final GwtEvent.Type<QueryGroupsChangedEventHandler> getType() {
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

