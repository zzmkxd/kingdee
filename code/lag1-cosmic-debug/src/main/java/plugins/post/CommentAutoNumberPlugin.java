package plugins.post;

import com.grapecity.documents.excel.Q;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.IFormView;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * 基础资料插件
 */
public class CommentAutoNumberPlugin extends AbstractBasePlugIn implements Plugin {
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //获取当前表单对象
        DynamicObject formData = this.getModel().getDataEntity();
        if(formData.getString("number")==null || formData.getString("number").isEmpty()){
            // 查询所有ID但不加载具体数据
            DynamicObject[] posts = BusinessDataServiceHelper.load(
                    "lag1_commentlist",  // 帖子基础资料表名
                    "id",         // 只查询ID字段
                    null          // 没有过滤条件
            );
// 获取记录数
            int totalCount = posts.length;
            System.out.println("评论基础资料总条数：" + totalCount);

            // 生成新编号（当前记录数+1）
            String newNumber = "Comment" + String.format("%03d", totalCount + 1);

            // 设置到表单字段
            formData.set("number", newNumber);
        }
    }

    //    绑定帖子编号
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        String postNumber = this.getView().getFormShowParameter().getCustomParam("postNumber");
        if(StringUtils.isNotBlank(postNumber)){
            this.getModel().setValue("lag1_postnumber",postNumber);
        }else{
            this.getView().showMessage("绑定帖子编号错误");
        }
    }

    //监听表单提交操作

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if("submit".equalsIgnoreCase(afterDoOperationEventArgs.getOperateKey())){
            String postNumber = this.getModel().getValue("lag1_postnumber").toString();

            if(StringUtils.isNotBlank(postNumber)){
                updateCommentCount(postNumber);
//                this.getModel().setValue("recnum","11");
                IFormView parentView = this.getView().getParentView();
                parentView.invokeOperation("refresh");
                this.getView().sendFormAction(parentView);
                this.getView().close();
            }else{
                this.getView().showMessage("帖子编号为空，无法更新评论数");
            }
        }
    }

    private void updateCommentCount(String postNumber) {
        if(StringUtils.isNotBlank(postNumber)){
//            DynamicObject post = BusinessDataServiceHelper.loadSingle(
//                    new QFilter("lag1_postnumber", QCP.equals,postNumber),
//                    "lag1_commentlist",
//                    "id"
//                        );
            DynamicObject post = BusinessDataServiceHelper.loadSingle("lag1_post",new QFilter[]{new QFilter("number",QCP.equals,postNumber)});
            if (post!=null){
                int currentCommentCount = post.getInt("lag1_comment");
                post.set("lag1_comment",currentCommentCount+1);
                SaveServiceHelper.update(post);
            }else{
                System.out.println("未找到对应的帖子数据");
            }
        }else{
            System.out.println("帖子编号不能为空");
        }
    }

}