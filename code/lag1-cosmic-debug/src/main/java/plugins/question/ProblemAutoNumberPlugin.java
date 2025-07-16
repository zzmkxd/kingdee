package plugins.question;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * 基础资料插件
 */
public class ProblemAutoNumberPlugin extends AbstractBillPlugIn implements Plugin {
    private final String TKCOURSE = "lag1_course";
    private final String TKPROBLEM = "lag1_protest";
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        String courseId = this.getView().getFormShowParameter().getCustomParam("courseId");
//        this.getView().showMessage("courseId"+courseId);
        if(courseId!=null || !courseId.isEmpty()) this.getModel().setValue("lag1_courseidtxt",courseId);
        if(StringUtils.isNotBlank(courseId)){
//            this.getModel().setValue("lag1_courseid",courseId);
            String fields = "number";
            QFilter qFilter = new QFilter("number", QCP.equals,courseId);
            DynamicObject course = BusinessDataServiceHelper.loadSingle(TKCOURSE,fields,new QFilter[]{qFilter});
            if(course!=null){
//                this.getView().showMessage(course.getString("name"));
                this.getModel().setValue("lag1_courseid",course);
            }
        }else{
//            this.getView().showMessage("绑定课程编号数据错误");
        }
//        //绑定教材ID
//        String courseId = this.getView().getFormShowParameter().getCustomParam("courseId");
//        if(StringUtils.isNotBlank(courseId)){
//            this.getModel().setValue("lag1_classno",courseId);
//        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //获取当前表单对象
        DynamicObject formData = this.getModel().getDataEntity();
        if(formData.getString("number")==null || formData.getString("number").isEmpty()){
            // 查询所有ID但不加载具体数据
            DynamicObject[] posts = BusinessDataServiceHelper.load(
                    TKPROBLEM,  // 教材基础资料表名
                    "number",         // 只查询ID字段
                    null          // 没有过滤条件
            );
// 获取记录数
            int totalCount = posts.length;
            System.out.println("问题基础资料总条数：" + totalCount);


            // 生成新编号（当前记录数+1）
            String newNumber = "Question" + String.format("%03d", totalCount + 1);

            // 设置到表单字段
            formData.set("number", newNumber);
        }
    }
}