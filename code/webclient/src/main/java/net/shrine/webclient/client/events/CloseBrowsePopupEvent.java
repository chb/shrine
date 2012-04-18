package net.shrine.webclient.client.events;

import net.shrine.webclient.client.widgets.WidgetUtil;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public class CloseBrowsePopupEvent extends GwtEvent<CloseBrowsePopupEventHandler> {

	private static final GwtEvent.Type<CloseBrowsePopupEventHandler> TYPE = WidgetUtil.eventType();
	
	public static final CloseBrowsePopupEvent Instance = new CloseBrowsePopupEvent();
	
	private CloseBrowsePopupEvent() {
		super();
	}
	
	public static final GwtEvent.Type<CloseBrowsePopupEventHandler> getType() {
		return TYPE;
	}
	
	@Override
	public GwtEvent.Type<CloseBrowsePopupEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(CloseBrowsePopupEventHandler handler) {
		handler.handle(this);
	}
}
