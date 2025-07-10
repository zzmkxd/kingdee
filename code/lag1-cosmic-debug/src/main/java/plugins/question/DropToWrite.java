package plugins.question;

import com.grapecity.documents.excel.Q;
import com.kingdee.cosmic.ctrl.kdf.servertable.IRow;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
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
                    Long selectId = (Long) col.getPrimaryKeyValue();
                    //传递选中的题单pkid给做题页面接收
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
    }
}

