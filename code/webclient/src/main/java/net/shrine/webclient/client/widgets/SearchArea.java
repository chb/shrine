package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 */
public final class SearchArea extends Composite {

	private static final SearchAreaUiBinder uiBinder = GWT.create(SearchAreaUiBinder.class);

	interface SearchAreaUiBinder extends UiBinder<Widget, SearchArea> { }

	@UiField
	SimplePanel ontSearchBoxHolder;

	@UiField
	Button browseButton;
	
	public SearchArea() {
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	void wireUp(final OntologySearchBox ontSearchBox) {
		Util.requireNotNull(ontSearchBox);
		
		//TODO: feels very hackish
		ontSearchBox.getElement().setId("ontSearchInput");
		
		this.ontSearchBoxHolder.setWidget(ontSearchBox);
	}
}
