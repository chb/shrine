package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.state.State;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 27, 2012
 */
public final class WebClientWrapper extends Composite {

	private static final WebClientWrapperUiBinder uiBinder = GWT.create(WebClientWrapperUiBinder.class);

	interface WebClientWrapperUiBinder extends UiBinder<Widget, WebClientWrapper> { }

	@UiField
	WebClientContent content;
	
	@UiField
	Header header;
	
	public WebClientWrapper() {
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public void wireUp(final EventBus eventBus, final State state, final Controllers controllers, final OntologySearchBox ontSearchBox, final PickupDragController dragController) {
		this.content.wireUp(eventBus, state, controllers, ontSearchBox, dragController);
		
		this.header.wireUp(eventBus);
	}
}
