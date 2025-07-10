package plugins.test;

import java.util.EventObject;

import kd.bos.form.chart.ItemValue;
import kd.bos.form.chart.PieChart;
import kd.bos.form.chart.PieSeries;
import kd.bos.form.chart.Position;
import kd.bos.form.chart.RoseType;
import kd.bos.form.control.Rate;
import kd.bos.form.control.events.RateEvent;
import kd.bos.form.control.events.RateListener;
import kd.bos.form.plugin.AbstractFormPlugin;

public class RateDemoFormPlugin extends AbstractFormPlugin implements RateListener {
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

//	public Number handleSumRate(Number score, String key) {
//		Number sum = score;
//		if (!key.equals("kdec_rateap2")) {
//			sum = getSum(sum, "kdec_rateap2");
//		}
//		if (!key.equals("kdec_rateap3")) {
//			sum = getSum(sum, "kdec_rateap3");
//		}
//		if (!key.equals("kdec_rateap4")) {
//			sum = getSum(sum, "kdec_rateap4");
//		}
//		Number rate5 = sum.intValue() / 3;
//		Rate rate = this.getControl("kdec_rateap5");
//		rate.setRateScore(rate5);
//		return rate5;
//	}

//	private Number getSum(Number sum, String key) {
//		String rate = this.getPageCache().get(key);
//		if (rate != null) {
//			sum = sum.intValue() + Integer.valueOf(rate);
//		}
//		return sum;
//	}

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
		ItemValue data1 = new ItemValue("张泽明得分", getRateNumber("30"), "#ffb61e");
		ItemValue data2 = new ItemValue("陈雯雯得分", getRateNumber("30"), "#ff8936");
		ItemValue data3 = new ItemValue("吴昊得分", getRateNumber("40"), "#ff7500");
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
}
