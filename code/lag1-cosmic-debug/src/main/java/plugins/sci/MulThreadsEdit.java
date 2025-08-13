package plugins.sci;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import plugins.sci.BizCodeThread;
import plugins.sci.LatchTest;

public class MulThreadsEdit extends AbstractBasePlugIn {
	Log Logger = LogFactory.getLog(MulThreadsEdit.class);
	@Override
	public void itemClick(ItemClickEvent evt) {
		if(evt.getItemKey().equals("lag1_concurrent")) {
			Runnable taskTemp = new BizCodeThread(false);
	        LatchTest latchTest = new LatchTest();
	        try {
				latchTest.startTaskAllInOnce(200, taskTemp);
			} catch (InterruptedException e) {
				Logger.error(e);
			}
		}else if(evt.getItemKey().equals("lag1_mq_concurrent")) {
			Runnable taskTemp = new BizCodeThread(true);
	        LatchTest latchTest = new LatchTest();
	        try {
				latchTest.startTaskAllInOnce(200, taskTemp);
			} catch (InterruptedException e) {
				Logger.error(e);
			}
		
		}
	}
}
