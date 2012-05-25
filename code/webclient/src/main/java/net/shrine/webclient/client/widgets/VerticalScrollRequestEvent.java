package net.shrine.webclient.client.widgets;


import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date May 18, 2012
 */
public final class VerticalScrollRequestEvent extends GwtEvent<VerticalScrollRequestEventHandler> {

	private static final GwtEvent.Type<VerticalScrollRequestEventHandler> TYPE = WidgetUtil.eventType();
	
	private final Widget toScrollTo;
	
	public VerticalScrollRequestEvent(final Widget toScrollTo) {
		super();
		
		this.toScrollTo = toScrollTo;
	}
	
	public Widget getWidgetToScrollTo() {
		return toScrollTo;
	}
	
	public int getPositionToScrollTo() {
		return toScrollTo.getAbsoluteTop();
	}
	
	public static final GwtEvent.Type<VerticalScrollRequestEventHandler> getType() {
		return TYPE;
	}
	
	@Override
	public GwtEvent.Type<VerticalScrollRequestEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(final VerticalScrollRequestEventHandler handler) {
		handler.handle(this);
	}
}
