package plugins.AI;

import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.CustomEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.util.JSONUtils;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态表单插件
 */
public class AssiCall extends AbstractFormPlugin implements Plugin {
    private static final String BTN_KEY = "lag1_buttonap";
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 获取按钮控件并添加点击监听
        Button button = this.getControl(BTN_KEY);
        button.addClickListener(this);
    }
    @Override
    public void customEvent(CustomEventArgs e) {
        // 检查事件名称
            String args = e.getEventArgs();
            if ("propsUpdated".equals(e.getEventName())) {
                this.getView().showMessage("原始数据:" + args);
                String pageId = this.getView().getMainView().getPageId();
                Object pkvalue = getProcessFid("process-250618841BB5F0");
                String bookList="计算机网络";
                JSONObject needJson = new JSONObject();
                needJson.put("bookList", bookList);
                DispatchServiceHelper.invokeBizService("ai", "gai", "GaiService","selectProcessInSideBar",pkvalue, pageId,args);
           }
    }
    @Override
    public void click(EventObject evt) {
        super.click(evt);
        // 判断事件源是否为目标按钮
        Control source = (Control) evt.getSource();
        if (BTN_KEY.equals(source.getKey())) {
            String pageId = this.getView().getMainView().getPageId();
            Object pkvalue = getProcessFid("process-2507146475CBF3");
            DispatchServiceHelper.invokeBizService("ai", "gai", "GaiService","selectProcessInSideBar",pkvalue, pageId, "----------------------正在搜索----------------------\n");
//            process-250709B1A2338A
        }
    }
    public Object getProcessFid(String billNo) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("gai_process",
                "number," +
                        "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        long idd = dynamicObject.getLong("id");
        return Long.parseLong(String.valueOf(idd));
    }
}