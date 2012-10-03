package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Sep 26, 2012
 */
public final class MissingBreakdownPanel extends Composite {

    private static MissingBreakdownsPanelUiBinder uiBinder = GWT.create(MissingBreakdownsPanelUiBinder.class);

    interface MissingBreakdownsPanelUiBinder extends UiBinder<Widget, MissingBreakdownPanel> { }

    @UiField
    SpanElement breakdownTypeSpan;
    
    public MissingBreakdownPanel(final String breakdownName) {
        initWidget(uiBinder.createAndBindUi(this));
        
        breakdownTypeSpan.setInnerText(breakdownName);
    }
}
