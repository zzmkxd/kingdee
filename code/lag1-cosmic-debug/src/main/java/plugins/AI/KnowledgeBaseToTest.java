package plugins.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDException;
import kd.bos.ext.form.control.CountDown;
import kd.bos.form.control.Control;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Filter;

/**
 * 动态表单插件
 */
public class KnowledgeBaseToTest extends AbstractFormPlugin implements Plugin {
    private Map<String , String> result = new HashMap<>();
    @Override
    public void registerListener(EventObject e) {
        //注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
        this.getView().getControl("btnok");
    }
//    lag1_basedatafield2课程名字 btnok提交
//    lag1_basedatafield lag1_basedatafield1 lag1_basedatafield3 三个知识
//    lag1_stepperfield 题目数量 lag1_stepperfield1 题目难度系数1-10
    public void click(EventObject e) throws KDException {
//        DynamicObject dynamicObjectSubscribe = BusinessDataServiceHelper.loadSingle("ozwe_subscribe", new QFilter[]{new QFilter("creator.id", QCP.equals, requestContext.getCurrUserId())});
//        if (ObjectUtils.isNotEmpty(dynamicObjectSubscribe)) {
        super.click(e);
        Control source = (Control) e.getSource();
        String key = source.getKey();
//这里是考试开始的逻辑，最好替换成动态表单弹窗-点击开始考试按钮后触发,也可以换为打开作答界面自动触发
        if (StringUtils.equals("btnok", key)) {
            String fields ="number.name";
            String courseId = this.getView().getFormShowParameter().getCustomParam("courseId");
            QFilter qFilter = new QFilter( "number", QCP.equals,courseId);
            DynamicObject course = BusinessDataServiceHelper.loadSingle( "lag1_course2",fields,new QFilter[]{qFilter});
            JSONArray jsonTaskArray = new JSONArray();
            JSONArray jsonTaskArray2 = new JSONArray();


            JSONObject jsonObjectSingle = new JSONObject();
            JSONObject jsonObjectSingle2 = new JSONObject();
//            jsonObjectSingle.put("courseContent", this.getModel().getValue("lag1_basedatafield2"));//课程名字
//**基础信息配置**
//- 课程名称：[用户输入课程名称]
//- 难度系数：[用户输入难度系数]
//- 题目总数：[用户输入数量]
//- 题型：简答题
//- 难度比例：
//• 难度不大于输入难度系数的题目数量：_70%
//• 难度大于输入难度系数的题目数量：_30%
//**待分配知识点配置
//**[输入知识点清单]：
//1. [知识点A]: [描述]
//2. [知识点B]: [描述]
//3. [知识点C]: [描述]
            jsonObjectSingle.put("courseContent", "思政");
            jsonObjectSingle.put("kpoint1",this.getModel().getValue("lag1_basedatafield"));
            jsonObjectSingle.put("kpoint2", this.getModel().getValue("lag1_basedatafield1"));
            jsonObjectSingle.put("kpoint3", this.getModel().getValue("lag1_basedatafield3"));

            jsonObjectSingle2.put("diff", this.getModel().getValue("lag1_stepperfield1"));//题目难度系数1-10
            jsonObjectSingle2.put("problemsCount", this.getModel().getValue("lag1_stepperfield"));//题目数量
//            jsonTaskArray.add(jsonObjectSingle);
//            jsonTaskArray2.add(jsonObjectSingle2);
//
//            JSONObject jsonResultObject = new JSONObject();
//            jsonResultObject.put("problemsIntroduction1", jsonTaskArray);
//            jsonResultObject.put("problemsIntroduction2", jsonTaskArray2);

            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("setting", jsonObjectSingle2.toJSONString());
            variableMap.put("knowpointJsonObject", jsonObjectSingle.toJSONString());
//            Map<String, String> params_z = new HashMap<>();
            //将无效字符进行处理
//            String jsonResult = params_z.get("jsonResult").replaceAll("\\s*|\r|\n|\t","");
//            JSONObject resultJsonObject = null;
//            try {
//                //若全部生成JSON字符串，则不会进入catch
//                resultJsonObject = JSON.parseObject(jsonResult);
//            } catch (Exception ee) {
//                //将"dayname"的上一个字符作为开始，以}]}字符作为结束，则最后需要+3
//                jsonResult = jsonResult.substring(jsonResult.indexOf("\"planName\"")-1 , jsonResult.indexOf("}]}")+3);
//                resultJsonObject = JSON.parseObject(jsonResult);
//            }
//            result.put("setting", CourseType);
//            result.put("knowpointJsonObject", resultJsonObject.toJSONString());

            //-----------------------调用任务流微服务----------------------------------------------------------------------
//            Map<String, String> variableMap = new HashMap<>();
            Object[] params = new Object[]{
                    //GPT提示编码
                    getProcessFid("process-250709B1A2338A"),
//                    dynamicObjectSubscribe.getString("ozwe_sub_content"),
//                    "用户输入的配置如下"
                    "整合输入的JSON为string",
                    variableMap
            };
            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiProcessService", "syncCall", params); // 固定写法
            JSONObject jsonObjectResult = new JSONObject(result); // 固定写法
            JSONObject jsonObjectData = jsonObjectResult.getJSONObject("data"); // 固定写法
            // 设置值
            String value = jsonObjectData.getString("subscribe_output");
            //处理value为cww可读格式

//---------------------------------------------------------------------------------------------
        }
//        }
    }

    // 获取GPT提示的Fid（固定函数）
    public long getProcessFid(String billNo) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("gai_process",
                "number," + "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        return (dynamicObject).getLong("id");
    }

}