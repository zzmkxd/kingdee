package plugins.course;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * 基础资料插件
 */
public class BookAutoNumberPlugin extends AbstractBasePlugIn implements Plugin {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //获取当前表单对象
        DynamicObject formData = this.getModel().getDataEntity();
        if(formData.getString("number")==null || formData.getString("number").isEmpty()){
            // 查询所有ID但不加载具体数据
            DynamicObject[] posts = BusinessDataServiceHelper.load(
                    "lag1_book",  // 教材基础资料表名
                    "id",         // 只查询ID字段
                    null          // 没有过滤条件
            );
// 获取记录数
            int totalCount = posts.length;
            System.out.println("教材基础资料总条数：" + totalCount);

            // 生成新编号（当前记录数+1）
            String newNumber = "Book" + String.format("%03d", totalCount + 1);

            // 设置到表单字段
            formData.set("number", newNumber);
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        String courseId = this.getView().getFormShowParameter().getCustomParam("courseId");
        String courseName = this.getView().getFormShowParameter().getCustomParam("courseName");
        if(StringUtils.isNotBlank(courseId)){
            this.getModel().setValue("lag1_courseid",courseId);
        }else{
//            this.getView().showMessage("绑定课程编号数据错误");
        }
        if(StringUtils.isNotBlank(courseName)){
            this.getModel().setValue("lag1_coursename",courseName);
        }else{
//            this.getView().showMessage("绑定课程名称数据错误");
        }
    }
}