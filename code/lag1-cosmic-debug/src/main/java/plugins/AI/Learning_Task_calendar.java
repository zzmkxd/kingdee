package plugins.AI;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.ext.form.control.Markdown;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础资料插件
 */
public class Learning_Task_calendar extends AbstractBasePlugIn implements Plugin {
//输入有：学生对知识点方案的平均正确率+该知识点的做题数量+想提升的模式（基础/中等+困难）+ 想要掌握的知识点列表
@Override
public void registerListener(EventObject e) {
    // 注册点击事件
    super.registerListener(e);
    this.addItemClickListeners("tbmain");
}

    public void itemClick(ItemClickEvent e) {
//        this.getView().setEnable(false, "lag1_pointbindcourse");
        super.itemClick(e);
        Control source = (Control) e.getSource();
        //改
        if (e.getItemKey().equalsIgnoreCase("lag1_button_knowpoint")) {
//            else {
//                 设置第二个微服务按钮不可见和禁用
//                this.getView().setVisible(false, "lag1_pointbindcourse");
//            }
            // 获取日任务信息，并且以JSON字符串的形式展现
            JSONObject jsonResultObject = new JSONObject();
            for (int i = 1; i <= 3; i++) {
                jsonResultObject.put("lag1_chapter" + i , this.getModel().getValue("lag1_chapter" + i).toString());

            }//目前没有对小标题分析
            //----下面是正常提取教材代码----
            // 调用GPT开发平台微服务
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("courseinfo", jsonResultObject.toJSONString());

            Object[] params = new Object[]{
                    //GPT提示编码
                    getPromptFid("prompt-2505280F653A39"),//改
                    "开始输出学习计划方案",
                    variableMap
            };
            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall", params);

            JSONObject jsonObjectResult = new JSONObject(result);
            JSONObject jsonObjectData = jsonObjectResult.getJSONObject("data");
            // 设置值

            //改
            this.getModel().setValue("lag1_knowpoint1", jsonObjectData.getString("llmValue"));//返回数据中的 llmValue 字段
            Markdown mk = this.getView().getControl("lag1_md");
            mk.setText(jsonObjectData.getString("llmValue"));

//            if (globalLlmValue != null && !globalLlmValue.isEmpty()) {
//                // 设置第二个微服务按钮可见和启用
////                this.getView().setVisible(true, "lag1_pointbindcourse");
//                this.getView().setEnable(true, "lag1_pointbindcourse");
//            }
        }

    }

    // 获取GPT提示的Fid
    public long getPromptFid(String billNo) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("gai_prompt",
                "number," + "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        return (dynamicObject).getLong("id");
    }

    @Override
    public void afterBindData(EventObject eventObject) {
        //改
        Markdown mk = this.getView().getControl("lag1_md");
        mk.setText(this.getModel().getValue("lag1_knowpoint1").toString());
    }
}