package plugins.graph;

import javafx.util.Pair;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.chart.*;
import kd.bos.form.control.events.RateEvent;
import kd.bos.form.control.events.RateListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

import java.util.*;
import java.util.stream.Collectors;

public class Binchart extends AbstractFormPlugin implements RateListener {
    private double lowdata=0, standard=0, highdata=0;
    @Override
    public void registerListener(EventObject e) {
        // TODO Auto-generated method stub
        super.registerListener(e);
//		Rate rate = this.getControl("kdec_rateap2");
//		rate.addRateListener(this);
//		Rate rate1 = this.getControl("kdec_rateap3");
//		rate1.addRateListener(this);
//		Rate rate2 = this.getControl("kdec_rateap4");
//		rate2.addRateListener(this);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        // TODO Auto-generated method stub
        super.afterCreateNewData(e);
        PieChart pieChart = this.getControl("lag1_piechartap1");
        this.drawChart(pieChart);
    }

    @Override
    public void beforeBindData(EventObject e) {
        // TODO Auto-generated method stub
        super.beforeBindData(e);
    }

    @Override
    public void update(RateEvent evt) {
        // TODO Auto-generated method stub
//		Number rateScore = evt.getRateScore();
//		Rate rate = (Rate) evt.getSource();
//		String key = rate.getKey();
//		this.getPageCache().put(key, rateScore.toString());
//		Number sum = this.handleSumRate(rateScore, key);
//		this.getModel().setValue("kdec_textfield", sum);
        PieChart pieChart = this.getControl("lag1_piechartap1");
        this.drawChart(pieChart);
        //必须刷新，否则不触发签到渲染
        pieChart.refresh();
    }
    public void drawChart(PieChart pieChart) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        //添加数据
        PieSeries series = pieChart.createPieSeries("评分");
        String rateStr = this.getPageCache().get("kdec_rateap2");
        String rate2Str = this.getPageCache().get("kdec_rateap3");
        String rate3Str = this.getPageCache().get("kdec_rateap4");
        ItemValue[] data = new ItemValue[3];
        demo2();
        ItemValue data1 = new ItemValue("掌握较差知识点", lowdata > 0 ? lowdata : 33.3, "#ffb61e");
        ItemValue data2 = new ItemValue("基本掌握知识点", standard > 0 ? standard : 33.3, "#ff8936");
        ItemValue data3 = new ItemValue("掌握较好知识点", highdata > 0 ? standard : 33.3, "#ff7500");
        data[0] = data1;
        data[1] = data2;
        data[2] = data3;
        series.setData(data);
        //series样式
        series.setRoseType(RoseType.radius);
    }

    private Number getRateNumber(String rateStr) {
        Number rate = 0;
        if (rateStr != null) {
            rate = Integer.valueOf(rateStr);
        }
        return rate;
    }
    public void demo2() {
        QFilter filter = new QFilter("creator.id",QCP.equals, RequestContext.get().getCurrUserId()); //筛选本userid的条目
        DynamicObjectCollection dataCollection;
        //查询数据
        dataCollection = QueryServiceHelper.query("lag1_user_data","lag1_data", new QFilter[]{filter});
        for (DynamicObject obj : dataCollection) {
            Object a = obj.getString("lag1_data");
            double value = (getRateNumber(Objects.equals(a, "0.00") ? "0": (String) a)).doubleValue();
            lowdata += (value > 0 && value <= 4) ? value : 0;
            standard += (value > 4 && value <= 7) ? value : 0;
            highdata += (value <= 0 || value > 7) ? value : 0;
        }
        double total = lowdata + standard + highdata;
        lowdata /= total * 100;
        standard /= total * 100;
        highdata /= total * 100;
    }
}