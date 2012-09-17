package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.Observer;
import com.allen_sauer.gwt.log.client.Log;
import net.shrine.webclient.client.util.ReadOnlyObservable;

import java.util.Map;

/**
 * @author clint
 * @date Sep 6, 2012
 */
public final class ResultsPanel extends Composite implements Observer {

    private static ResultsPanelUiBinder uiBinder = GWT.create(ResultsPanelUiBinder.class);

    interface ResultsPanelUiBinder extends UiBinder<Widget, ResultsPanel> {
    }

    @UiField
    FlowPanel resultsInstitutionsDelegate;

    private ReadOnlyObservable<Map<String, Integer>> results;

    public ResultsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void wireUp(final ReadOnlyObservable<Map<String, Integer>> results) {
        this.results = results;
        results.observedBy(this);
        setVisibility(hasResults(results));
    }

    private boolean hasResults(final ReadOnlyObservable<Map<String, Integer>> results) {
        return !results.isEmpty() && !results.get().isEmpty();
    }

    private void setVisibility(final boolean visible) {
        this.setVisible(visible);
    }

    @Override
    public void inform() {
        resultsInstitutionsDelegate.clear();
        Log.debug("Informed of query results!");
        setVisibility(hasResults(results));
        if(hasResults(results)) {
            for(Map.Entry<String, Integer> result : results.get().entrySet()) {
                resultsInstitutionsDelegate.add(new InstitutionResult(result.getKey(), result.getValue()));
            }
        }
    }

    @Override
    public void stopObserving() {
        this.results.noLongerObservedBy(this);
    }
}
