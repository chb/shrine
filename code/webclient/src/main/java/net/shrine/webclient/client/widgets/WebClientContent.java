package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.state.QueryGroup;
import net.shrine.webclient.client.state.QueryGroupsChangedEvent;
import net.shrine.webclient.client.state.QueryGroupsChangedEventHandler;
import net.shrine.webclient.client.state.State;
import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservableList;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Map;

/**
 * @author clint
 * @date Mar 27, 2012
 */
public final class WebClientContent extends Composite {

    private static final WebClientContentUiBinder uiBinder = GWT.create(WebClientContentUiBinder.class);

    interface WebClientContentUiBinder extends UiBinder<Widget, WebClientContent> {
    }

    @UiField
    SimplePanel searchBoxHolder;

    @UiField
    DataDictionaryRow dataDictionaryRow;

    @UiField
    QueryPanel queryPanel;

    @UiField
    SummaryPanel summaryPanel;

    @UiField
    ResultsPanel resultsPanel;

    public WebClientContent() {
        initWidget(uiBinder.createAndBindUi(this));

        setQueryGroupsAndResultsPanelVisibility(false);
    }

    public void wireUp(final EventBus eventBus, final State state, final Controllers controllers, final OntologySearchBox ontSearchBox, final PickupDragController dragController) {
        Util.requireNotNull(eventBus);
        Util.requireNotNull(ontSearchBox);

        searchBoxHolder.setWidget(ontSearchBox);

        dataDictionaryRow.wireUp(eventBus, controllers);

        queryPanel.wireUp(controllers, state.getQueries(), dragController);

        summaryPanel.wireUp(eventBus, controllers);

        resultsPanel.wireUp(state.getAllResult());

        eventBus.addHandler(QueryGroupsChangedEvent.getType(), new QueryGroupsChangedEventHandler() {
            @Override
            public void handle(final QueryGroupsChangedEvent event) {
                final boolean showQueryGroupsAndResultsPanels = !event.getQueryGroups().isEmpty();

                setQueryGroupsAndResultsPanelVisibility(showQueryGroupsAndResultsPanels);
                setResulsPanelVisibility(false);
            }
        });
    }

    void setResulsPanelVisibility(final boolean showResultsPanel) {
        resultsPanel.setVisible(showResultsPanel);
    }

    void setQueryGroupsAndResultsPanelVisibility(final boolean showQueryGroupsAndResultsPanels) {
        queryPanel.setVisible(showQueryGroupsAndResultsPanels);

        summaryPanel.setVisible(showQueryGroupsAndResultsPanels);
    }
}
