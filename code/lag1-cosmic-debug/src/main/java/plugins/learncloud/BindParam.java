package plugins.learncloud;

import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 动态表单插件
 */
public class BindParam extends AbstractFormPlugin implements Plugin {
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        Object treeDataObj = this.getView().getFormShowParameter().getCustomParam("treeData");
//        this.getView().showMessage("json"+treeDataObj.toString());
        CustomControl customControl = this.getView().getControl("lag1_customcontrolap");
        customControl.setData(treeDataObj);
    }
}