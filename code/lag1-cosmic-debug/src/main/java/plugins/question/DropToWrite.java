package plugins.question;

import com.grapecity.documents.excel.Q;
import com.kingdee.cosmic.ctrl.kdf.servertable.IRow;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.*;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.list.BillList;
import kd.bos.list.ListGridView;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

import static kd.bos.list.ListShowParameter.BILLLISTID;

/**
 * 基础资料插件
 */
public class DropToWrite extends AbstractBasePlugIn implements Plugin {
    private static final String TKPROBLEMS = "lag1_problems";
    private static final String TKFACEVERITY = "lag1_faceverify";
    private Long selectId=null;

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册工具栏点击事件
        Toolbar toolbar = this.getView().getControl("toolbarap");
        toolbar.addItemClickListener(this);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if ("lag1_startans".equals(itemKey)) {
            // 获取当前选中的行
//            ListGridView gridview = this.getControl("gridview");
            BillList billList = this.getControl(BILLLISTID);
            ListSelectedRowCollection gridview = billList.getSelectedRows();
            if(gridview!=null){
                ListSelectedRow col = gridview.get(0);
                if(col!=null){
                    selectId = (Long) col.getPrimaryKeyValue();
                    QFilter qFilter = new QFilter("id", QCP.equals, selectId);
                    DynamicObject dys = BusinessDataServiceHelper.loadSingle(TKPROBLEMS, "id,lag1_prolist,number,lag1_combofield", new QFilter[]{qFilter});
                    if(dys.getString("lag1_combofield").equals("2")){
                        FormShowParameter showParameter = new FormShowParameter();
                        showParameter.getOpenStyle().setShowType(ShowType.Modal);
                        showParameter.setFormId(TKFACEVERITY);
                        CloseCallBack closeCallBack = new CloseCallBack(this,"returnVerifyResult");
                        showParameter.setCloseCallBack(closeCallBack);
                        //设置回调
// 在这里可以设置打开页面的大小
                        StyleCss style = new StyleCss();
                        style.setWidth("1000");
                        style.setHeight("600");
                        showParameter.getOpenStyle().setInlineStyleCss(style);
                        this.getView().showForm(showParameter);
                    }else{
                        openWrite();
                    }
                }else{
                    this.getView().showMessage("未选中试卷");
                }
            }else{
                this.getView().showMessage("无法获取列表视图");
            }
        }
    }

    /**
     * 打开做题表单
     */
    private void openWrite(){
        //传递选中的题单pkid给做题页面接收
        BillList billList = this.getControl(BILLLISTID);
        ListSelectedRowCollection gridview = billList.getSelectedRows();
        if(gridview!=null) {
            ListSelectedRow col = gridview.get(0);
            if (col != null) {
                selectId = (Long) col.getPrimaryKeyValue();
                //打开做题界面
                FormShowParameter nxtList = new FormShowParameter();
                nxtList.getOpenStyle().setShowType(ShowType.Modal);
                nxtList.setFormId("lag1_quesition_write");
                nxtList.setCustomParam("prolistPKID",selectId);
                this.getView().showForm(nxtList);
            }else{
                this.getView().showMessage("未选中试卷");
            }
        }else{
            this.getView().showMessage("无法获取列表视图");
        }

    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        String callBackId = closedCallBackEvent.getActionId();
        if(callBackId.equalsIgnoreCase("returnVerifyResult")){
            boolean res = (boolean) closedCallBackEvent.getReturnData();
            if(!res){
                this.getView().showMessage("验证失败");
            }else{
                openWrite();
            }
        }
    }

    //    @Override
//    public void afterClosed(AfterClosedEvent closedEvent) {
//        if (TKFACEVERITY.equals(closedEvent.getFormId())) {
//            Boolean verifyResult = (Boolean) closedEvent.getReturnData();
//            Long selectId = (Long) closedEvent.getCustomParam("prolistPKID");
//
//            if (Boolean.TRUE.equals(verifyResult)) {
//                // 验证通过，打开做题界面
//                FormShowParameter questionParam = new FormShowParameter();
//                questionParam.setFormId("lag1_quesition_write");
//                questionParam.setCustomParam("prolistPKID", selectId);
//                this.getView().showForm(questionParam);
//            } else {
//                this.getView().showMessage("人脸验证失败，无法开始答题");
//            }
//        }
//    }


}

