package plugins.MQ;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.events.ItemClickEvent;
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

/**
 * @说明 TODO
 * @Author Tian_Yumi
 * @Date 2025/8/22 00:56
 * @Version 1.0
 */

public class SecMulThreadsEdit extends AbstractBasePlugIn implements Plugin {
    private static final String TK_USERDATA = "lag1_user_data";
    private static final String TK_KNPOINT = "lag1_knowpoints";
    private static final String TD_UDCODE = "number";
    private static final String TD_UDCOURSEID = "name";
    private static final String TD_UDUSERID = "creator";
    private static final String TD_UDKNPOINT = "lag1_linkkp";
    private static final String TD_UDANSNUM = "lag1_ans_num";
    private static final String TD_UDSUNSCORE = "lag1_sum_score";
    private static final String TD_UDdata = "lag1_data";
    private static final String TD_UDenable = "enbale";
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
            for (int i = 1; i < 2; i++) {
                initNormalUserData();
            }
        } else if ("lag1_mq_concurrent".equals(itemKey)) {
            // 使用MQ处理
            this.getView().showMessage("进入循环");
            MessagePublisher mp = MQFactory.get().createSimplePublisher("lag1_learn", "erkai_queue2");
            for (int i = 1; i < 2; i++) {
                mp.publish(i);
            }
        }
    }
    private void initNormalUserData( ) {
        DynamicObject kp = BusinessDataServiceHelper.loadSingle(TK_KNPOINT, "id,name", new QFilter[]{new QFilter("name", QCP.equals, "普通测试用知识点")});
        DynamicObject userData = BusinessDataServiceHelper.newDynamicObject(TK_USERDATA);
        StringBuilder sb1 = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            int ascii = 48 + (int) (Math.random() * 9);
            char c = (char) ascii;
            sb1.append(c);
        }
        userData.set(TD_UDCODE,sb1);
        userData.set(TD_UDCOURSEID,"普通选课专用数据");
        userData.set(TD_UDUSERID,RequestContext.get().getCurrUserId());
        userData.set(TD_UDKNPOINT,kp);
        userData.set(TD_UDANSNUM,0);
        userData.set(TD_UDSUNSCORE,0);
        userData.set(TD_UDdata,0);
        userData.set("status", "C");
        userData.set("enable", 1);
//            userData.set(TD_UDenable,"可用"); //使用状态
        SaveServiceHelper.saveOperate(TK_USERDATA,new DynamicObject[]{userData},null);

    }
}
