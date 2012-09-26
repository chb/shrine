package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Sep 26, 2012
 */
public final class MissingBreakdownsPanel extends Composite {

    private static MissingBreakdownsPanelUiBinder uiBinder = GWT.create(MissingBreakdownsPanelUiBinder.class);

    interface MissingBreakdownsPanelUiBinder extends UiBinder<Widget, MissingBreakdownsPanel> { }

    public MissingBreakdownsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
