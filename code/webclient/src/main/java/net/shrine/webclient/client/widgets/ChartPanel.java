package net.shrine.webclient.client.widgets;

import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;

/**
 * 
 * @author clint
 * @date Sep 20, 2012
 */
public final class ChartPanel extends Composite {

    private static ChartPanelUiBinder uiBinder = GWT.create(ChartPanelUiBinder.class);

    interface ChartPanelUiBinder extends UiBinder<Widget, ChartPanel> { }

    @UiField
    SimplePanel chartHolder;
    
    @UiField
    SpanElement chartNameSpan;
    
    public ChartPanel(final String breakdownType, final ColumnChart chart) {
        initWidget(uiBinder.createAndBindUi(this));
        
        this.chartNameSpan.setInnerText(getBreakdownName(breakdownType));
        
        this.chartHolder.setWidget(chart);
    }

    static String getBreakdownName(final String breakdownType) {
        if(!breakdownTypesToNames.containsKey(breakdownType)) {
            Log.warn("Couldn't look up name for unknown breakdown type '" + breakdownType + "'");
            
            return ""; //TODO: Is this ok?
        }
        
        return breakdownTypesToNames.get(breakdownType);
    }

    @SuppressWarnings("serial")
    private static final Map<String, String> breakdownTypesToNames = new HashMap<String, String>() {{
        this.put("PATIENT_AGE_COUNT_XML", "Age");  
        this.put("PATIENT_RACE_COUNT_XML", "Race");
        this.put("PATIENT_VITALSTATUS_COUNT_XML", "Vital Status"); //TODO: verify, not present in mockup
        this.put("PATIENT_GENDER_COUNT_XML", "Gender");
    }};
}
