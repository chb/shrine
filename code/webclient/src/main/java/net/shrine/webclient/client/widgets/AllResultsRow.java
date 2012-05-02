package net.shrine.webclient.client.widgets;

import java.util.HashMap;
import java.util.Map.Entry;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.ReadOnlyObservableList;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 27, 2012
 */
public final class AllResultsRow extends Composite implements Observer {

	private static final AllResultsRowUiBinder uiBinder = GWT.create(AllResultsRowUiBinder.class);

	interface AllResultsRowUiBinder extends UiBinder<Widget, AllResultsRow> { }

	private ReadOnlyObservable<HashMap<String, IntWrapper>> allResults;
	
	@UiField
	HTMLPanel querySentencePanel;
	
	@UiField
	Button runQueryButton;
	
	@UiField
	FlowPanel resultsPanel;
	
	private ReadOnlyObservableList<QueryGroup> queryGroups;
	
	public AllResultsRow() {
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
	}

	void wireUp(final Controllers controllers, final ReadOnlyObservableList<QueryGroup> queryGroups, final ReadOnlyObservable<HashMap<String, IntWrapper>> allResults) {
		Util.requireNotNull(controllers);
		Util.requireNotNull(queryGroups);
		Util.requireNotNull(allResults);
		
		this.allResults = allResults;
		
		this.allResults.observedBy(this);
		
		this.queryGroups = queryGroups;
		
		this.queryGroups.observedBy(this);
		
		resultsPanel.clear();
		
		runQueryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resultsPanel.clear();
				
				resultsPanel.add(new LoadingSpinner());
				
				controllers.query.runAllQuery();
			}
		});
		
		controllers.query.completeAllQueryWithNoResults();
	}

	@Override
	public void inform() {
		runQueryButton.setEnabled(queryGroups.size() > 0);
		
		if(allResults.isDefined()) {
			resultsPanel.clear();
			
			for(final Entry<String, IntWrapper> entry : allResults.get().entrySet()) {
				final String instName = entry.getKey();
				final int count = entry.getValue().getValue();
				
				resultsPanel.add(new InstitutionResult(instName, count));
			}
		}
	}

	@Override
	public void stopObserving() {
		allResults.noLongerObservedBy(this);
		queryGroups.noLongerObservedBy(this);
	}
}
