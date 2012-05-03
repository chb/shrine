package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date May 3, 2012
 */
public final class QuerySummary extends Composite {

	private static QuerySummaryUiBinder uiBinder = GWT.create(QuerySummaryUiBinder.class);

	interface QuerySummaryUiBinder extends UiBinder<Widget, QuerySummary> { }

	@UiField
	HTML querySentencePanel;
	
	public QuerySummary(final String querySummaryHTML) {
		initWidget(uiBinder.createAndBindUi(this));
		
		querySentencePanel.setHTML(querySummaryHTML);
	}
}
