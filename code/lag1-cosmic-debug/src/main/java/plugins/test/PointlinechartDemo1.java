package plugins.test;

import kd.bos.form.chart.PointLineChart;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

public class PointlinechartDemo1 extends AbstractFormPlugin implements Plugin {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);

        PointLineChart pointLineChart = this.getControl("kdec_pointlinechartap");
        PointLineChartHelper pointLineChartHelper = new PointLineChartHelper();
        pointLineChartHelper.drawChart(pointLineChart);
    }
}