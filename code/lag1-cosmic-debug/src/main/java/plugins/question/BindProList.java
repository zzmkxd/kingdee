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
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.list.BillList;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础资料插件
 */
public class BindProList extends AbstractBasePlugIn implements Plugin {
    private static final String FIELD_PROBLEM_LIST = "lag1_prolist";
    private static final String PROTEST_FORM_NAME = "lag1_protest";

    private boolean isRefresh=false;

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
            DynamicObject DO = this.getModel().getDataEntity();
            String courseId = DO.getString("lag1_coursenumber");    //传递给题目list，作为filter过滤
            ListShowParameter nxtList = new ListShowParameter();
            nxtList.setBillFormId(PROTEST_FORM_NAME);
            nxtList.setFormId("bos_list");
            nxtList.getOpenStyle().setShowType(ShowType.Modal);
            nxtList.setCustomParam("courseId",courseId);

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

    /**
     * //刷新此组卷下的题目单据列表，初始化filter
     * @param e
     */
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        DynamicObject formData = this.getModel().getDataEntity();
        //刷新此组卷下的题目单据列表，初始化filter
        String billnos = formData.getString(FIELD_PROBLEM_LIST);
        if(isRefresh) return;
        isRefresh=true;
        setProFilter(billnos);
        isRefresh=false;
    }

    //回调方法，赋值题目列表
    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        String callBackId = closedCallBackEvent.getActionId();
        if(callBackId.equalsIgnoreCase("returnSelectedProblems")){
            String billnos = (String) closedCallBackEvent.getReturnData();
            this.getModel().setValue(FIELD_PROBLEM_LIST, billnos);
            if(isRefresh) return;
            isRefresh=true;
            setProFilter(billnos);
            isRefresh=false;
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

        //刷新此组卷下的题目单据列表，初始化filter
        String billnos = formData.getString(FIELD_PROBLEM_LIST);
        if(isRefresh) return;
        isRefresh=true;
        setProFilter(billnos);
        isRefresh=false;
    }

    /**
     * 题目单据列表filter,动态渲染在组卷下
     * @param billnos
     */
    private void setProFilter(String billnos){
//        this.getView().showMessage("setProFilter被调用");
        BillList proList = this.getView().getControl("lag1_billlistap");
        if(proList != null){
//            if(billnos == null || billnos.isEmpty()){
//                this.getView().showMessage("No billnos provided.");
//                return;
//            }

            // 分割 billnos 字符串并去除空格
            String[] billnoArray = billnos.split(",");
            List<String> billnoList = new ArrayList<>();
            for(String billno : billnoArray){
                String trimmedBillno = billno.trim();
                if(!trimmedBillno.isEmpty()){
                    billnoList.add(trimmedBillno);
                }
            }

//            if(billnoList.isEmpty()){
//                this.getView().showMessage("No valid billnos to filter.");
//                return;
//            }

            // 使用 QFilter.in 方法
            QFilter inFilter = new QFilter("billno", QCP.in, billnoList);

            // 应用过滤器
            proList.setDataPermQFilters(Collections.singletonList(inFilter));

            // 刷新列表
            proList.refresh();
        } else {
            this.getView().showMessage("Bill list control not found.");
        }
    }
//    private void setProFilter(String billnos){
//        this.getView().showMessage("setProFilter被调用");
//        BillList proList = this.getView().getControl("lag1_billlistap");
//        if(proList!=null){
//            String[] billno = billnos.split(",");
//            QFilter[] filters = new QFilter[billno.length];
//            for(int i=0;i<billno.length;i++){
//                filters[i]=new QFilter("billno",QCP.equals,billno[i]);
//            }
//            QFilter combinedFilter = QFilter.or(filters);
//            proList.setDataPermQFilters(Arrays.asList(filters));
//        }
//        proList.refresh();
//    }

}