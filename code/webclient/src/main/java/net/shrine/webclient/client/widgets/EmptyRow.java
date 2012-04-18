package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 30, 2012
 */
public final class EmptyRow extends Composite {

	private static EmptyRowUiBinder uiBinder = GWT.create(EmptyRowUiBinder.class);

	interface EmptyRowUiBinder extends UiBinder<Widget, EmptyRow> { }

	public EmptyRow() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	HTMLPanel delegate;
}
