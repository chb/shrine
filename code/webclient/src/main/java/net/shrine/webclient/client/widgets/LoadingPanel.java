package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author clint
 * @date Oct 4, 2012
 */
public final class LoadingPanel extends Composite {

    private static LoadingPanelUiBinder uiBinder = GWT.create(LoadingPanelUiBinder.class);

    interface LoadingPanelUiBinder extends UiBinder<Widget, LoadingPanel> { }

    public LoadingPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
