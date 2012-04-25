package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 24, 2012
 */
public final class CloseButton extends Composite {

	private static CloseButtonUiBinder uiBinder = GWT.create(CloseButtonUiBinder.class);

	interface CloseButtonUiBinder extends UiBinder<Widget, CloseButton> { }

	@UiField
	Image delegate;
	
	public CloseButton() {
		initWidget(uiBinder.createAndBindUi(this));
		
		delegate.setHeight("16px");
		delegate.setWidth("16px");
	}

	public HandlerRegistration addClickHandler(final ClickHandler handler) {
		return delegate.addClickHandler(handler);
	}

	@Override
	public void fireEvent(final GwtEvent<?> event) {
		delegate.fireEvent(event);
	}
}
