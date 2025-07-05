package plugins.question;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * 基础资料插件
 */
public class ProblemAutoNumberPlugin extends AbstractBillPlugIn implements Plugin {
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        //绑定教材ID
        String courseId = this.getView().getFormShowParameter().getCustomParam("courseId");
        if(StringUtils.isNotBlank(courseId)){
            this.getModel().setValue("lag1_classno",courseId);
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //获取当前表单对象
        DynamicObject formData = this.getModel().getDataEntity();
        if(formData.getString("billno")==null || formData.getString("billno").isEmpty()){
            // 查询所有ID但不加载具体数据
            DynamicObject[] posts = BusinessDataServiceHelper.load(
                    "lag1_protest",  // 教材基础资料表名
                    "id",         // 只查询ID字段
                    null          // 没有过滤条件
            );
// 获取记录数
            int totalCount = posts.length;
            System.out.println("问题基础资料总条数：" + totalCount);

            // 生成新编号（当前记录数+1）
            String newNumber = "Question" + String.format("%03d", totalCount + 1);

            // 设置到表单字段
            formData.set("billno", newNumber);
        }
    }
}