package plugins.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.gpt.IGPTAction;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础资料插件
 */
public class LearningTaskCalendar implements IGPTAction {
    private String str = null;
//输入有：学生对知识点方案的平均正确率+该知识点的做题数量+想提升的模式（基础/中等+困难）+ 想要掌握的知识点列表
    @Override
    public Map<String, String> invokeAction(String action, Map<String, String> params) {
        Map<String , String> result = new HashMap<>();
        if ("GET_JSON_STRING".equalsIgnoreCase(action)) {
            //将无效字符进行处理
            String jsonResult = params.get("jsonResult").replaceAll("\\s*|\r|\n|\t","");
            JSONObject resultJsonObject = null;
            try {
                //若全部生成JSON字符串，则不会进入catch
                resultJsonObject = JSON.parseObject(jsonResult);
            } catch (Exception ee) {
                //将"dayname"的上一个字符作为开始，以}]}字符作为结束，则最后需要+3
                jsonResult = jsonResult.substring(jsonResult.indexOf("\"planName\"")-1 , jsonResult.indexOf("}]}")+3);
                resultJsonObject = JSON.parseObject(jsonResult);
            }
            str=jsonResult.substring(jsonResult.indexOf("\"id\"")-2 , jsonResult.indexOf("}]")+2);
            //new一个DynamicObject表单对象
            DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject("lag1_calendar_plan");
            StringBuilder sb1 = new StringBuilder();
            for (int i = 1; i <= 10; i++) {
                int ascii = 48 + (int) (Math.random() * 9);
                char c = (char) ascii;
                sb1.append(c);
            }
            //设置对应属性
            dynamicObject.set("number", sb1.toString());
            dynamicObject.set("name", resultJsonObject.getString("planName"));
            dynamicObject.set("status", "C");
            dynamicObject.set("enable", 1);
            dynamicObject.set("creator", RequestContext.get().getCurrUserId());
//            lag1_textfield
            dynamicObject.set("lag1_textfield", str);
            //操作单据体
            DynamicObjectCollection dynamicObjectCollection = dynamicObject.getDynamicObjectCollection("lag1_entity_daytask");
            for (Object object : resultJsonObject.getJSONArray("dayTaskList")) {
                JSONObject jsonObjectSingle = (JSONObject) object;
                DynamicObject dynamicObjectEntry = dynamicObjectCollection.addNew();
                dynamicObjectEntry.set("lag1_task_name", jsonObjectSingle.getString("name"));
                dynamicObjectEntry.set("lag1_description", jsonObjectSingle.getString("description"));
                dynamicObjectEntry.set("lag1_expect_minutes", getDaysBetween(jsonObjectSingle.getString("start"),jsonObjectSingle.getString("end")));
                dynamicObjectEntry.set("lag1_begindate", jsonObjectSingle.getString("start"));
                dynamicObjectEntry.set("lag1_enddate", jsonObjectSingle.getString("end"));
                dynamicObjectEntry.set("lag1_diff", jsonObjectSingle.getString("difficulty"));
            }
            SaveServiceHelper.saveOperate("lag1_calendar_plan", new DynamicObject[] {dynamicObject}, null);

            Long pkId = (Long) dynamicObject.getPkValue();
            //拼接URL字符串
            String targetForm = "bizAction://currentPage?gaiShow=1&selectedProcessNumber=processNumber&gaiAction=showBillForm&gaiParams={\"appId\":\"lag1_learn_helper\",\"billFormId\":\"lag1_calendar_plan\",\"billPkId\":\""+pkId+"\"}&title=学习任务日历生成表单&iconType=bill&method=bizAction";
            System.out.println(targetForm);
            result.put("formUrl", targetForm);
            result.put("resultJsonObject", resultJsonObject.toJSONString());
        }
        return result;
    }

    public static int getDaysBetween(String startDateStr, String endDateStr) {

            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            return (int) Math.abs(ChronoUnit.DAYS.between(startDate, endDate));
    }
}