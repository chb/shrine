package net.shrine.webclient.client.widgets;

import java.util.Arrays;
import java.util.List;

import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.Breakdown;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;

/**
 * @author Bill Simons
 * @date 9/19/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public final class ResultDetails extends FlowPanel {

    private static final int CHART_WIDTH = 660;
    private static final int CHART_HEIGHT = 270;
    
    //TODO: Make ResultOutputType enum available to GWT to avoid this hard-coding
    private static final List<String> breakdownTypes = Arrays.asList("PATIENT_GENDER_COUNT_XML", "PATIENT_AGE_COUNT_XML", "PATIENT_RACE_COUNT_XML", "PATIENT_VITALSTATUS_COUNT_XML");

    public ResultDetails(final SingleInstitutionQueryResult result) {
        super();
        
        this.setVisible(false);
        
        for (final String breakdownType : breakdownTypes) {
            final Widget breakdownWidget;
            
            if(result.getBreakdowns().containsKey(breakdownType)) {
                final Breakdown breakdown = result.getBreakdowns().get(breakdownType);
                
                final ColumnChart chart = new ColumnChart(createDataTable(breakdown), createOptions());
                
                breakdownWidget = new ChartPanel(breakdownType, chart);
            } else {
                breakdownWidget = new MissingBreakdownPanel();
            }
            
            this.add(breakdownWidget);
        }
        
        this.setVisible(true);
    }

    private static Options createOptions() {
        final Options options = CoreChart.createOptions();
        options.setHeight(CHART_HEIGHT);
        options.setWidth(CHART_WIDTH);

        final AxisOptions vAxisOptions = AxisOptions.create();
        vAxisOptions.setMinValue(0);

        options.setVAxisOptions(vAxisOptions);
        return options;
    }

    private static DataTable createDataTable(final Breakdown breakdown) {
        final DataTable data = DataTable.create();
        
        data.addColumn(AbstractDataTable.ColumnType.STRING, "Demographics Category");
        data.addRows(1);
        data.setValue(0, 0, "# of Patients");
        
        //TODO: Is sorting by column name appropriate here?  Hopefully it will be enough, though
        //we can always preserve the order in which the columns come from i2b2 
        final List<String> columnNames = Util.sorted(breakdown.keySet());
        
        //NB: Defensive scope, to avoid leaking i
        {
            int i = 0;
        
            for (final String columnName : columnNames) {
                data.addColumn(AbstractDataTable.ColumnType.NUMBER, columnName);
                data.setValue(0, i + 1, breakdown.get(columnName));
                
                ++i;
            }
        }
        
        return data;
    }
}