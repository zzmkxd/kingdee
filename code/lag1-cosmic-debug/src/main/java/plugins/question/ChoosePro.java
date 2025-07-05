package plugins.question;

import dm.jdbc.util.StringUtil;
import javafx.scene.control.SelectionModel;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.list.BillList;
import kd.bos.list.IListView;
import kd.bos.list.ListCardView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import static kd.bos.list.ListShowParameter.BILLLISTID;

/**
 * 单据界面插件
 */
public class ChoosePro extends AbstractBillPlugIn implements Plugin {
    private boolean isRefresh=false;
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //注册工具栏点击事件
        Toolbar toolbar = this.getView().getControl("toolbarap");
        toolbar.addItemClickListener(this);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if ("lag1_confirm".equals(itemKey)) {
            String billNos = getSelectedBillNos();
            // 确保回调方法名称与BindProList中设置的一致
            this.getView().returnDataToParent(billNos);
            this.getView().close();
        }
    }

    /**
     * 设置根据传递过来的courseId,过滤当前proList
     * @param e
     */
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        if(isRefresh) return;   //防止递归调用
        isRefresh = true;
        String courseId = this.getView().getFormShowParameter().getCustomParam("courseId");
//        this.getView().showMessage("courseId"+courseId);
        if(courseId!=null && StringUtils.isNotBlank(courseId)){
            setProFilter(courseId);
            isRefresh = false;
        }
    }

    private void setProFilter(String courseId) {
        BillList proList = this.getView().getControl(BILLLISTID);
        if(proList!=null){
            QFilter filter = new QFilter("lag1_classno",QCP.equals,courseId);
            proList.setFilter(filter);
            proList.refresh();
        }else{
            System.out.println("setProFilter: 没有courseID传递");
        }
    }

    /**
     * 获取当前选中的 billno 列表
     *
     * @return 选中的 billno 列表
     */
    private String getSelectedBillNos() {
        String fields = "id,billno";
        List<String> billNos = new ArrayList<>();
        BillList billList = this.getControl(BILLLISTID);
        ListSelectedRowCollection collection = billList.getSelectedRows();
        for(ListSelectedRow col : collection){
            Long selectId = (Long) col.getPrimaryKeyValue();
            QFilter qFilter = new QFilter("id", QCP.equals, selectId);
            DynamicObject dys = BusinessDataServiceHelper.loadSingle("lag1_protest", "id,billno",
                    new QFilter[]{qFilter});
            String billNo = dys.getString("billno");
            billNos.add(billNo);
        }
        return String.join(",", billNos); // 改为返回逗号分隔的字符串
    }
}