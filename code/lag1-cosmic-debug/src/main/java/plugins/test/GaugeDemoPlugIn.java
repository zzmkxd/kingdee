package plugins.test;

import kd.bos.form.chart.ChartType;
import kd.bos.form.chart.GaugeChart;
import kd.bos.form.chart.GaugeSeries;
import kd.bos.form.chart.ItemValue;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态表单插件
 */
public class GaugeDemoPlugIn extends AbstractFormPlugin implements Plugin {
    @Override
    public void afterBindData(EventObject e) {
        // TODO Auto-generated method stub
        super.afterBindData(e);
        // 声明一个仪表盘对象
        GaugeChart chart = this.getControl("kdec_gaugechartap");
        GaugeSeries gaugeSeries = chart.createSeries("gauge");
        gaugeSeries.setType(ChartType.gauge);// 构造为扇形
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("formatter", "{value}%");
        // map.put("show", false);
        gaugeSeries.setDetail(map);
        ItemValue itemValue = new ItemValue("test", 80);
        ItemValue itemValue1 = new ItemValue("test1", 56);
        ItemValue[] itemValues = new ItemValue[] { itemValue, itemValue1 };
        gaugeSeries.setData(itemValues);
        chart.refresh();
    }
}