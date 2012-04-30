package net.shrine.webclient.client.events;

import net.shrine.webclient.client.widgets.WidgetUtil;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public class CollapseDataDictionaryPanelEvent extends GwtEvent<CollapseDataDictionaryPanelEventHandler> {

	private static final GwtEvent.Type<CollapseDataDictionaryPanelEventHandler> TYPE = WidgetUtil.eventType();
	
	public static final CollapseDataDictionaryPanelEvent Instance = new CollapseDataDictionaryPanelEvent();
	
	private CollapseDataDictionaryPanelEvent() {
		super();
	}
	
	public static final GwtEvent.Type<CollapseDataDictionaryPanelEventHandler> getType() {
		return TYPE;
	}
	
	@Override
	public GwtEvent.Type<CollapseDataDictionaryPanelEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(CollapseDataDictionaryPanelEventHandler handler) {
		handler.handle(this);
	}
}
