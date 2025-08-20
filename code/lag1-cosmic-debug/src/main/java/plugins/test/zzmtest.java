package plugins.test;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.form.ConfirmCallBackListener;
import kd.bos.form.MessageBoxOptions;
import kd.bos.form.MessageBoxResult;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * 动态表单插件
 */
public class zzmtest extends AbstractFormPlugin implements Plugin {
    private static final String BTN_KEY = "lag1_buttonap";
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 获取按钮控件并添加点击监听
        Button button = this.getControl(BTN_KEY);
        button.addClickListener(this);
    }
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
//        return;
        if(StringUtils.equals("lag1_xiangsi",source.getKey())){
            this.getView().showMessage("您确认来一波这么骚的操作吗？");
        }
    }
}