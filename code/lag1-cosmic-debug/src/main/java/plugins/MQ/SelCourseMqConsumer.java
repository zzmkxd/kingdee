package plugins.MQ;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mq.MessageAcker;
import kd.bos.mq.MessageConsumer;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * @说明 TODO
 * @Author Tian_Yumi
 * @Date 2025/8/22 00:55
 * @Version 1.0
 */

public class SelCourseMqConsumer implements MessageConsumer {
    Log log = LogFactory.getLog(getClass());
    @Override
    public void onMessage(Object message, String messageId, boolean resend, MessageAcker acker) {
        log.info("JirSuanMqConsumer开始起飞");
        try {
            DynamicObject obj = BusinessDataServiceHelper.newDynamicObject("lag1_homework_correct");
            //设置对应属性
            obj.set("number", "ProList002");
            obj.set("creator", RequestContext.get().getCurrUserId());
            obj.set("status", "C");
            obj.set("enable", 1);
            obj.set("name", "Mq考试test");
            obj.set("status", "C");
            obj.set("lag1_textfield1", ((int) message)%11);
            SaveServiceHelper.saveOperate("lag1_homework_correct", new DynamicObject[]{obj}, null);
        } catch (Throwable e) {
            boolean discard = false;
            if (discard){
                acker.discard(messageId);
            } else{
                acker.deny(messageId);
            }
        }
    }
}
