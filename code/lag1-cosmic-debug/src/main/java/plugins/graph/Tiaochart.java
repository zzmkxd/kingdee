package plugins.graph;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.form.chart.CustomChart;
import kd.bos.form.chart.Axis;
import kd.bos.form.chart.AxisType;
import kd.bos.form.chart.BarSeries;
import kd.bos.form.control.Control;
import kd.bos.context.RequestContext;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

import java.math.BigDecimal;
import java.util.*;

/**
 * 动态表单条形统计图插件
 */
public class Tiaochart extends AbstractFormPlugin {
    // 通用图控件标识
    private static final String CHART_CONTROL_KEY = "lag1_customchartap";
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        try {
            CustomChart chart = this.getControl(CHART_CONTROL_KEY);
            chart.clearData();

            // 1. 从业务数据获取图表数据
            List<Map<Object, Object>> chartData = loadChartDataFromService(demo());
            // 2. 准备图表数据
            List<String> categories = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();
            for (Map<Object, Object> item : chartData) {
                categories.add(item.get("category").toString());
                values.add(new BigDecimal(item.get("amount").toString()));
            }
            // 3. 设置X轴
            Axis xAxis = chart.createXAxis("知识点名称", AxisType.category);
            xAxis.setCategorys(categories);
            // 4. 设置Y轴
            Axis yAxis = chart.createYAxis("掌握度(%)", AxisType.value);
            yAxis.setMin(0);
            yAxis.setMax(100);
            // 5. 创建系列
            BarSeries series = chart.createBarSeries("掌握度");
            series.setData(values.toArray(new BigDecimal[0]));
            // 6. 设置图表标题
            chart.setName(new LocaleString("知识点掌握度统计"));
            // 7. 刷新图表
            chart.refresh();
        } catch (Exception ex) {
            this.getView().showErrorNotification("加载图表失败: " + ex.getMessage());
        }
    }

    // 模拟从服务获取数据
    private List<Map<Object, Object>> loadChartDataFromService(KeyValueList chartData) {
        return chartData.toListOfMaps();
    }

    public KeyValueList demo() {

        QFilter filter = new QFilter("lag1_data", QCP.not_equals,null);
        filter.and(new QFilter("creator.id",QCP.equals, RequestContext.get().getCurrUserId())); //筛选本userid的条目
        String orderBy = "lag1_data asc";

        DynamicObjectCollection dataCollection;
        //查询数据，限制返回8条
        dataCollection = QueryServiceHelper.query(
                "lag1_user_data",
                "lag1_linkkp.name,lag1_data",
                new QFilter[]{filter},
                orderBy,
                8
        );
// 遍历结果获取lag1_linkkp字段值
        KeyValueList linkkpValues = new KeyValueList();
        for (DynamicObject obj : dataCollection) {
            Object linkkpValue = obj.get("lag1_linkkp.name");
            Object linkdata = obj.get("lag1_data");
            linkkpValues.add(linkkpValue, linkdata);
        }
//        this.getView().showMessage(linkkpValues.toString());
        return linkkpValues;
    }
}
