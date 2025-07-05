package plugins.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.ext.form.control.Markdown;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础资料插件
 */
public class KnowpointForm_input extends AbstractBasePlugIn implements Plugin {

    @Override
    public void registerListener(EventObject e) {
        // 注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }

    public void itemClick(ItemClickEvent e) {
        super.itemClick(e);
        Control source = (Control) e.getSource();
        if (e.getItemKey().equalsIgnoreCase("lag1_pointbindcourse")) {
            String str=this.getModel().getValue("lag1_knowpoint1").toString().replaceAll("\\s*|\r|\n|\t","");
            JSONObject jsonResultObject = JSON.parseObject(str); // 直接解析字符串
            // 调用GPT开发平台微服务
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("knowpointinfos", jsonResultObject.toJSONString());
            Object[] params = new Object[]{
                    //GPT提示编码
                    getPromptFid("prompt-250630BAA79177"),
                    "开始分析这些知识点",
                    variableMap
            };
            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall", params);

            JSONObject jsonObjectResult2 = new JSONObject(result);
            JSONObject jsonObjectData2 = jsonObjectResult2.getJSONObject("data");//微服务的输出，即代填入单据体的知识点JSON及正常微服务输出的各个键值对

            this.getView().showMessage(jsonObjectData2.getString("llmValue"));
            String llmValue2 = jsonObjectData2.getString("llmValue");//代填入单据体的知识点JSON
            this.getModel().setValue("lag1_knowpoint1", llmValue2);//返回数据中的 llmValue 字段
            Map<String, String> params1 = new HashMap<>();
            params1.put("jsonResult", llmValue2);
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

//-------------------------------------------------------------------------------------------------------------------------------------
// 修改已有的表单，修改如下：
// 首先尝试加载已存在的基础资料（假设您知道基础资料的唯一标识）
//            QFilter filter = new QFilter("name", QCP.equals, this.getModel().getValue("name").toString()); // 使用名称或其他唯一标识查询
//            DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("lag1_knowpoints", new QFilter[]{filter});
// 新建多个知识点
//            JSONArray knowledgePoints = resultJsonObject.getJSONArray("knowledgePoints");
            if (resultJsonObject != null) {
                this.getView().showMessage("正常");
                for (Object object : resultJsonObject.getJSONArray("knowledgePoints")) {
                    JSONObject jsonObjectSingle = (JSONObject) object;
                    DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject("lag1_knowpoints");

                    StringBuilder sb1 = new StringBuilder();
                    for (int i = 1; i <= 10; i++) {
                        int ascii = 48 + (int) (Math.random() * 9);
                        char c = (char) ascii;
                        sb1.append(c);
                    }
                    //设置对应属性
                    dynamicObject.set("number", sb1.toString());
                    dynamicObject.set("lag1_integerfield", jsonObjectSingle.getString("knpId"));
                    dynamicObject.set("creator", RequestContext.get().getCurrUserId());
                    dynamicObject.set("status", "C");
                    dynamicObject.set("enable", 1);
                    dynamicObject.set("name", jsonObjectSingle.getString("knowpName"));
                    dynamicObject.set("lag1_knowpointplan", jsonObjectSingle.getString("knowpoint_plan"));
                    dynamicObject.set("lag1_knowpointparent", jsonObjectSingle.getString("knowpointParent"));
                    dynamicObject.set("lag1_knowp_expand", jsonObjectSingle.getString("knowpExpand"));
    //                 创建新的基础资料记录
    //                DynamicObject newBase = BusinessDataServiceHelper.newDynamicObject("lag1_knowpoints");
                    dynamicObject.set("lag1_description", jsonObjectSingle.getString("description"));
                    dynamicObject.set("lag1_chap", jsonObjectSingle.getString("chap"));
                    // 保存新的基础资料记录
                    SaveServiceHelper.saveOperate("lag1_knowpoints", new DynamicObject[]{dynamicObject}, null);

                }
            }else this.getView().showMessage("毁了");

//-------------------------------------------------------------------------------------------------------------------------------------
            //修改已有的单据体，修改后
//            IDataModel formDataModel2 = this.getModel();
//            DynamicObject formDataEntity2 = formDataModel2.getDataEntity();// 直接使用当前表单的实体对象（formDataEntity）操作数据
//            Long currentFormId = (Long) formDataEntity2.getPkValue(); // 获取当前表单主键（可选）
//            String billNo = (String) formDataEntity2.get("FBillNo");  // 直接获取当前表单字段值
//            DynamicObject materialObj = (DynamicObject) formDataEntity2.get("FMaterialId");
//            String materialNumber = materialObj.get("FNumber").toString(); // 获取物料编码
//            DynamicObject postSingle = BusinessDataServiceHelper.loadSingle("lag1_knowpoint", new QFilter[]{new QFilter("id", QCP.equals,currentFormId)});

//            // 首先尝试加载已存在的单据（假设您知道单据ID或可以通过某些条件查询）
//            QFilter filter = new QFilter("number", QCP.equals, this.getModel().getValue("number").toString()); // 使用编号或其他唯一标识查询
//            DynamicObject existingBill = BusinessDataServiceHelper.loadSingle("lag1_knowpoint", new QFilter[]{filter});
//
//            if (existingBill != null) {
//                // 修改已存在单据
//                existingBill.set("name", resultJsonObject.getString("knowpoint_plan"));
////                existingBill.set("status", "C");
////                existingBill.set("enable", 1);
//
//                // 获取并清空原有单据体
//                DynamicObjectCollection dynamicObjectCollection = existingBill.getDynamicObjectCollection("lag1_knp");
////                dynamicObjectCollection.clear();
//
//                // 添加新的单据体数据
//                for (Object object : resultJsonObject.getJSONArray("knowledgePoints")) {
//                    JSONObject jsonObjectSingle = (JSONObject) object;
//                    DynamicObject dynamicObjectEntry = dynamicObjectCollection.addNew();
//                    dynamicObjectEntry.set("lag1_knpid", jsonObjectSingle.getString("knpId"));
//                    dynamicObjectEntry.set("lag1_knowpname", jsonObjectSingle.getString("knowpName"));
//                    dynamicObjectEntry.set("lag1_knowpointparent", jsonObjectSingle.getString("knowpointParent"));
//                    dynamicObjectEntry.set("lag1_knowp_expand", jsonObjectSingle.getString("knowpExpand"));
//                    dynamicObjectEntry.set("lag1_chap", jsonObjectSingle.getString("chap"));
//                    dynamicObjectEntry.set("lag1_description", jsonObjectSingle.getString("description"));
//                }
////                SaveServiceHelper.saveOperate("lag1_knowpoint", new DynamicObject[] {existingBill}, null);
//
//                // 更新单据
//                SaveServiceHelper.update(new DynamicObject[]{existingBill});
//            }
//-------------------------------------------------------------------------------------------------------------------------------------
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