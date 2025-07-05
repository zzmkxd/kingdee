package plugins.AI;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.form.control.Control;
import kd.sdk.plugin.Plugin;
import com.alibaba.fastjson.JSONObject;
import kd.bos.ext.form.control.Markdown;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.dataentity.entity.DynamicObject;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
/**
 * 单据界面插件
 */
public class KpointsAnalysis extends AbstractBillPlugIn implements Plugin {
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
                    getPromptFid("prompt-2505280F653A39"),
                    "开始分析这本教材",
                    variableMap
            };
            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall", params);

            JSONObject jsonObjectResult = new JSONObject(result);
            JSONObject jsonObjectData = jsonObjectResult.getJSONObject("data");
            // 设置值
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
        Markdown mk = this.getView().getControl("lag1_md");
        mk.setText(this.getModel().getValue("lag1_knowpoint1").toString());
    }
}