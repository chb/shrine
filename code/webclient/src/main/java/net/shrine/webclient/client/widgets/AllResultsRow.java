package net.shrine.webclient.client.widgets;

import static net.shrine.webclient.client.state.QuerySummarizer.summarize;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.state.QueryGroupsChangedEvent;
import net.shrine.webclient.client.state.QueryGroupsChangedEventHandler;
import net.shrine.webclient.client.state.ReadOnlyQueryGroup;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 27, 2012
 */
public final class AllResultsRow extends Composite implements Observer {

	private static final AllResultsRowUiBinder uiBinder = GWT.create(AllResultsRowUiBinder.class);

	interface AllResultsRowUiBinder extends UiBinder<Widget, AllResultsRow> { }

	private ReadOnlyObservable<Map<String, Integer>> allResults;
	
	@UiField
	SimplePanel querySummaryHolder;
	
	@UiField
	Button runQueryButton;
	
	@UiField
	FlowPanel resultsPanel;
	
	@UiField
	HTMLPanel resultsWrapper;
	
	public AllResultsRow() {
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
	}

	void wireUp(final EventBus eventBus, final Controllers controllers, final ReadOnlyObservable<Map<String, Integer>> allResults) {
		Util.requireNotNull(controllers);
		Util.requireNotNull(allResults);
		Util.requireNotNull(eventBus);
		
		this.allResults = allResults;
		
		this.allResults.observedBy(this);
		
		clearResults();
		
		initRunQueryButton(controllers);
		
		controllers.query.completeAllQueryWithNoResults();
		
		initQueryGroupsChangeHandler(eventBus);
		
		runQueryButton.setEnabled(false);
	}

	void initRunQueryButton(final Controllers controllers) {
		runQueryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearResults();
				
				showLoadingSpinner();
				
				controllers.query.runAllQuery();
			}
		});
	}

	void initQueryGroupsChangeHandler(final EventBus eventBus) {
		eventBus.addHandler(QueryGroupsChangedEvent.getType(), new AllResultsQueryGroupsChangedEventHandler());
	}

	@Override
	public void inform() {
		for(final Map<String, Integer> resultsMap : allResults) {
			clearResults();
			
			resultsWrapper.setVisible(true);
			
			for(final Entry<String, Integer> entry : resultsMap.entrySet()) {
				final String instName = entry.getKey();
				final int count = entry.getValue();
				
				resultsPanel.add(new InstitutionResult(instName, count));
			}
		}
	}

	@Override
	public void stopObserving() {
		allResults.noLongerObservedBy(this);
	}

	@Override
	public String toString() {
		return "AllResultsRow []";
	}

	void updateQuerySummary(final List<ReadOnlyQueryGroup> queryGroups) {
		querySummaryHolder.setWidget(new QuerySummary(summarize(queryGroups)));
	}
	
	private void clearQuerySummary() {
		querySummaryHolder.clear();
	}

	void clearResults() {
		resultsPanel.clear();
		
		resultsWrapper.setVisible(false);
	}
	
	void showLoadingSpinner() {
		resultsWrapper.setVisible(true);
		
		resultsPanel.add(new LoadingSpinner());
	}
	
	private final class AllResultsQueryGroupsChangedEventHandler implements QueryGroupsChangedEventHandler {
		@Override
		public void handle(final QueryGroupsChangedEvent event) {
			final List<ReadOnlyQueryGroup> queryGroups = event.getQueryGroups();
			
			if(queryGroups.size() > 0) {
				updateQuerySummary(queryGroups);
			} else {
				clearQuerySummary();
			}
			
			//TODO, REVISITME: We shouldn't clear the result display if the new query group list
			//isn't actually different than the old one.  This is tricky in the face of a mutable
			//query list made of mutable QueryGroups. :/
			clearResults();
			
			setRunQueryButtonEnabledStatus(queryGroups);
		}

		void setRunQueryButtonEnabledStatus(final List<ReadOnlyQueryGroup> queryGroups) {
			runQueryButton.setEnabled(queryGroups.size() > 0);
		}
	}
}
