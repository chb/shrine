package net.shrine.webclient.client.widgets;

import java.util.Map;

import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

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

    private ReadOnlyObservable<Map<String, SingleInstitutionQueryResult>> results;

    public ResultsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void wireUp(final ReadOnlyObservable<Map<String, SingleInstitutionQueryResult>> results) {
        this.results = results;
        results.observedBy(this);
        setVisibility(hasResults(results));
    }

    private boolean hasResults(final ReadOnlyObservable<Map<String, SingleInstitutionQueryResult>> results) {
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
        if (hasResults(results)) {
            for (final Map.Entry<String, SingleInstitutionQueryResult> result : results.get().entrySet()) {
                resultsInstitutionsDelegate.add(new InstitutionResult(result.getKey(), result.getValue().getCount()));
            }
        }
    }

    @Override
    public void stopObserving() {
        results.noLongerObservedBy(this);
    }
}
