package plugins.question;

import com.aliyun.odps.utils.StringUtils;
import dm.jdbc.util.StringUtil;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.CloseCallBack;
import kd.bos.form.ShowType;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.list.ListShowParameter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.List;

/**
 * 基础资料插件
 */
public class BindProList extends AbstractBasePlugIn implements Plugin {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners();
        Button button1 = this.getView().getControl("lag1_addpro");
        button1.addClickListener(this);
    }

    //跳转题目卡片视图
    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        if(StringUtils.equals("lag1_addpro",source.getKey())){
            ListShowParameter nxtList = new ListShowParameter();
            nxtList.setBillFormId("lag1_protest");
            nxtList.setFormId("bos_list");
            nxtList.getOpenStyle().setShowType(ShowType.Modal);

            CloseCallBack closeCallBack = new CloseCallBack(this,"returnSelectedProblems");
            nxtList.setCloseCallBack(closeCallBack);
            
            this.getView().showForm(nxtList);
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        //绑定课程id
        String courseId = this.getView().getFormShowParameter().getCustomParam("courseId");
        if(StringUtils.isNotBlank(courseId)){
            this.getModel().setValue("lag1_coursenumber",courseId);
        }
    }

    //回调方法，赋值题目列表
    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        String callBackId = closedCallBackEvent.getActionId();
        if(callBackId.equalsIgnoreCase("returnSelectedProblems")){
            String billnos = (String) closedCallBackEvent.getReturnData();
            this.getModel().setValue("lag1_prolist", billnos);
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        //获取当前表单对象，自增prolist编号
        DynamicObject formData = this.getModel().getDataEntity();
        if(formData.getString("number")==null || formData.getString("number").isEmpty()){
            // 查询所有ID但不加载具体数据
            DynamicObject[] posts = BusinessDataServiceHelper.load(
                    "lag1_problems",  // 教材基础资料表名
                    "id",         // 只查询ID字段
                    null          // 没有过滤条件
            );
// 获取记录数
            int totalCount = posts.length;
            System.out.println("题单基础资料总条数：" + totalCount);

            // 生成新编号（当前记录数+1）
            String newNumber = "ProList" + String.format("%03d", totalCount + 1);

            // 设置到表单字段
            formData.set("number", newNumber);
        }
    }
}