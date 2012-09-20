package net.shrine.webclient.client.widgets;

import java.util.List;
import java.util.Map;

import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.Breakdown;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;

import com.google.gwt.user.client.ui.FlowPanel;
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

    public ResultDetails(final SingleInstitutionQueryResult result) {
        super();
        
        this.setVisible(false);
        
        for (final Map.Entry<String, Breakdown> entry : result.getBreakdowns().entrySet()) {
            final Breakdown breakdown = entry.getValue();
            final String breakdownType = entry.getKey();
            
            final DataTable dataTable = getDataTable(breakdown);

            final Options options = CoreChart.createOptions();
            options.setHeight(167); //TODO: Factor out somehow
            options.setWidth(465); //TODO: Factor out somehow

            final AxisOptions vAxisOptions = AxisOptions.create();
            vAxisOptions.setMinValue(0);
            
            options.setVAxisOptions(vAxisOptions);
            
            final ColumnChart chart = new ColumnChart(dataTable, options);
            
            this.add(new ChartPanel(breakdownType, chart));
        }
        
        this.setVisible(true);
    }

    private static DataTable getDataTable(final Breakdown value) {
        final DataTable data = DataTable.create();
        
        data.addColumn(AbstractDataTable.ColumnType.STRING, "Demographics Category");
        data.addRows(1);
        data.setValue(0, 0, "# of Patients");
        
        final List<Map.Entry<String, Long>> entries = Util.makeArrayList(value.entrySet());
        
        for (int i = 0; i < entries.size(); i++) {
            data.addColumn(AbstractDataTable.ColumnType.NUMBER, entries.get(i).getKey());
            data.setValue(0, i + 1, entries.get(i).getValue());
        }
        
        return data;
    }
}