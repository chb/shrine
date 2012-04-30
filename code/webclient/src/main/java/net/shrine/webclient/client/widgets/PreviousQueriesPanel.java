package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.domain.PreviousQuery;
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
 * @date Apr 27, 2012
 */
public final class PreviousQueriesPanel extends Composite {

	private static PreviousQueriesPanelUiBinder uiBinder = GWT.create(PreviousQueriesPanelUiBinder.class);

	interface PreviousQueriesPanelUiBinder extends UiBinder<Widget, PreviousQueriesPanel> { }

	@UiField
	HTMLPanel rows;

	public PreviousQueriesPanel(final Iterable<PreviousQuery> queries) {
		initWidget(uiBinder.createAndBindUi(this));
		
		Util.requireNotNull(queries);
		
		rows.clear();
		
		for(final PreviousQuery query : queries) {
			rows.add(new PreviousQueryRow(query));
		}
	}

	public void wireUp() {
		// TODO
	}
}
