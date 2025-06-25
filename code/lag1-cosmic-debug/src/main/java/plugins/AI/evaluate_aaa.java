package plugins.AI;

import com.alibaba.fastjson.JSONArray;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.plugin.AbstractFormPlugin;
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
 * 动态表单插件
 */
public class evaluate_aaa extends AbstractFormPlugin implements Plugin {
    @Override
    public void registerListener(EventObject e) {
        //注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }

    public void itemClick(ItemClickEvent e) {
        super.itemClick(e);
        if (e.getItemKey().equalsIgnoreCase("lag1_evaluate")) {
            //获取日任务信息，并且以JSON字符串的形式展现
            JSONObject jsonResultObject = new JSONObject();
            jsonResultObject.put("taskName", this.getModel().getValue("name").toString());
            jsonResultObject.put("createTime", this.getModel().getValue("createtime").toString());
            DynamicObjectCollection dynamicObjectCollection = this.getModel().getEntryEntity("lag1_entryentity_daytask");
            JSONArray jsonTaskArray = new JSONArray();
            for (DynamicObject dynamicObjectSingle : dynamicObjectCollection) {
                JSONObject jsonObjectSingle = new JSONObject();
                jsonObjectSingle.put("taskContent", dynamicObjectSingle.getString("lag1_task_name"));
                jsonObjectSingle.put("expectTime", dynamicObjectSingle.getString("lag1_expect_minutes"));
                jsonObjectSingle.put("diff", dynamicObjectSingle.getString("lag1_diff"));
                jsonObjectSingle.put("finishTime", dynamicObjectSingle.getString("lag1_finish_minute"));
                jsonObjectSingle.put("finishSituation", dynamicObjectSingle.getString("lag1_finish"));
                jsonTaskArray.add(jsonObjectSingle);
            }
            jsonResultObject.put("taskIntroduction", jsonTaskArray);

            //调用AI开发平台微服务
            Map<String , String> variableMap = new HashMap<>();
            variableMap.put("taskResult", jsonResultObject.toJSONString());

            Object[] params = new Object[] {
                    //提示词
                    getPromptFid("prompt-2506188951459F"),
                    "",
                    variableMap
            };
            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall", params);
            JSONObject jsonObjectResult = new JSONObject(result);
            JSONObject jsonObjectData = jsonObjectResult.getJSONObject("data");
            //设置值
            this.getModel().setValue("lag1_evaluate_all", jsonObjectData.getString("llmValue"));
            Markdown mk = this.getView().getControl("lag1_markdownap");
            mk.setText(jsonObjectData.getString("llmValue"));
        }

    }

    //获取提示词的Fid
    public long getPromptFid(String billNo) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("gai_prompt",
                "number," +
                        "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        return dynamicObject.getLong("id");
    }
    @Override
    public void afterBindData(EventObject eventObject) {
        Markdown mk = this.getView().getControl("lag1_markdownap");
        mk.setText(this.getModel().getValue("lag1_evaluate_all").toString());
    }
}