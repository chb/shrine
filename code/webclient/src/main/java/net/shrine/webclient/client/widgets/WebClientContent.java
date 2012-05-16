package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.State;

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
public final class WebClientContent extends Composite {

	private static final WebclientUiBinder uiBinder = GWT.create(WebclientUiBinder.class);

	interface WebclientUiBinder extends UiBinder<Widget, WebClientContent> { }

	@UiField
	SearchArea searchArea;
	
	@UiField
	DataDictionaryRow dataDictionaryRow;
	
	@UiField
	QueryColumn queryColumn;
	
	@UiField
	AllResultsRow allResultColumn;
	
	public WebClientContent() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void wireUp(final EventBus eventBus, final State state, final Controllers controllers, final OntologySearchBox ontSearchBox, final PickupDragController dragController) {
		searchArea.wireUp(ontSearchBox);
		
		dataDictionaryRow.wireUp(eventBus, controllers);
		
		queryColumn.wireUp(controllers, state.getQueries(), dragController);
		
		allResultColumn.wireUp(eventBus, controllers, state.getAllResult());
	}
}
