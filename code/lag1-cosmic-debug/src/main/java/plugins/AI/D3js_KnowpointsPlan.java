package plugins.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.AbstractBillPlugIn;
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
public class D3js_KnowpointsPlan extends AbstractBasePlugIn implements Plugin {

    @Override
    public void registerListener(EventObject e) {
        // 注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }

    public void itemClick(ItemClickEvent e) {
        super.itemClick(e);
        Control source = (Control) e.getSource();
        if (e.getItemKey().equalsIgnoreCase("lag1_d3jsplan_creat")) {

            String llmValue2 = this.getModel().getValue("lag1_knowpoint1").toString();
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
            //操作单据体
            DynamicObjectCollection dynamicObjectCollection = dynamicObject.getDynamicObjectCollection("lag1_knp");
            for (Object object : resultJsonObject.getJSONArray("dayTaskList")) {
                JSONObject jsonObjectSingle = (JSONObject) object;
                DynamicObject dynamicObjectEntry = dynamicObjectCollection.addNew();
                dynamicObjectEntry.set("lag1_knpid", jsonObjectSingle.getString("knpId"));
                dynamicObjectEntry.set("lag1_knowpname", jsonObjectSingle.getString("knowpName"));
                dynamicObjectEntry.set("lag1_knowpointparent", jsonObjectSingle.getString("knowpointParent"));
                dynamicObjectEntry.set("lag1_knowp_expand", jsonObjectSingle.getString("knowpExpand"));
                dynamicObjectEntry.set("lag1_chap", jsonObjectSingle.getString("chap"));
                dynamicObjectEntry.set("lag1_description", jsonObjectSingle.getString("description"));
            }
            SaveServiceHelper.saveOperate("lag1_d3js_knowpoints", new DynamicObject[] {dynamicObject}, null);
        }
    }
}