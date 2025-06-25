package plugins.post;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.IFormView;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 基础资料插件-帖子编码自增
 */
public class PostAutoNumberPlugin extends AbstractBasePlugIn implements Plugin {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //获取当前表单对象
        DynamicObject formData = this.getModel().getDataEntity();
        if(formData.getString("number")==null || formData.getString("number").isEmpty()){
            // 查询所有ID但不加载具体数据
            DynamicObject[] posts = BusinessDataServiceHelper.load(
                    "lag1_post",  // 帖子基础资料表名
                    "id",         // 只查询ID字段
                    null          // 没有过滤条件
            );
// 获取记录数
            int totalCount = posts.length;
            System.out.println("帖子基础资料总条数：" + totalCount);

            // 生成新编号（当前记录数+1）
            String newNumber = "Post" + String.format("%03d", totalCount + 1);

            // 设置到表单字段
            formData.set("number", newNumber);
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if("submit".equalsIgnoreCase(afterDoOperationEventArgs.getOperateKey())){
            //刷新父页面
            IFormView parentView = this.getView().getParentView();
            parentView.invokeOperation("refresh");
            this.getView().sendFormAction(parentView);
            this.getView().close(); //关闭窗口
        }
    }
}