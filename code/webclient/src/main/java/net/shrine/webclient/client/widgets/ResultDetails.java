package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import net.shrine.webclient.shared.domain.Breakdown;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Bill Simons
 * @date 9/19/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class ResultDetails extends Composite {
    interface ResultDetailsUiBinder extends UiBinder<HTMLPanel, ResultDetails> {
    }

    private static ResultDetailsUiBinder uiBinder = GWT.create(ResultDetailsUiBinder.class);

    @UiField
    FlowPanel delegate;

    public ResultDetails(final SingleInstitutionQueryResult result) {
        initWidget(uiBinder.createAndBindUi(this));
        for(Map.Entry<String, Breakdown> entry : result.getBreakdowns().entrySet()) {
            DataTable dataTable = getDataTable(entry.getValue());
            Options options = CoreChart.createOptions();
            options.setHeight(167);
            options.setTitle(entry.getKey());
            options.setWidth(465);

            AxisOptions vAxisOptions = AxisOptions.create();
            vAxisOptions.setMinValue(0);
            options.setVAxisOptions(vAxisOptions);
            ColumnChart chart = new ColumnChart(dataTable, options);
            delegate.add(chart);
        }
        delegate.setVisible(true);
    }


    private static DataTable getDataTable(Breakdown value) {
        DataTable data = DataTable.create();
        data.addColumn(AbstractDataTable.ColumnType.STRING, "Demographics Category");
        data.addRows(1);
        data.setValue(0, 0, "# of Patients");
        ArrayList<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(value.entrySet());
        for(int i = 0; i < entries.size(); i++) {
            data.addColumn(AbstractDataTable.ColumnType.NUMBER, entries.get(i).getKey());
            data.setValue(0, i+1, entries.get(i).getValue());
        }
        return data;
    }
}