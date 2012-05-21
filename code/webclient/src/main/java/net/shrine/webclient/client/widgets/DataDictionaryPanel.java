package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 25, 2012
 * 
 */
public final class DataDictionaryPanel extends Composite  {

	private static DataDictionaryDataHolderUiBinder uiBinder = GWT.create(DataDictionaryDataHolderUiBinder.class);

	interface DataDictionaryDataHolderUiBinder extends UiBinder<Widget, DataDictionaryPanel> { }

	@UiField
	HTMLPanel wrapper;
	
	@UiField
	HTMLPanel browser;
	
	public DataDictionaryPanel(final OntologyTree ontTree) {
		initWidget(uiBinder.createAndBindUi(this));
		
		Util.requireNotNull(ontTree);
		
		//TODO: hacky
		setIds();
		
		browser.clear();
		
		browser.add(ontTree);
	}

	void setIds() {
		wrapper.getElement().setId("dataDictionaryData");
		
		browser.getElement().setId("browser");
	}
}
