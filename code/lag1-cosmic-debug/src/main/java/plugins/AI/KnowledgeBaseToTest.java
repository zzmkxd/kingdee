package plugins.AI;

import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDException;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.ObjectUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态表单插件
 */
public class KnowledgeBaseToTest extends AbstractFormPlugin implements Plugin {

    @Override
    public void registerListener(EventObject e) {
        //注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }
//    lag1_basedatafield2课程名字
//    lag1_basedatafield lag1_basedatafield1 lag1_basedatafield3三个知识点
//    lag1_stepperfield题目数量 lag1_stepperfield1题目难度系数1-10
    public void click(RequestContext requestContext, Map<String, Object> map) throws KDException {
        DynamicObject dynamicObjectSubscribe = BusinessDataServiceHelper.loadSingle("ozwe_subscribe", new QFilter[]{new QFilter("creator.id", QCP.equals, requestContext.getCurrUserId())});
        if (ObjectUtils.isNotEmpty(dynamicObjectSubscribe)) {
            Map<String, String> variableMap = new HashMap<>();
            Object[] params = new Object[]{
                    //GPT提示编码
                    getProcessFid("process-2503165440A8BC"),
                    dynamicObjectSubscribe.getString("ozwe_sub_content"),
                    variableMap
            };
            Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiProcessService", "syncCall", params); // 固定写法
            JSONObject jsonObjectResult = new JSONObject(result); // 固定写法
            JSONObject jsonObjectData = jsonObjectResult.getJSONObject("data"); // 固定写法
            // 设置值
            String value = jsonObjectData.getString("subscribe_output");
        }
    }

    // 获取GPT提示的Fid（固定函数）
    public long getProcessFid(String billNo) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("gai_process",
                "number," + "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        return (dynamicObject).getLong("id");
    }

}