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
public final class ResultErrorPanel extends Composite {

    private static ResultErrorPanelUiBinder uiBinder = GWT.create(ResultErrorPanelUiBinder.class);

    interface ResultErrorPanelUiBinder extends UiBinder<Widget, ResultErrorPanel> { }

    public ResultErrorPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
