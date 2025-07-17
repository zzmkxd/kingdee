package plugins.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDException;
import kd.bos.ext.form.control.CountDown;
import kd.bos.form.control.Button;
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
    private static final String SAVE_BUTTON_KEY = "btnok";
    @Override
    public void registerListener(EventObject e) {
        //注册点击事件
        super.registerListener(e);
        Button button = this.getView().getControl("btnok");
        button.addClickListener(this);
    }
    @Override
    public void click(EventObject e) {
        Control button = (Control) e.getSource();
        if (SAVE_BUTTON_KEY.equals(button.getKey())) {
//这里是考试开始的逻辑，最好替换成动态表单弹窗-点击开始考试按钮后触发,也可以换为打开作答界面自动触发
            JSONObject jsonObjectSingle = new JSONObject();
//            jsonObjectSingle.put("courseContent", this.getModel().getValue("lag1_basedatafield2"));//课程名字
            jsonObjectSingle.put("courseContent", "思政");
            jsonObjectSingle.put("kpoint",this.getModel().getValue("lag1_linkknp.lag1_description"));
//            jsonObjectSingle.put("kpoint2", this.getModel().getValue("lag1_linkknp2.lag1_description"));
//            jsonObjectSingle.put("kpoint3", this.getModel().getValue("lag1_linkknp3.lag1_description"));
            jsonObjectSingle.put("diff", this.getModel().getValue("lag1_stepperfield1"));//题目难度系数1-10
            jsonObjectSingle.put("problemsCount", this.getModel().getValue("lag1_stepperfield"));//题目数量
            String str = JSON.toJSONString(jsonObjectSingle);
            this.getView().showMessage(str);
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
//            可输出调试
//            Map<String, String> variableMap = new HashMap<>();
////-----------------------调用任务流微服务----------------------------------------------------------------------
//            Object[] params = new Object[]{
//                    //GPT提示编码
//                    getProcessFid("process-250709B1A2338A"),
//                    jsonObjectSingle.toJSONString(),
//                    variableMap
//            };
//            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiProcessService", "syncCall", params); // 固定写法
//            JSONObject jsonObjectResult = new JSONObject(result); // 固定写法
//            JSONObject jsonObjectData = jsonObjectResult.getJSONObject("data"); // 固定写法
//            // 设置值
//            String value = String.valueOf(jsonObjectData.getJSONArray("questions"));
//            this.getView().showMessage(value);
            //处理value为cww可读格式
//---------------------------------------------------------------------------------------------
        }
//    lag1_basedatafield2课程名字 btnok提交
//    lag1_basedatafield lag1_basedatafield1 lag1_basedatafield3 三个知识
//    lag1_stepperfield 题目数量 lag1_stepperfield1 题目难度系数1-10
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