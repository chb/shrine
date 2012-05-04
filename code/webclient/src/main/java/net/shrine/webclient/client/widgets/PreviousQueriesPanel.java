package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 27, 2012
 */
public final class PreviousQueriesPanel extends Composite {

	private static PreviousQueriesPanelUiBinder uiBinder = GWT.create(PreviousQueriesPanelUiBinder.class);

	interface PreviousQueriesPanelUiBinder extends UiBinder<Widget, PreviousQueriesPanel> { }

	public PreviousQueriesPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}
