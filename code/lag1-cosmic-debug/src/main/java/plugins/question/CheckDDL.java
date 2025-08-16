package plugins.question;

import com.alibaba.fastjson.JSONObject;
import kd.bos.orm.query.QCP;
import kd.bos.schedule.executor.AbstractTask;
import com.alibaba.druid.util.StringUtils;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.exception.KDException;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.workflow.engine.msg.info.MessageInfo;
import org.apache.commons.lang3.ObjectUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 后台任务插件
 */
public class CheckDDL extends AbstractTask{
    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
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
            MessageInfo message = new MessageInfo();
            message.setType(MessageInfo.TYPE_MESSAGE);
            message.setTitle("校园咨询智能助手订阅来信");
            ArrayList<Long> receivers = new ArrayList<Long>();
            receivers.add(Long.parseLong(dynamicObjectSubscribe.getDynamicObject("creator").getString("id")));
            message.setUserIds(receivers);// 接收人
            message.setSenderName("校园咨询智能助手");// 发送人
            message.setSenderId(requestContext.getCurrUserId());
            message.setTag("重要");// 业务标签
            message.setPubaccNumber("flowassist");
            message.setContent(value);// 内容
            message.setEntityNumber("ozwe_subscribe");
            message.setOperation("save");
            message.setBizDataId(requestContext.getCurrUserId());
            message.setPubaccNumber("flowassist");
            List<MessageInfo> resultMessageList = new ArrayList<>();
            resultMessageList.add(message);
            Map<String, Object> msgMap = MessageCenterServiceHelper.batchSendMessages(resultMessageList);
            System.out.println("消息发送情况："+msgMap);
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