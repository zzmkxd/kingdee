package plugins.knowpoint;

import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态表单插件
 */
public class BindPicData extends AbstractFormPlugin implements Plugin {
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        String jsonString = this.getView().getFormShowParameter().getCustomParam("treeData");
        this.getView().showMessage(jsonString);
//        this.getView().getModel().putContextVariable("treeData",jsonString);
        // BindPicData.java
//        this.getView().getScriptEngine().executeScript("setExternalData(" + jsonString + ");");
        //获取自定义控件
        CustomControl customControl = this.getView().getControl("lag1_customcontrolap1");
        customControl.setData(jsonString);
    }
}