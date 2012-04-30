package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.domain.PreviousQuery;
import net.shrine.webclient.client.util.Formats;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 27, 2012
 */
public final class PreviousQueryRow extends Composite {

	private static PreviousQueryRowUiBinder uiBinder = GWT.create(PreviousQueryRowUiBinder.class);

	interface PreviousQueryRowUiBinder extends UiBinder<Widget, PreviousQueryRow> { }

	@UiField
	SpanElement queryId;
	
	@UiField
	SpanElement date;
	public PreviousQueryRow(final PreviousQuery query) {
		initWidget(uiBinder.createAndBindUi(this));
		
		queryId.setInnerText(query.getQueryId());
		date.setInnerText(Formats.Date.yearMonthDay.format(query.getDate()));
	}
}
