package plugins.MQ;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.mq.MQFactory;
import kd.bos.mq.MessagePublisher;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

public class SecBizCodeThread implements Runnable {
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

    private boolean useMQ;

    public SecBizCodeThread(boolean useMQ) {
        this.useMQ = useMQ;
    }

    @Override
    public void run() {
        if (useMQ) {
            // 使用MQ处理
            MessagePublisher mp = MQFactory.get().createSimplePublisher("lag1_learn", "erkai_queue2");
            for (int i = 1; i < 50; i++) {
                mp.publish(i);
            }
        } else {
            // 普通处理
            for (int i = 1; i < 50; i++) {
                initNormalUserData();
            }
        }
    }

    private void initNormalUserData() {
        DynamicObject kp = BusinessDataServiceHelper.loadSingle(TK_KNPOINT, "id,name", new QFilter[]{new QFilter("name", QCP.equals, "普通测试用知识点")});
        DynamicObject userData = BusinessDataServiceHelper.newDynamicObject(TK_USERDATA);
        StringBuilder sb1 = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            int ascii = 48 + (int) (Math.random() * 9);
            char c = (char) ascii;
            sb1.append(c);
        }
        userData.set(TD_UDCODE, sb1);
        userData.set(TD_UDCOURSEID, "普通选课专用数据");
        userData.set(TD_UDUSERID, RequestContext.get().getCurrUserId());
        userData.set(TD_UDKNPOINT, kp);
        userData.set(TD_UDANSNUM, 0);
        userData.set(TD_UDSUNSCORE, 0);
        userData.set(TD_UDdata, 0);
        userData.set("status", "C");
        userData.set("enable", 1);
        SaveServiceHelper.saveOperate(TK_USERDATA, new DynamicObject[]{userData}, null);
    }
}