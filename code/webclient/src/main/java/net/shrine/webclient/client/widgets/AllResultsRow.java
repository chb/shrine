package net.shrine.webclient.client.widgets;

import static net.shrine.webclient.client.util.QuerySummarizer.summarize;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.domain.ReadOnlyQueryGroup;
import net.shrine.webclient.client.events.QueryGroupsChangedEvent;
import net.shrine.webclient.client.events.QueryGroupsChangedEventHandler;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.ReadOnlyObservableList;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
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

	private ReadOnlyObservable<HashMap<String, IntWrapper>> allResults;
	
	@UiField
	SimplePanel querySummaryHolder;
	
	@UiField
	Button runQueryButton;
	
	@UiField
	FlowPanel resultsPanel;
	
	private ReadOnlyObservableList<QueryGroup> queryGroups;
	
	public AllResultsRow() {
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
	}

	void wireUp(final EventBus eventBus, final Controllers controllers, final ReadOnlyObservableList<QueryGroup> queryGroups, final ReadOnlyObservable<HashMap<String, IntWrapper>> allResults) {
		Util.requireNotNull(controllers);
		Util.requireNotNull(queryGroups);
		Util.requireNotNull(allResults);
		Util.requireNotNull(eventBus);
		
		this.allResults = allResults;
		
		this.allResults.observedBy(this);
		
		this.queryGroups = queryGroups;
		
		clearResults();
		
		runQueryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearResults();
				
				resultsPanel.add(new LoadingSpinner());
				
				controllers.query.runAllQuery();
			}
		});
		
		controllers.query.completeAllQueryWithNoResults();
		
		eventBus.addHandler(QueryGroupsChangedEvent.getType(), new QueryGroupsChangedEventHandler() {
			@Override
			public void handle(final QueryGroupsChangedEvent event) {
				if(event.getQueryGroups().size() > 0) {
					updateQuerySummary(event.getQueryGroups());
				}
				
				//TODO, REVISITME: We shouldn't clear the result display if the new query group list
				//isn't actually different than the old one.  This is tricky in the face of a mutable
				//query list made of mutable QueryGroups. :/
				clearResults();
				
				setRunQueryButtonEnabledStatus(event);
			}

			void setRunQueryButtonEnabledStatus(final QueryGroupsChangedEvent event) {
				runQueryButton.setEnabled(event.getQueryGroups().size() > 0);
			}
		});
		
		runQueryButton.setEnabled(false);
	}

	@Override
	public void inform() {
		querySummaryHolder.clear();
		
		if(allResults.isDefined()) {
			clearResults();
			
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

	@Override
	public String toString() {
		return "AllResultsRow []";
	}

	void updateQuerySummary(final List<ReadOnlyQueryGroup> queryGroups) {
		querySummaryHolder.setWidget(new QuerySummary(summarize(queryGroups)));
	}

	void clearResults() {
		resultsPanel.clear();
	}
}
