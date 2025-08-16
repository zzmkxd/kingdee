package plugins.MQ;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mq.MQFactory;
import kd.bos.mq.MessagePublisher;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

public class MulThreadsEdit extends AbstractBasePlugIn implements Plugin {
    @Override
    public void registerListener(EventObject e) {
        // 注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("toolbarap");
    }
    Log Logger = LogFactory.getLog(MulThreadsEdit.class);
    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if ("lag1_concurrent".equals(itemKey)) {
            // 普通处理
            for (int i = 1; i < 1000; i++) {
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
        } else if ("lag1_mq_concurrent".equals(itemKey)) {
            // 使用MQ处理
            MessagePublisher mp = MQFactory.get().createSimplePublisher("lag1_damn", "erkai_queue");
            for (int i = 1; i < 1000; i++) {
                mp.publish(i);
            }
        }
    }
}