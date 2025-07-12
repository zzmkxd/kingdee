package plugins.test;

import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;
import java.util.EventObject;

import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
/**
 * 动态表单插件
 */
public class FacePicture extends AbstractFormPlugin  implements Plugin {
    private final static String KEY_PICTUREFIELD1 = "picturefield1";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        // 侦听单据体图片字段点击事件
        this.addClickListeners(KEY_PICTUREFIELD1);
    }

    @Override
    public void beforeClick(BeforeClickEvent evt) {
        super.beforeClick(evt);
        Control source = (Control)evt.getSource();
        if (StringUtils.equals(KEY_PICTUREFIELD1, source.getKey())){
            // TODO 在此添加业务逻辑
        }
    }
    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control)evt.getSource();
        if (StringUtils.equals(KEY_PICTUREFIELD1, source.getKey())){
            // TODO 在此添加业务逻辑
        }
    }
}