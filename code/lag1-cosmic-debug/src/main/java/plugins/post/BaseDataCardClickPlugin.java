package plugins.post;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.list.BillList;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 基础资料插件
 * 这个插件没在用
 */
public class BaseDataCardClickPlugin extends AbstractBasePlugIn implements Plugin {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("cardview");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        BillList billList = this.getControl("cardview");
        ListSelectedRowCollection collection = billList.getSelectedRows();
        Long selectId = (Long) collection.get(0).getPrimaryKeyValue();
        QFilter qFilter = new QFilter("id", QCP.equals,selectId);
        DynamicObject dys = BusinessDataServiceHelper.loadSingle("name",new QFilter[]{qFilter});
        String name = dys.getString("name");
        this.getView().showMessage(name);
    }
}