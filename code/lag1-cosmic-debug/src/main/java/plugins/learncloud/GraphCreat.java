package plugins.learncloud;

import com.google.common.collect.Maps;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.chart.*;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;
import plugins.demo.PointLineChartHelper;

import java.math.BigDecimal;
import java.util.*;

/**
 * 基础资料插件
 */
public class GraphCreat extends AbstractFormPlugin implements Plugin {
    @Override
    public void registerListener(EventObject e) {
        //注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }

    public void itemClick(ItemClickEvent e) {
        super.itemClick(e);
        if (e.getItemKey().equalsIgnoreCase("lag1_baritemap")) {
            PointLineChart pointLineChart = this.getControl("lag1_pointlinechartap");
            PointLineChartHelper pointLineChartHelper = new PointLineChartHelper();
            drawChart(pointLineChart);
            this.getView().updateView("lag1_pointlinechartap");//刷新控件
         }
    }
    public void drawChart(PointLineChart pointLineChart) {
        boolean isX = true;
        // 创建分类轴，X轴方式展现
        Axis categoryAxis = this.createCategoryAxis(pointLineChart, "任务名称", isX);
        // 设置分类轴nametextstyle属性，
        Map<String, Object> nametextstyle = Maps.newHashMap();
        nametextstyle.put("color", "#000000");
        // nametextstyle.put("fontStyle", "italic");
        nametextstyle.put("fontSize", 18);
        categoryAxis.setPropValue("nameTextStyle", nametextstyle);

        // 设置分类轴名称位置属性，end表示在最后
        categoryAxis.setPropValue("nameLocation", new String("end"));

        // 设置分类轴分类值显示位置，bottom表示在下
        categoryAxis.setPropValue("position", "bottom");
        // 设置分类轴分类值liaxisLabel属性
        Map<String, Object> axislabel = Maps.newHashMap();
        Map<String, Object> textstyle = Maps.newHashMap();
        textstyle.put("color", "#000000");
        textstyle.put("fontSize", "16");
        axislabel.put("textStyle", textstyle);
        categoryAxis.setPropValue("axisLabel", axislabel);

        // 创建数据轴，name为其名字。
        Axis ValueAxis = this.createValueAxis(pointLineChart, "天", !isX);
        // 设置数据轴的nameTextStyle属性
        Map<String, Object> yAxisnametextstyle = Maps.newHashMap();
        yAxisnametextstyle.put("color", "#000000");
        yAxisnametextstyle.put("fontSize", 18);
        // yAxisnametextstyle.put("fontStyle", "oblique");
        ValueAxis.setPropValue("nameTextStyle", yAxisnametextstyle);

        // 设置分类轴数据
        categoryAxis.setCategorys(contructCatetoryData());

        // 创建折线并赋值
        this.createLineSeries(pointLineChart, "计划任务耗时", contructValueData(), "red");

        // 创建多个折线按如下方式添加
        this.createLineSeries(pointLineChart, "实际任务耗时", contructValue2Data(), "#0000CD");
        // this.createLineSeries(pointLineChart,"高度", contructValue3Data(), "#282828");

        // 设置图的边距
        pointLineChart.setMargin(Position.right, "80px");
        pointLineChart.setMargin(Position.top, "80px");
        pointLineChart.setMargin(Position.left, "80px");

        // 设置图例的位置
        pointLineChart.setLegendPropValue("top", "8%");
        // 设置图例中文字的字体大小和颜色等
        Map<String, Object> legendtextstyle = Maps.newHashMap();
        legendtextstyle.put("fontSize", 18);
        legendtextstyle.put("color", "#000000");
        pointLineChart.setLegendPropValue("textStyle", legendtextstyle);

        // 刷新图标
        pointLineChart.refresh();
    }

    /**
     * 创建类目型坐标轴
     *
     *            是否X轴，ture创建X轴，false创建Y轴
     */
    private Axis createCategoryAxis(PointLineChart pointLineChart, String name, boolean isx) {
        Axis axis = null;
        if (isx)
            axis = pointLineChart.createXAxis(name, AxisType.category);
        else
            axis = pointLineChart.createYAxis(name, AxisType.category);

        // 创建一个map存储x轴的复杂属性的属性-值对
        Map<String, Object> axisTick = Maps.newHashMap();
        axisTick.put("interval", Integer.valueOf(0));

        axisTick.put("show", true);
        axisTick.put("grid", Position.left);
        axis.setPropValue("axisTick", axisTick);
        return axis;
    }

    /**
     * 创建值类型坐标轴
     *
     * @param name
     *            坐标轴名称
     *            是否X轴，ture创建X轴，false创建Y轴
     */
    private Axis createValueAxis(PointLineChart pointLineChart, String name, boolean isx) {
        Axis axis = null;
        if (isx)
            axis = pointLineChart.createXAxis(name, AxisType.value);
        else
            axis = pointLineChart.createYAxis(name, AxisType.value);

        // 创建一个map存储y轴的复杂属性的属性-值对
        Map<String, Object> axisTick = Maps.newHashMap();
        axisTick.put("show", true);
        axis.setPropValue("axisTick", axisTick);

        // 创建一个map存储y轴的复杂属性的属性-值对
        Map<String, Object> splitLine = Maps.newHashMap();
        Map<String, Object> lineStyle = Maps.newHashMap();
        lineStyle.put("type", "dotted");
        lineStyle.put("color", "#E2E2E2");
        splitLine.put("lineStyle", lineStyle);
        axis.setPropValue("splitLine", splitLine);
        pointLineChart.setShowTooltip(true);
        return axis;
    }

    private List<String> contructCatetoryData() {

        // 此处需修改成实际分类数据，以下为案例数据
        List<String> categoryData = new ArrayList<>();
        DynamicObjectCollection dynamicObjectCollection = this.getModel().getEntryEntity("lag1_entity_daytask");
        for (DynamicObject dynamicObjectSingle : dynamicObjectCollection) {
            String str = dynamicObjectSingle.getString("lag1_task_name");
            categoryData.add(str);
        }
        return categoryData;
    };

    private List<BigDecimal> contructValueData() {

        // 此处需添加实际数构建，以下为案例数据
        List<BigDecimal> valueData = new ArrayList<>();
        DynamicObjectCollection dynamicObjectCollection = this.getModel().getEntryEntity("lag1_entity_daytask");
        for (DynamicObject dynamicObjectSingle : dynamicObjectCollection) {
//            jsonObjectSingle.put("expectTime", dynamicObjectSingle.getString("lag1_expect_minutes"));
            int i=Integer.parseInt(dynamicObjectSingle.getString("lag1_expect_minutes"));
            valueData.add(new BigDecimal(i));
        }

        return valueData;
    }

    private List<BigDecimal> contructValue2Data() {

        // 此处需添加实际数构建，以下为案例数据
        List<BigDecimal> valueData = new ArrayList<>();
        DynamicObjectCollection dynamicObjectCollection = this.getModel().getEntryEntity("lag1_entity_daytask");
        for (DynamicObject dynamicObjectSingle : dynamicObjectCollection) {
            int i=Integer.parseInt(dynamicObjectSingle.getString("lag1_finish_minute"));
            valueData.add(new BigDecimal(i));
        }

        return valueData;
    }

    // 创建折线
    private void createLineSeries(PointLineChart pointLineChart, String name, List<BigDecimal> values, String color) {
        // 折线的名字
        LineSeries expireSeries = pointLineChart.createSeries(name);
        // 设置折线上文本的相关属性
        Label label = new Label();
        label.setShow(true);
        label.setColor("#000000");
        expireSeries.setLabel(label);
        // 连线颜色
        expireSeries.setItemColor(color);
        // 动画效果
        expireSeries.setAnimationDuration(2000);
        // 该点纵坐标的值setData(Number[] data)
        expireSeries.setData((Number[]) values.toArray(new Number[0]));
    }
}