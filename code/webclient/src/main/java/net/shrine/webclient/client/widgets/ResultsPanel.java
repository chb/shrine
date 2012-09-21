package net.shrine.webclient.client.widgets;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import net.shrine.webclient.client.state.InstitutionResultClickedEvent;
import net.shrine.webclient.client.state.InstitutionResultClickedEventHandler;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;

import java.util.List;
import java.util.Map;

/**
 * @author clint
 * @date Sep 6, 2012
 */
public final class ResultsPanel extends Composite implements Observer {

    private static ResultsPanelUiBinder uiBinder = GWT.create(ResultsPanelUiBinder.class);
    private EventBus eventBus;

    interface ResultsPanelUiBinder extends UiBinder<Widget, ResultsPanel> {
    }

    @UiField
    FlowPanel resultsInstitutionsDelegate;

    @UiField
    SimplePanel resultDetails;

    private ReadOnlyObservable<Map<String, SingleInstitutionQueryResult>> results;
    private final List<InstitutionResult> institutionResults = Util.makeArrayList();
    private int currentSelectIndex = -1;

    public ResultsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void wireUp(EventBus eventBus, final ReadOnlyObservable<Map<String, SingleInstitutionQueryResult>> results) {
        this.eventBus = eventBus;
        this.results = results;
        results.observedBy(this);
        setVisibility(hasResults(results));
        this.eventBus.addHandler(InstitutionResultClickedEvent.getType(), new InstitutionResultClickedEventHandler() {
            @Override
            public void handle(InstitutionResultClickedEvent event) {
                Log.debug("received institution result clicked event");
                if(currentSelectIndex != -1){
                    institutionResults.get(currentSelectIndex).setSelected(false);
                }
                currentSelectIndex = event.getIndex();
                selectCurrentResult();
            }
        });

    }

    private boolean hasResults(final ReadOnlyObservable<Map<String, SingleInstitutionQueryResult>> results) {
        return !results.isEmpty() && !results.get().isEmpty();
    }

    private void setVisibility(final boolean visible) {
        this.setVisible(visible);
    }

    @Override
    public void inform() {
        Log.debug("Informed of query results!");
        clearResults();
        setVisibility(hasResults(results));
        if(hasResults(results)) {
            int i = 0;
            for(Map.Entry<String, SingleInstitutionQueryResult> result : results.get().entrySet()) {
                InstitutionResult institutionResult = new InstitutionResult(eventBus, i, result.getKey(), result.getValue());
                institutionResults.add(institutionResult);
                resultsInstitutionsDelegate.add(institutionResult);
                i++;
            }
            currentSelectIndex = 0;
            selectCurrentResult();
        }
    }

    private void selectCurrentResult() {
        InstitutionResult currentResult = institutionResults.get(currentSelectIndex);
        currentResult.setSelected(true);
        resultDetails.setWidget(new ResultDetails(currentResult.getResult()));
    }

    private void clearResults() {
        resultsInstitutionsDelegate.clear();
        institutionResults.clear();
        currentSelectIndex = -1;
    }

    @Override
    public void stopObserving() {
        results.noLongerObservedBy(this);
    }
}
