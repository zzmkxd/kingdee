package plugins.MQ;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mq.MessageAcker;
import kd.bos.mq.MessageConsumer;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

public class JirSuanMqConsumer implements MessageConsumer {
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
//            QFilter qFilter = new QFilter("number", QCP.equals, "ProList002");
//            DynamicObject obj = BusinessDataServiceHelper.loadSingle("lag1_homework_correct", new QFilter[]{qFilter});
//            obj.set("name", (String) message);
//            SaveServiceHelper.update(obj);
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