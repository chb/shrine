package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Sep 6, 2012
 */
public final class ResultsPanel extends Composite {

    private static ResultsPanelUiBinder uiBinder = GWT.create(ResultsPanelUiBinder.class);

    interface ResultsPanelUiBinder extends UiBinder<Widget, ResultsPanel> {
    }

    public ResultsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
