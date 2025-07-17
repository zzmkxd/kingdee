package plugins.knowpoint;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
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
public class Kpointsinput_D3jsKpointsPlan extends AbstractBasePlugIn implements Plugin {
    private String damn;
    @Override
    public void registerListener(EventObject e) {
        // 注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }
    DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("customRegion");

    public void itemClick(ItemClickEvent e) {
        super.itemClick(e);
        Control source = (Control) e.getSource();
        if (e.getItemKey().equalsIgnoreCase("lag1_d3jsplan_creat")) {
            String strdamn = cache.get("damn");
            String jsonResult = strdamn.replaceAll("\\s*|\r|\n|\t", "");
            JSONObject resultJsonObject = null;
            try {
                //若全部生成JSON字符串，则不会进入catch
                resultJsonObject = JSON.parseObject(jsonResult);
            } catch (Exception ee) {
                //将"knowpoint_plan"的上一个字符作为开始，以}]}字符作为结束，则最后需要+3
                jsonResult = jsonResult.substring(jsonResult.indexOf("\"knowpoint_plan\"")-1 , jsonResult.indexOf("}]}")+3);
                resultJsonObject = JSON.parseObject(jsonResult);
            }
            this.getView().showMessage(jsonResult);

            //new一个DynamicObject表单对象
            DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject("lag1_d3js_knowpoints");
            StringBuilder sb1 = new StringBuilder();
            for (int i = 1; i <= 10; i++) {
                int ascii = 48 + (int) (Math.random() * 9);
                char c = (char) ascii;
                sb1.append(c);
            }
            //设置对应属性
            dynamicObject.set("number", sb1.toString());
            dynamicObject.set("name", resultJsonObject.getString("knowpoint_plan"));
            dynamicObject.set("status", "C");
            dynamicObject.set("enable", 1);
            dynamicObject.set("creator", RequestContext.get().getCurrUserId());
            //操作表单
            DynamicObjectCollection dynamicObjectCollection = dynamicObject.getDynamicObjectCollection("lag1_knp");
            for (Object object : resultJsonObject.getJSONArray("knowledgePoints")) {
                JSONObject jsonObjectSingle = (JSONObject) object;
                DynamicObject dynamicObjectEntry = dynamicObjectCollection.addNew();
                dynamicObjectEntry.set("lag1_knpid", jsonObjectSingle.getString("knpId"));
                dynamicObjectEntry.set("lag1_knowpname", jsonObjectSingle.getString("knowpName"));
                dynamicObjectEntry.set("lag1_knowpointparent", jsonObjectSingle.getString("knowpointParent"));
                dynamicObjectEntry.set("lag1_expand", jsonObjectSingle.getString("knowpExpand"));
                dynamicObjectEntry.set("lag1_chap", jsonObjectSingle.getString("chap"));
                dynamicObjectEntry.set("lag1_description", jsonObjectSingle.getString("description"));
            }
            SaveServiceHelper.saveOperate("lag1_d3js_knowpoints", new DynamicObject[] {dynamicObject}, null);
        }
//-------------------------------------------------------------------------------------------------------------------------------------

        if (e.getItemKey().equalsIgnoreCase("lag1_kpoint_new")) {
//            String str=this.getModel().getValue("lag1_knowpoint1").toString();
            String yourString = cache.get("yourValName");
            // 调用GPT开发平台微服务
            Map<String, String> variableMap = new HashMap<>();
//            variableMap.put("knowpointinfos", yourString);
            Object[] params = new Object[]{
                    //GPT提示编码
                    getPromptFid("prompt-250630BAA79177"),
                    yourString,
                    variableMap
            };
            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall", params);

            JSONObject jsonObjectResult2 = new JSONObject(result);
            JSONObject jsonObjectData2 = jsonObjectResult2.getJSONObject("data");//微服务的输出，即代填入单据体的知识点JSON及正常微服务输出的各个键值对

            this.getView().showMessage(jsonObjectData2.getString("llmValue"));

            String llmValue2 = jsonObjectData2.getString("llmValue");//代填入单据体的知识点JSON
//            damn=llmValue2;
            cache.put("damn", llmValue2);
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
                    //依据所属的基础资料获取
                    dynamicObject.set("lag1_courseid", this.getModel().getValue("lag1_course"));

                    dynamicObject.set("lag1_knowpointplan", this.getModel().getValue("name"));
                    dynamicObject.set("lag1_knowpointparent", jsonObjectSingle.getString("knowpointParent"));
                    dynamicObject.set("lag1_knowp_expand", jsonObjectSingle.getString("knowpExpand"));
                    dynamicObject.set("lag1_description", jsonObjectSingle.getString("description"));
                    dynamicObject.set("lag1_chap", jsonObjectSingle.getString("chap"));
                    // 保存新的基础资料记录
                    SaveServiceHelper.saveOperate("lag1_knowpoints", new DynamicObject[]{dynamicObject}, null);

                }
            }else this.getView().showMessage("毁了");

//-------------------------------------------------------------------------------------------------------------------------------------
//修改已有的单据体，修改后// 更新单据
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