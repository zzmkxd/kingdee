package plugins.demo;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.sdk.plugin.Plugin;
import plugins.MQ.MulThreadsEdit;
import plugins.graph.KeyValueList;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import static org.eclipse.jdt.internal.compiler.util.Messages.records;

/**
 * 基础资料插件
 */
public class UpdateUserData extends AbstractBasePlugIn implements Plugin {
    private static final String BIAODAN = "lag1_d3js_knowpoints";
    @Override
    public void registerListener(EventObject e) {
        // 注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("toolbarap");
    }
    Log Logger = LogFactory.getLog(MulThreadsEdit.class);
    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if ("lag1_baritemap".equals(itemKey)) {
            demo();
        }
    }

    public void demo() {

        QFilter filter = new QFilter("lag1_data", QCP.not_equals,null);
        filter.and(new QFilter("creator.id",QCP.equals, RequestContext.get().getCurrUserId())); //筛选本userid的条目
        String orderBy = "lag1_data asc";

        DynamicObjectCollection dataCollection;

        //查询数据，限制返回10条
        dataCollection = QueryServiceHelper.query(
                "lag1_user_data",
                "lag1_linkkp.name,lag1_data",
                new QFilter[]{filter},
                orderBy,
                10
        );
// 遍历结果获取lag1_linkkp字段值
        KeyValueList linkkpValues = new KeyValueList();
        for (DynamicObject obj : dataCollection) {
            Object linkkpValue = obj.get("lag1_linkkp.name");
            Object linkdata = obj.get("lag1_data");
            linkkpValues.add(linkkpValue, linkdata);
        }
        this.getView().showMessage(linkkpValues.toString());
    }
    public void demo2() {

        QFilter filter = new QFilter("creator.id",QCP.equals, RequestContext.get().getCurrUserId()); //筛选本userid的条目
        DynamicObjectCollection dataCollection;

        //查询数据
        dataCollection = QueryServiceHelper.query("lag1_user_data","lag1_data", new QFilter[]{filter});
// 遍历结果获取lag1_linkkp字段值
        List<Object> linkkpValues = new ArrayList<>();
        for (DynamicObject obj : dataCollection) {

        }
        this.getView().showMessage(linkkpValues.toString());
    }

    private boolean findExistingRecord_KnpPlan(String coursename) {
        // 定义要查询的字段（可选，如果不需要特定字段可以传 null 或空字符串）
        String fields = "name"; // 或者直接传 null/"" 表示查询所有字段
        // 构建 QFilter 条件
        QFilter filter = new QFilter("lag1_courseid", QCP.equals, coursename + "知识点衔接方案");
        // 执行查询
        DynamicObject[] records = BusinessDataServiceHelper.load(BIAODAN, fields, new QFilter[]{filter});
        // 返回第一条记录（如果没有则返回 null）
        return records.length > 0;
    }


}