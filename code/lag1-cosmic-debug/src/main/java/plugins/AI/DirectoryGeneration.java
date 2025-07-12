package plugins.AI;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
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
public class DirectoryGeneration extends AbstractBasePlugIn implements Plugin {
    @Override
    public void afterBindData(EventObject e) {
      super.afterBindData(e);
        JSONObject jsonResultObject = new JSONObject();
        for (int i = 1; i <= 8; i++) {
            jsonResultObject.put("lag1_chapter" + i , this.getModel().getValue("lag1_chapter" + i).toString());
            for(int j=1;j<=5;j++){
                jsonResultObject.put("lag1_chapter" + i+"p"+j, this.getModel().getValue("lag1_chapter" + i+"p"+j).toString());
            }

        }//目前没有对小标题分析
        //----下面是正常提取教材代码----
        // 调用GPT开发平台微服务
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("courseinfo", jsonResultObject.toJSONString());
      Object[] params = new Object[]{
              //GPT提示编码
              getPromptFid("prompt-250708D5B99E7D"),
              "开始分析这些知识点",
              variableMap
      };
      Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall", params);

      JSONObject jsonObjectResult2 = new JSONObject(result);
      JSONObject jsonObjectData2 = jsonObjectResult2.getJSONObject("data");//微服务的输出，即代填入单据体的知识点JSON及正常微服务输出的各个键值对

      this.getView().showMessage(jsonObjectData2.getString("llmValue"));

      String llmValue2 = jsonObjectData2.getString("llmValue");

    }
    // 获取GPT提示的Fid
    public long getPromptFid(String billNo) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("gai_prompt",
                "number," + "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        return (dynamicObject).getLong("id");
    }
}