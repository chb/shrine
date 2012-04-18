package net.shrine.webclient.client.widgets;

import java.util.HashMap;
import java.util.Map.Entry;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 27, 2012
 */
public final class AllResultColumn extends Composite implements Observer {

	private static final AllResultUiBinder uiBinder = GWT.create(AllResultUiBinder.class);

	interface AllResultUiBinder extends UiBinder<Widget, AllResultColumn> { }

	private ReadOnlyObservable<HashMap<String, IntWrapper>> allResults;

	private Controllers controllers;
	
	@UiField
	Button runQueryButton;
	
	@UiField
	FlowPanel resultsPanel;
	
	public AllResultColumn() {
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
		
		runQueryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				controllers.query.runEveryQuery();
			}
		});
	}

	void wireUp(final Controllers controllers, final ReadOnlyObservable<HashMap<String, IntWrapper>> allResults) {
		Util.requireNotNull(controllers);
		Util.requireNotNull(allResults);
		
		this.controllers = controllers;
		this.allResults = allResults;
		
		this.allResults.observedBy(this);
		
		resultsPanel.clear();
	}

	@Override
	public void inform() {
		resultsPanel.clear();
		
		if(allResults.isDefined()) {
			for(final Entry<String, IntWrapper> entry : allResults.get().entrySet()) {
				final String instName = entry.getKey();
				final int count = entry.getValue().getValue();
				
				resultsPanel.add(new ResultRow(instName, Observable.from(count)));
			}
		} else {
			resultsPanel.add(new LoadingSpinner());
		}
	}

	@Override
	public void stopObserving() {
		allResults.noLongerObservedBy(this);
	}
}
