package plugins.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.ext.form.control.Markdown;
import kd.bos.form.chart.ItemValue;
import kd.bos.form.chart.PieChart;
import kd.bos.form.chart.PieSeries;
import kd.bos.form.chart.radar.*;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.sdk.plugin.Plugin;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.plugin.AbstractFormPlugin;
//import kd.bos.form.plugin.AbstractFormPluginEvents;
//import kd.bos.form.plugin.FormShowParameter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
//import plugins.question.BindPbScore;

import java.math.BigDecimal;
import java.util.*;

/**
 * 基础资料插件
 */
public class HomeworkPigai extends AbstractBasePlugIn implements Plugin {
    private String AIORNOR = "";    //是否ai出题
    //题目分数表单据体名词
    private static final String ENTRY_ENTITY_COLLECTION = "lag1_entryentity_pigai";
    //成绩关联表的表单标识
    private static final String TKPROBLEM_SCORE= "lag1_problem_score";
    private static final String TKPROBLEM = "lag1_protest";
    private static final String TKCOURSE = "lag1_course";
    private static final String TKKNPOINT = "lag1_knowpoints";

    @Override
    public void registerListener(EventObject e) {
        //注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }
    public void itemClick(ItemClickEvent e) {
        super.itemClick(e);
        if (e.getItemKey().equalsIgnoreCase("lag1_ai_pingfen")) {
            //获取日任务信息，并且以JSON字符串的形式展现
            JSONObject jsonResultObject = new JSONObject();
            jsonResultObject.put("taskName", this.getModel().getValue("name").toString());
            jsonResultObject.put("createTime", this.getModel().getValue("createtime").toString());
            DynamicObjectCollection dynamicObjectCollection = this.getModel().getEntryEntity(ENTRY_ENTITY_COLLECTION);
            JSONArray jsonTaskArray = new JSONArray();
            int i=1;
            for (DynamicObject dynamicObjectSingle : dynamicObjectCollection) {
                JSONObject jsonObjectSingle = new JSONObject();
                jsonObjectSingle.put("id", i++);
                jsonObjectSingle.put("problemContent", dynamicObjectSingle.getString("lag1_prodes"));//题干
                jsonObjectSingle.put("userAnswer", dynamicObjectSingle.getString("lag1_useranswer"));//用户作答
                jsonObjectSingle.put("diff", dynamicObjectSingle.getString("lag1_difficulty"));//题目难度
                jsonObjectSingle.put("answer", dynamicObjectSingle.getString("lag1_standard_answer"));//标准答案
                jsonObjectSingle.put("kPoints", dynamicObjectSingle.getString("lag1_link_kpoints"));//相关知识点
                jsonTaskArray.add(jsonObjectSingle);
            }
            jsonResultObject.put("problemsIntroduction", jsonTaskArray);

            //调用AI开发平台微服务
            Map<String , String> variableMap = new HashMap<>();
            Object[] params = new Object[] {
                    //提示词
                    getPromptFid("prompt-2507094056E37A"),
                    jsonResultObject.toJSONString(),
                    variableMap
            };
            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall", params);
            JSONObject jsonObjectResult = new JSONObject(result);
            JSONObject jsonObjectData = jsonObjectResult.getJSONObject("data");
            //设置值
            String str=jsonObjectData.getString("llmValue");//JSON结构的玩意
            String jsonResult = str.replaceAll("\\s*|\r|\n|\t", "");
            JSONObject resultJsonObject = null;
            try {
                //若全部生成JSON字符串，则不会进入catch
                resultJsonObject = JSON.parseObject(jsonResult);
            } catch (Exception ee) {
                //将"knowpoint_plan"的上一个字符作为开始，以}]}字符作为结束，则最后需要+3
                jsonResult = jsonResult.substring(jsonResult.indexOf("\"answer\"")-1 , jsonResult.indexOf("}]}")+3);
                resultJsonObject = JSON.parseObject(jsonResult);
            }

            int sum=0;
            // entryentity是单据体标识，your_field_key是要修改的字段标识
            DynamicObjectCollection entryRows = this.getModel().getEntryEntity(ENTRY_ENTITY_COLLECTION);
            // 将JSONObject转换为金蝶可识别的DynamicObject数组
            for (int j = 0; j < entryRows.size(); j++) {
                JSONObject jsonRow = resultJsonObject.getJSONArray("answer").getJSONObject(j);
                DynamicObject newRow = entryRows.get(j);
                // 批量设置字段值
                sum+=Integer.parseInt(jsonRow.getString("score"));
                newRow.set("lag1_userscore", jsonRow.getString("score"));
                newRow.set("lag1_ai_pigai", jsonRow.getString("analysis"));
            }
            this.getModel().setValue("lag1_textfield1",String.valueOf(sum/entryRows.size()));
            AIORNOR = this.getModel().getDataEntity().getString("lag1_aiornor");
            if (AIORNOR.equals("normal")){
                bindData(); //绑定至成绩关联表
            }else {
//                this.getView().showMessage("ai:ai");
            }
            // 刷新界面显示
            this.getView().updateView(ENTRY_ENTITY_COLLECTION);
        }

    }

    /**
     * 获取aiornormal
     * @param e
     */
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        AIORNOR = this.getModel().getDataEntity().getString("lag1_aiornor");
    }
    //获取提示词的Fid
    public long getPromptFid(String billNo) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("gai_prompt",
                "number," + "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        return dynamicObject.getLong("id");
    }
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        radarchart();
        piechart();
    }
    public void radarchart(){
        RadarChart chart = this.getControl("lag1_radarchartap");
        RadarAxis radarAxis = new RadarAxis();// 构建轴
        List<RadarIndicator> indicators = new ArrayList<RadarIndicator>();
        RadarIndicator indicator1 = new RadarIndicator("基础题掌握度",6500);
        RadarIndicator indicator2 = new RadarIndicator("高难创新能力",16000);
        RadarIndicator indicator3 = new RadarIndicator("全面程度",30000);
        RadarIndicator indicator4 = new RadarIndicator("选择题答题技巧",38000);
        RadarIndicator indicator5 = new RadarIndicator("问答题答题技巧",52000);
        indicators.add(indicator1);
        indicators.add(indicator2);
        indicators.add(indicator3);
        indicators.add(indicator4);
        indicators.add(indicator5);
        radarAxis.setIndicator(indicators);// 设置轴线
        chart.addRadarAxis(radarAxis);
        RadarSeries radarSeries = new RadarSeries();
        RadarData radarData = new RadarData();//构建数据
        radarData.setName("张泽明(这里传学号)");//数据一
//        radarData.setValue(new Number[] {4200, 3000, 20000, 35000, 50000, 18000});
        radarData.setValue(new Number[] {4200, 3000, 20000, 35000, 50000});
        Map<String, Object>tooltip = new HashMap<String, Object>();
        tooltip.put("trigger", "item");
        RadarData radarData1 = new RadarData();
//        radarData1.setName("Actual Spending");//数据二
//        radarData1.setValue(new Number[] {5000, 14000, 28000, 26000, 42000, 21000});
//        radarData1.setValue(new Number[] {5000, 14000});
        radarSeries.addData(radarData);
        radarSeries.addData(radarData1);
        radarSeries.setPropValue("tooltip", tooltip);//设置tips
        chart.addRadarSeries(radarSeries);
        chart.refresh();
        chart.setShowTooltip(true);//开启tips
        this.getView().updateView("lag1_radarchartap");//刷新控件

        long org = UserServiceHelper.getUserDefaultOrgID(Long.valueOf(RequestContext.get().getUserId()));
        UserServiceHelper.setUserDefaultOrg(Long.valueOf(RequestContext.get().getUserId()), 100000L);
    }

    public void piechart(){
        PieChart pieChart = this.getControl("lag1_piechartap");
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
        this.getView().updateView("lag1_piechartap");
    }

    private ItemValue[] getDefaultProfitData() {
        ItemValue[] items = new ItemValue[4];
        ItemValue item1 = new ItemValue("困难题平均分", new BigDecimal(10));
        items[0] = item1;
        ItemValue item2 = new ItemValue("进阶题平均分", new BigDecimal(7));
        items[1] = item2;
        ItemValue item3 = new ItemValue("中等题平均分", new BigDecimal(5));
        items[2] = item3;
        ItemValue item4 = new ItemValue("基础题平均分", new BigDecimal(4));
        items[3] = item4;
        return items;
    }

    /**
     * 成绩关联表方法
     */
    private void bindData(){
        //            获取当前表单的数据实体
        DynamicObjectCollection dataEntities = this.getModel().getEntryEntity(ENTRY_ENTITY_COLLECTION);
        if(dataEntities==null){
            this.getView().showMessage("未找到数据实体");
            return;
        }
        String studentid = String.valueOf(RequestContext.get().getCurrUserId());
        String studentname=RequestContext.get().getUserName();
        //遍历单据体每一行,lag1_proid, lag1_protype,
        for(DynamicObject entryEntity:dataEntities){
            //new成绩关联表的表单对象
            String proid = entryEntity.getString("lag1_proid"); //题目id
            String userscore = entryEntity.getString("lag1_userscore");
            String courseid = entryEntity.getString("lag1_courseid");
            String knpoints = entryEntity.getString("lag1_link_kpoints");
            this.getView().showMessage("kn"+knpoints);
            String[] knpointArr = knpoints.split(",");
            String knpoint1="";
            String knpoint2 = "";
            if(knpointArr.length>0){
                knpoint1 = knpointArr[0].trim();
            }
            if(knpointArr.length>1){
                knpoint2 = knpointArr[1].trim();
            }
            //插入之前能否先去TKPROBLEM_SCORE查找是否有数据的lag1_studentid与numer 和即将插入的studentid与proid一致，若一致，则只更新分数即lag1_score
//                否则新建数据
            DynamicObject existingRecord = findExistingRecord(studentid,proid);


            if(existingRecord!=null){
                this.getView().showMessage(existingRecord.toString());
                existingRecord.set("lag1_score",Integer.parseInt(userscore));
                SaveServiceHelper.update(existingRecord);
            }else{
                this.getView().showMessage("不存在");
                String field1 = "number";
                QFilter qFilter1 = new QFilter("number", QCP.equals,proid);
                DynamicObject problem = BusinessDataServiceHelper.loadSingle(TKPROBLEM,field1,new QFilter[]{qFilter1});

                String field2 = "number";
                QFilter qFilter2 = new QFilter("number", QCP.equals,courseid);
                DynamicObject course = BusinessDataServiceHelper.loadSingle(TKCOURSE,field2,new QFilter[]{qFilter2});

                String field3 = "name";
                QFilter qFilter3 = new QFilter("name",QCP.equals,knpoint1);
                DynamicObject knpointname1 = BusinessDataServiceHelper.loadSingle(TKKNPOINT,field3,new QFilter[]{qFilter3});

                String field4 = "name";
                QFilter qFilter4 = new QFilter("name",QCP.equals,knpoint2);
                DynamicObject knpointname2 = BusinessDataServiceHelper.loadSingle(TKKNPOINT,field4,new QFilter[]{qFilter4});


                DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject(TKPROBLEM_SCORE);
                dynamicObject.set("lag1_studentid",studentid);
                dynamicObject.set("lag1_studentname",studentname);
                dynamicObject.set("status", "C");
                dynamicObject.set("enable", 1);
                dynamicObject.set("lag1_proid",problem);
                dynamicObject.set("lag1_score",Integer.parseInt(userscore));
                dynamicObject.set("lag1_courseid",course);

                if(!knpoint1.isEmpty()){
                    dynamicObject.set("lag1_knowpoint1",knpointname1);
                }else{
//                    dynamicObject.set("lag1_knowpoint1","未绑定");
                }
                if(!knpoint1.isEmpty()){
                    dynamicObject.set("lag1_knowpoint2",knpointname2);
                }else{
//                    dynamicObject.set("lag1_knowpoint2","未绑定");
                }
                try {
                    SaveServiceHelper.saveOperate(TKPROBLEM_SCORE,new DynamicObject[]{dynamicObject},null);
                }catch (Exception e){
                    this.getView().showMessage("exception: "+e);
                }
            }
        }
        this.getView().showMessage("更新数据成功");
    }
    private DynamicObject findExistingRecord(String studentid, String proid) {
        // 定义要查询的字段（可选，如果不需要特定字段可以传 null 或空字符串）
        String fields = "lag1_score"; // 或者直接传 null/"" 表示查询所有字段

        // 构建 QFilter 条件
        QFilter filter1 = new QFilter("lag1_studentid", QCP.equals, studentid);
        QFilter filter2 = new QFilter("lag1_proid.number", QCP.equals, proid);
        QFilter combinedFilter = QFilter.and(filter1, filter2); // 组合两个条件

        // 执行查询
        DynamicObject[] records = BusinessDataServiceHelper.load(TKPROBLEM_SCORE, fields, new QFilter[]{combinedFilter});

        // 返回第一条记录（如果没有则返回 null）
        return records.length > 0 ? records[0] : null;
    }
}