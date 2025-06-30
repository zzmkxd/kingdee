package plugins.AI;

import com.alibaba.fastjson.JSON;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.control.Control;
import kd.bos.servicehelper.operation.SaveServiceHelper;
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
public class buttom_Gpt_evaluate extends AbstractBillPlugIn implements Plugin {
    private static String globalLlmValue = "";

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

            for (int i = 1; i <= 12; i++) {
                jsonResultObject.put("lag1_chapter" + i , this.getModel().getValue("lag1_chapter" + i).toString());
            }//目前没有对小标题分析

            //测试微服务正常运行代码----
            //prompt 变量
//            Map<String, String> variableMap = new HashMap<>();
//            variableMap.put("user_input","深圳");
//            // long promptId =1L;
//            String input = "深圳明天天气怎么样";
//            Object[] params =  new Object[]{getPromptFid("prompt-2505280F653A39"),input,variableMap};
//            //调用
//            Map<String,Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall",params);
//            this.getView().showMessage(result.toString());
            //-----

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
            this.getModel().setValue("lag1_knowpoint1", jsonObjectData.getString("llmValue"));//返回数据中不包含 llmValue 字段
            globalLlmValue = jsonObjectData.getString("llmValue");
            Markdown mk = this.getView().getControl("lag1_md");
            mk.setText(jsonObjectData.getString("llmValue"));

//            if (globalLlmValue != null && !globalLlmValue.isEmpty()) {
//                // 设置第二个微服务按钮可见和启用
////                this.getView().setVisible(true, "lag1_pointbindcourse");
//                this.getView().setEnable(true, "lag1_pointbindcourse");
//            }
        }else if (e.getItemKey().equalsIgnoreCase("lag1_pointbindcourse")) {
            // 获取日任务信息，并且以JSON字符串的形式展现
            JSONObject jsonResultObject = new JSONObject();

            for (int i = 1; i <= 12; i++) {
                jsonResultObject.put("lag1_chapter" + i , this.getModel().getValue("lag1_chapter" + i).toString());
            }//目前没有对小标题分析
            jsonResultObject.put("details", globalLlmValue.toString());
            // 调用GPT开发平台微服务
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("detail", jsonResultObject.toJSONString());

            Object[] params = new Object[]{
                    //GPT提示编码
                    getPromptFid("prompt-250630BAA79177"),
                    "开始分析这些知识点",
                    variableMap
            };
            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall", params);

            JSONObject jsonObjectResult2 = new JSONObject(result);
            JSONObject jsonObjectData2 = jsonObjectResult2.getJSONObject("data");
                String llmValue2 = jsonObjectData2.getString("llmValue");
            // 设置值
                Map<String, String> params1 = new HashMap<>();
                variableMap.put("jsonResult", llmValue2);
                String jsonResult = params1.get("jsonResult").replaceAll("\\s*|\r|\n|\t","");
                JSONObject resultJsonObject = null;
                try {
                    //若全部生成JSON字符串，则不会进入catch
                    resultJsonObject = JSON.parseObject(jsonResult);
                } catch (Exception ee) {
                    //将"knowpoint_plan"的上一个字符作为开始，以}]}字符作为结束，则最后需要+3
                    jsonResult = jsonResult.substring(jsonResult.indexOf("\"knowpoint_plan\"")-1 , jsonResult.indexOf("}]}")+3);
                    resultJsonObject = JSON.parseObject(jsonResult);
                }
                //new一个DynamicObject表单对象
//
//                DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject("lag1_knowpoint");
//                StringBuilder sb1 = new StringBuilder();
//                for (int i = 1; i <= 10; i++) {
//                    int ascii = 48 + (int) (Math.random() * 9);
//                    char c = (char) ascii;
//                    sb1.append(c);
//                }
//                //设置对应属性
//                dynamicObject.set("number", sb1.toString());
//                dynamicObject.set("name", resultJsonObject.getString("knowpoint_plan"));
//                dynamicObject.set("status", "C");
//                dynamicObject.set("enable", 1);
//                dynamicObject.set("creator", RequestContext.get().getCurrUserId());
//                //操作单据体
//                DynamicObjectCollection dynamicObjectCollection = dynamicObject.getDynamicObjectCollection("lag1_knp");
//                for (Object object : resultJsonObject.getJSONArray("knowledgePoints")) {
//                    JSONObject jsonObjectSingle = (JSONObject) object;
//                    DynamicObject dynamicObjectEntry = dynamicObjectCollection.addNew();
//                    dynamicObjectEntry.set("lag1_knpid", jsonObjectSingle.getString("knpId"));//知识点ID
//                    dynamicObjectEntry.set("lag1_knowpname", jsonObjectSingle.getString("knowpName"));//知识点名称
//                    dynamicObjectEntry.set("lag1_knowpointparent", jsonObjectSingle.getString("knowpointParent"));//知识点父节点
//                    dynamicObjectEntry.set("lag1_knowp_expand", jsonObjectSingle.getString("knowpExpand"));//节点层次
//                    dynamicObjectEntry.set("lag1_chap", jsonObjectSingle.getString("chap"));//知识点所属章节
//                    dynamicObjectEntry.set("lag1_description", jsonObjectSingle.getString("description"));//知识点描述
//                }
//                SaveServiceHelper.saveOperate("lag1_knowpoint", new DynamicObject[] {dynamicObject}, null);
//


            // 首先尝试加载已存在的单据（假设您知道单据ID或可以通过某些条件查询）
            QFilter filter = new QFilter("number", QCP.equals, this.getModel().getValue("number").toString()); // 使用编号或其他唯一标识查询
            DynamicObject existingBill = BusinessDataServiceHelper.loadSingle("lag1_knowpoint", "id,number,name,status,enable,creator,lag1_knp", new QFilter[]{filter});

            if (existingBill != null) {
                // 修改已存在单据
                existingBill.set("name", resultJsonObject.getString("knowpoint_plan"));
                existingBill.set("status", "C");
                existingBill.set("enable", 1);

                // 获取并清空原有单据体
                DynamicObjectCollection dynamicObjectCollection = existingBill.getDynamicObjectCollection("lag1_knp");
                dynamicObjectCollection.clear();

                // 添加新的单据体数据
                for (Object object : resultJsonObject.getJSONArray("knowledgePoints")) {
                    JSONObject jsonObjectSingle = (JSONObject) object;
                    DynamicObject dynamicObjectEntry = dynamicObjectCollection.addNew();
                    dynamicObjectEntry.set("lag1_knpid", jsonObjectSingle.getString("knpId"));
                    dynamicObjectEntry.set("lag1_knowpname", jsonObjectSingle.getString("knowpName"));
                    dynamicObjectEntry.set("lag1_knowpointparent", jsonObjectSingle.getString("knowpointParent"));
                    dynamicObjectEntry.set("lag1_knowp_expand", jsonObjectSingle.getString("knowpExpand"));
                    dynamicObjectEntry.set("lag1_chap", jsonObjectSingle.getString("chap"));
                    dynamicObjectEntry.set("lag1_description", jsonObjectSingle.getString("description"));
                }

                // 更新单据
                SaveServiceHelper.update(new DynamicObject[]{existingBill});
            } else {
                // 不存在则新建单据（保留您原来的新建逻辑）
                DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject("lag1_knowpoint");
                StringBuilder sb1 = new StringBuilder();
                for (int i = 1; i <= 10; i++) {
                    int ascii = 48 + (int) (Math.random() * 9);
                    char c = (char) ascii;
                    sb1.append(c);
                }
                dynamicObject.set("number", sb1.toString());
                dynamicObject.set("name", resultJsonObject.getString("knowpoint_plan"));
                dynamicObject.set("status", "C");
                dynamicObject.set("enable", 1);
                dynamicObject.set("creator", RequestContext.get().getCurrUserId());

                DynamicObjectCollection dynamicObjectCollection = dynamicObject.getDynamicObjectCollection("lag1_knp");
                for (Object object : resultJsonObject.getJSONArray("knowledgePoints")) {
                    JSONObject jsonObjectSingle = (JSONObject) object;
                    DynamicObject dynamicObjectEntry = dynamicObjectCollection.addNew();
                    dynamicObjectEntry.set("lag1_knpid", jsonObjectSingle.getString("knpId"));
                    dynamicObjectEntry.set("lag1_knowpname", jsonObjectSingle.getString("knowpName"));
                    dynamicObjectEntry.set("lag1_knowpointparent", jsonObjectSingle.getString("knowpointParent"));
                    dynamicObjectEntry.set("lag1_knowp_expand", jsonObjectSingle.getString("knowpExpand"));
                    dynamicObjectEntry.set("lag1_chap", jsonObjectSingle.getString("chap"));
                    dynamicObjectEntry.set("lag1_description", jsonObjectSingle.getString("description"));
                }
                SaveServiceHelper.saveOperate("lag1_knowpoint", new DynamicObject[]{dynamicObject}, null);
            }

//            }
//            return result;
        }
    }

    //        @Override
//    public Map<String, String> invokeAction(String action, Map<String, String> params) {
//
//    }
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