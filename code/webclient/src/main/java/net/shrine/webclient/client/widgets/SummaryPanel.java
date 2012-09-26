package net.shrine.webclient.client.widgets;

import static net.shrine.webclient.client.state.QuerySummarizer.summarize;

import java.util.List;

import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.state.QueryGroupsChangedEvent;
import net.shrine.webclient.client.state.QueryGroupsChangedEventHandler;
import net.shrine.webclient.client.state.ReadOnlyQueryGroup;
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
public final class SummaryPanel extends Composite {

    private static final ResultsPanelUiBinder uiBinder = GWT.create(ResultsPanelUiBinder.class);

    interface ResultsPanelUiBinder extends UiBinder<Widget, SummaryPanel> { }

    @UiField
    SimplePanel querySummaryHolder;

    @UiField
    Button runQueryButton;

    @Deprecated
    @UiField
    FlowPanel resultsPanel;

    @Deprecated
    @UiField
    HTMLPanel resultsWrapper;

    public SummaryPanel() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
    }

    void wireUp(final EventBus eventBus, final Controllers controllers) {
        Util.requireNotNull(controllers);
        Util.requireNotNull(eventBus);

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

            if (queryGroups.size() > 0) {
                updateQuerySummary(queryGroups);
            } else {
                clearQuerySummary();
            }

            // TODO, REVISITME: We shouldn't clear the result display if the new
            // query group list
            // isn't actually different than the old one. This is tricky in the
            // face of a mutable
            // query list made of mutable QueryGroups. :/
            clearResults();

            setRunQueryButtonEnabledStatus(queryGroups);
        }

        void setRunQueryButtonEnabledStatus(final List<ReadOnlyQueryGroup> queryGroups) {
            runQueryButton.setEnabled(queryGroups.size() > 0);
        }
    }
}
