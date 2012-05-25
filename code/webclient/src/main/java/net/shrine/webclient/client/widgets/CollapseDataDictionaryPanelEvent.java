package net.shrine.webclient.client.widgets;


import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public final class CollapseDataDictionaryPanelEvent extends GwtEvent<CollapseDataDictionaryPanelEventHandler> {

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
	protected void dispatch(final CollapseDataDictionaryPanelEventHandler handler) {
		handler.handle(this);
	}
}
