package plugins.MQ;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.mq.MQFactory;
import kd.bos.mq.MessagePublisher;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

public class BizCodeThread implements Runnable {
    private boolean useMQ;

    public BizCodeThread(boolean useMQ) {
        this.useMQ = useMQ;
    }

    @Override
    public void run() {
        if (useMQ) {
            // 使用MQ处理
            MessagePublisher mp = MQFactory.get().createSimplePublisher("lag1_learn", "erkai_queue");
            for (int i = 1; i < 50; i++) {
                mp.publish(i);
            }
        } else {
            // 普通处理
            for (int i = 1; i < 50; i++) {
                DynamicObject obj = BusinessDataServiceHelper.newDynamicObject("lag1_homework_correct");
                //设置对应属性
                obj.set("number", "ProList002");
                obj.set("creator", RequestContext.get().getCurrUserId());
                obj.set("status", "C");
                obj.set("enable", 1);
                obj.set("name", "考试test");
                obj.set("status", "C");
                obj.set("lag1_textfield1", i%11);
                SaveServiceHelper.saveOperate("lag1_homework_correct", new DynamicObject[]{obj}, null);
            }
        }
    }
}