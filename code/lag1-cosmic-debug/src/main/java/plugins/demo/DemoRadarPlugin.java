package plugins.demo;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kd.bos.context.RequestContext;
import kd.bos.form.chart.radar.RadarAxis;
import kd.bos.form.chart.radar.RadarChart;
import kd.bos.form.chart.radar.RadarData;
import kd.bos.form.chart.radar.RadarIndicator;
import kd.bos.form.chart.radar.RadarSeries;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.servicehelper.user.UserServiceHelper;

public class DemoRadarPlugin extends AbstractFormPlugin {

	@Override
	public void registerListener(EventObject e) {
		super.registerListener(e);
	}

	@Override
	public void afterBindData(EventObject e) {
		super.afterBindData(e);
		RadarChart chart = this.getControl("lag1_radarchartap");
		RadarAxis radarAxis = new RadarAxis();// 构建轴
		List<RadarIndicator> indicators = new ArrayList<RadarIndicator>();
		RadarIndicator indicator1 = new RadarIndicator("Sales",6500);
		RadarIndicator indicator2 = new RadarIndicator("Administration",16000);
		RadarIndicator indicator3 = new RadarIndicator("Information Technology",30000);
		RadarIndicator indicator4 = new RadarIndicator("Customer Support",38000);
		RadarIndicator indicator5 = new RadarIndicator("Development",52000);
		RadarIndicator indicator6 = new RadarIndicator("Marketing",25000);
		indicators.add(indicator1);
		indicators.add(indicator2);
		indicators.add(indicator3);
		indicators.add(indicator4);
		indicators.add(indicator5);
		indicators.add(indicator6);
		radarAxis.setIndicator(indicators);// 设置轴线
		chart.addRadarAxis(radarAxis);
		RadarSeries radarSeries = new RadarSeries();
		RadarData radarData = new RadarData();//构建数据
		radarData.setName("Allocated Budget");//数据一
		radarData.setValue(new Number[] {4200, 3000, 20000, 35000, 50000, 18000});
		Map<String, Object>tooltip = new HashMap<String, Object>();
		tooltip.put("trigger", "item");
		RadarData radarData1 = new RadarData();
		radarData1.setName("Actual Spending");//数据二
		radarData1.setValue(new Number[] {5000, 14000, 28000, 26000, 42000, 21000});
		radarSeries.addData(radarData);
		radarSeries.addData(radarData1);
		radarSeries.setPropValue("tooltip", tooltip);//设置tips
		chart.addRadarSeries(radarSeries);
		chart.refresh();
		chart.setShowTooltip(true);//开启tips
		this.getView().updateView("lag1_radarchartap");//刷新控件
		
		long org = UserServiceHelper.getUserDefaultOrgID(Long.valueOf(RequestContext.get().getUserId()));
		UserServiceHelper.setUserDefaultOrg(Long.valueOf(RequestContext.get().getUserId()), 100000L);
		System.out.println(org);
	}

}
