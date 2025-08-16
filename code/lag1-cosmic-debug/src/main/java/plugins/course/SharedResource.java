package plugins.course;

import kd.bos.base.AbstractBasePlugIn;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 基础资料插件
 */
public class SharedResource extends AbstractBasePlugIn implements Plugin {
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        String courseId = this.getView().getFormShowParameter().getCustomParam("courseId");
        String courseName = this.getView().getFormShowParameter().getCustomParam("courseName");
        if(courseId!=null && courseName!=null){
            this.getModel().setValue("number",courseId);
            this.getModel().setValue("name",courseName);
        }
    }
}