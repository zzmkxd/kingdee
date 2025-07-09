package plugins.test;

import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.chart.ItemValue;
import kd.bos.form.chart.PieChart;
import kd.bos.form.chart.PieSeries;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;
import java.math.BigDecimal;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
public class DemoFormPlugin01 extends AbstractFormPlugin implements Plugin {
    public DemoFormPlugin01() {
    }

    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        PieChart pieChart = this.getControl("kdec_piechartap");
        pieChart.setShowTitle(false);
        pieChart.setShowTooltip(true);
        pieChart.addTooltip("trigger","item");
        pieChart.setShowLegend(true);
//        pieChart.setLegendPropValue("top","1%");
        pieChart.setLegendPropValue("left","center");
        //创建echarts中的series对象
        PieSeries series = pieChart.createPieSeries(ResManager.loadKDString("Access From", "PieChartCardDemoPlugin_0", "bos-portal-plugin", new Object[0]));
        ItemValue[] items = this.getDefaultProfitData();
        series.setData(items);
        series.setPropValue("name", "Access From");
        series.setPropValue("type", "pie");
        series.setPropValue("avoidLabelOverlap", Boolean.FALSE);
        //设置内圆与外圆半径
        series.setRadius("40%", "70%");
        //构造series子属性
        //itemStyle
        Map map = new HashMap();
        map.put("borderRadius",10);
        map.put("borderColor","#fff");
        map.put("borderWidth",2);
        series.setPropValue("itemStyle", map);
        //label
        map = new HashMap();
        Map normalMap = new HashMap();
        map.put("show", Boolean.FALSE);
        map.put("position", "center");
        series.setPropValue("label", map);
        //labelLine
        map = new HashMap();
        map.put("show", Boolean.FALSE);
        series.setPropValue("labelLine", map);
        //emphasis
        map = new HashMap();
        normalMap = new HashMap();
        map.put("label",normalMap);
        normalMap.put("show",Boolean.TRUE);
        normalMap.put("fontSize",20);
        normalMap.put("fontWeight","bold");
        series.setPropValue("emphasis",map);
    }

    private ItemValue[] getDefaultProfitData() {
        ItemValue[] items = new ItemValue[5];
        ItemValue item1 = new ItemValue("Search Engine", new BigDecimal(1048));
        items[0] = item1;
        ItemValue item2 = new ItemValue("Direct", new BigDecimal(735));
        items[1] = item2;
        ItemValue item3 = new ItemValue("Email", new BigDecimal(580));
        items[2] = item3;
        ItemValue item4 = new ItemValue("Union Ads", new BigDecimal(484));
        items[3] = item4;
        ItemValue item5 = new ItemValue("Video Ads", new BigDecimal(300));
        items[4] = item5;
        return items;
    }
}
