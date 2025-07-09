package plugins.AI;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.ext.form.control.Markdown;
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
public class HomeworkPigai extends AbstractBasePlugIn implements Plugin {
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
            DynamicObjectCollection dynamicObjectCollection = this.getModel().getEntryEntity("lag1_entryentity_problms");
            JSONArray jsonTaskArray = new JSONArray();
            for (DynamicObject dynamicObjectSingle : dynamicObjectCollection) {
                JSONObject jsonObjectSingle = new JSONObject();
                jsonObjectSingle.put("problemContent", dynamicObjectSingle.getString("lag1_prodes"));//题干
                jsonObjectSingle.put("userAnswer", dynamicObjectSingle.getString("lag1_useranswer"));//用户作答
                jsonObjectSingle.put("diff", dynamicObjectSingle.getString("lag1_difficulty"));//题目难度
                jsonObjectSingle.put("answer", dynamicObjectSingle.getString("lag1_standard_answer"));//标准答案
                jsonObjectSingle.put("kPoints", dynamicObjectSingle.getString("lag1_link_kpoints"));//相关知识点
                jsonTaskArray.add(jsonObjectSingle);
            }
            jsonResultObject.put("taskIntroduction", jsonTaskArray);

            //调用AI开发平台微服务
            Map<String , String> variableMap = new HashMap<>();
            variableMap.put("taskResult", jsonResultObject.toJSONString());

            Object[] params = new Object[] {
                    //提示词
                    getPromptFid("prompt-2507094056E37A"),
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
                "number," + "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        return dynamicObject.getLong("id");
    }
    @Override
    public void afterBindData(EventObject eventObject) {
        Markdown mk = this.getView().getControl("lag1_markdownap");
        mk.setText(this.getModel().getValue("lag1_evaluate_all").toString());
    }
}