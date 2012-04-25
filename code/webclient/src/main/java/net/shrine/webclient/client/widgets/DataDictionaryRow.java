package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 25, 2012
 */
public final class DataDictionaryRow extends Composite {

	private static DataDictionaryRowUiBinder uiBinder = GWT.create(DataDictionaryRowUiBinder.class);

	interface DataDictionaryRowUiBinder extends UiBinder<Widget, DataDictionaryRow> { }

	@UiField
	SimplePanel expandCollapseButtonHolder;
	
	@UiField
	SimplePanel dataDictionaryDataHolder;
	
	private final Image expandButton = new ExpandButton();
	
	private final Image collapseButton = new CollapseButton();
	
	public DataDictionaryRow() {
		initWidget(uiBinder.createAndBindUi(this));
		
		final ClickHandler toggleHandler = new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				toggleDataDictionaryDisplay();
			}
		};
		
		expandButton.addClickHandler(toggleHandler);
		collapseButton.addClickHandler(toggleHandler);
		
		hideDataDictionaryTree();
	}
	
	void toggleDataDictionaryDisplay() {
		if(dataDictionaryDataHolder.getWidget() == null) {
			//TODO: allow jumping to specific section in ontology tree
			//TODO: Don't use dummy data
			showDataDictionaryTree();
		} else {
			hideDataDictionaryTree();
		}
	}

	void showDataDictionaryTree() {
		dataDictionaryDataHolder.setWidget(new DataDictionaryPanel());
		expandCollapseButtonHolder.setWidget(collapseButton);
	}

	void hideDataDictionaryTree() {
		dataDictionaryDataHolder.clear();
		expandCollapseButtonHolder.setWidget(expandButton);
	}
}
