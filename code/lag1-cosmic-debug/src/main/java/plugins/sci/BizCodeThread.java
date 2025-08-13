package plugins.sci;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.mq.MQFactory;
import kd.bos.mq.MessagePublisher;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;

public class BizCodeThread implements Runnable {
	private boolean isUseMQ = false;
	public BizCodeThread(boolean isUseMQ) {
		super();
		this.isUseMQ = isUseMQ;
	}
	@Override
	public void run() {
		if(isUseMQ) {
			MessagePublisher mp = MQFactory.get().createSimplePublisher("lag1_damn", "erkai_queue");
			try{
				mp.publish("hello");
			}finally{
				mp.close();
			}
		}else {
			DynamicObject obj = BusinessDataServiceHelper.newDynamicObject("lag1_homework_correct");
			obj.set("status", "C");
			obj.set("enable", 1);
			obj.set("name", "Mq考试test");
			obj.set("creater", (int) (Math.random() % 100));
			obj.set("number", "ProList002");
			obj.set("lag1_textfield1", (int) (Math.random() % 11));

			OperationResult result = OperationServiceHelper.executeOperate("submit", "lag1_homework_correct", new DynamicObject[] {obj}, OperateOption.create());
			//---------发消息通知用户申请成功或失败
			//--------MessageCenterServiceHelper.sendMessage(messageInfo)
		}
	}
}
