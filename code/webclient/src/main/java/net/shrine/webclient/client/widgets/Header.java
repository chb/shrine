package net.shrine.webclient.client.widgets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.shrine.webclient.client.domain.PreviousQuery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 26, 2012
 */
public final class Header extends Composite {

	private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

	interface HeaderUiBinder extends UiBinder<Widget, Header> { }

	@UiField
	Button queryHistoryButton;
	
	private final PopupPanel queryHistoryPopup = new PopupPanel(true, false);
	
	public Header() {
		initWidget(uiBinder.createAndBindUi(this));
		
		queryHistoryPopup.hide();
		
		queryHistoryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				if(queryHistoryPopup.isShowing()) {
					queryHistoryPopup.hide();
				} else {
					final List<PreviousQuery> queries = Arrays.asList(new PreviousQuery("7316cb3b-b7ee-45f5-90db-97054ad807cc", new Date()), new PreviousQuery("6349b2cc-24e7-4184-a914-b1646aefac08", new Date()), new PreviousQuery("8f4de438-85c7-436c-a3e8-6a4ea6a97284", new Date()));
					
					queryHistoryPopup.setWidget(new PreviousQueriesPanel(queries));
					
					queryHistoryPopup.show();
				}
			}
		});
	}
}
