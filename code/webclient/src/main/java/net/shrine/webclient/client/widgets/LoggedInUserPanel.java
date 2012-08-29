package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * @author Bill Simons
 * @date 8/29/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class LoggedInUserPanel extends Composite {
    interface LoggedInUserPanelUiBinder extends UiBinder<HTMLPanel, LoggedInUserPanel> { }

    private static LoggedInUserPanelUiBinder uiBinder = GWT.create(LoggedInUserPanelUiBinder.class);

    private static final String logoutUrlBase = "j_spring_security_logout";
    
    @UiField
    SpanElement username;

    @UiField
    Anchor logoutLink;
    
    public LoggedInUserPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        username.setInnerText("Seth Paine");
        
        logoutLink.setHref(logoutUrlBase + Window.Location.getQueryString());
    }
}