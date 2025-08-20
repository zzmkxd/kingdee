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
    private static final String ENTRY_ENTITY_COLLECTION = "lag1_knp";
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

// 获取当前表单的数据包（主实体）
            DynamicObject dynamicObject = this.getModel().getDataEntity(true);

//            DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject("lag1_d3js_knowpoints");
            StringBuilder sb1 = new StringBuilder();
            for (int i = 1; i <= 10; i++) {
                int ascii = 48 + (int) (Math.random() * 9);
                char c = (char) ascii;
                sb1.append(c);
            }
            //设置对应属性
            this.getModel().setValue("number", sb1.toString());
            DynamicObject basedataObj = (DynamicObject)this.getModel().getValue("lag1_course");// 获取基础资料字段的数据包
            if (basedataObj != null) {
                String refPropertyValue = basedataObj.getString("name");// 获取引用属性值
                this.getModel().setValue("name", refPropertyValue+"知识点衔接方案");
            }
            this.getModel().setValue("status", "Approved");
            this.getModel().setValue("enable", 1);
//            this.getModel().setValue("creator", RequestContext.get().getCurrUserId());
            //操作表单
            DynamicObjectCollection dynamicObjectCollection = this.getModel().getEntryEntity(ENTRY_ENTITY_COLLECTION);
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
//            SaveServiceHelper.saveOperate("lag1_d3js_knowpoints", new DynamicObject[] {dynamicObject}, null);
            this.getView().updateView();
        }
//-------------------------------------------------------------------------------------------------------------------------------------

        if (e.getItemKey().equalsIgnoreCase("lag1_kpoint_new")) {
            String jsonKnp = cache.get("damn");
            Map<String, String> params1 = new HashMap<>();
            params1.put("jsonResult", jsonKnp);
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

            if (resultJsonObject != null) {
                this.getView().showMessage("正常");
                for (Object object : resultJsonObject.getJSONArray("knowledgePoints")) {
                    JSONObject jsonObjectSingle = (JSONObject) object;
                    if(validateDotCount(jsonObjectSingle.getString("knid")) && (jsonObjectSingle.getString("description")== "")){
                        continue;
                    }
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
        }
    }


    public static boolean validateDotCount(String input) {
        // 1. 验证整体格式是否符合数字.数字.数字的模式
        if (!input.matches("^\\d+(\\.\\d+)*$")) {
            return false;
        }

        // 2. 计算点的数量
        int dotCount = input.length() - input.replace(".", "").length();

        // 3. 判断点数量是否<=2
        return dotCount <= 2;
    }
}