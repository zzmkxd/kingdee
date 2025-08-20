package plugins.recomanddata;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mq.MQFactory;
import kd.bos.mq.MessagePublisher;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import plugins.MQ.MulThreadsEdit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础资料插件
 */
public class UpdateUserData extends AbstractBasePlugIn implements Plugin {
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
        if ("lag1_baritemap1".equals(itemKey)) {
            selectKNP("社会主义实践的探索");
        }
    }

    public void selectKNP(String knpname) {
//        每次在首页打开，后端发送给前端最低的7~10个知识点和权重--前端点击某个知识点，会向后端发送该知识点名字，按该知识点筛选题目(按知识点找，并只留下一列字段-题目id 用其转换成列表存储)
// 1. 构建过滤条件（lag1_knpoint1 或 lag1_knpoint2 = "社会主义实践的探索"）
        QFilter filter1 = new QFilter("lag1_entryentity_linkp.lag1_knpoint1.name", QCP.equals, knpname)
                .and(new QFilter("lag1_entryentity_linkp.lag1_knpoint1", QCP.is_notnull, null));
        QFilter filter2 = new QFilter("lag1_entryentity_linkp.lag1_knpoint2.name", QCP.equals, knpname)
                .and(new QFilter("lag1_entryentity_linkp.lag1_knpoint2", QCP.is_notnull, null));;
        QFilter orFilter = filter1.or(filter2);

// 2. 查询基础资料 lag1_protest，并关联过滤单据体条件
        DynamicObject[] protestList = BusinessDataServiceHelper.load(
                "lag1_protest",  // 基础资料元数据标识
                "name",  // 选择字段（包含单据体）
                new QFilter[]{orFilter}  // 传入 OR 条件
        );
        List<String> questionIds = new ArrayList<>();
        if (protestList != null) {
            for (DynamicObject problem : protestList) {
                    questionIds.add(problem.getString("number")); // 基础资料主键通常叫id
                }

        }
        this.getView().showMessage(questionIds.toString());
        //题库中 与所传入的知识点 同样相关联的题目序号列表
    }

    public void demo() {
        QFilter filter = new QFilter("lag1_data", QCP.not_equals,0);
        filter.and(new QFilter("creator.id",QCP.equals, RequestContext.get().getCurrUserId())); //筛选本userid的条目

// 设置排序条件 - 按lag1_data升序排列
        String orderBy = "lag1_data asc";
// 查询数据，限制返回10条
        DynamicObjectCollection dataCollection = QueryServiceHelper.query(
                "lag1_user_data",       // 实体名称
                "lag1_linkkp,lag1_data", // 查询字段
                new QFilter[]{filter},  // 过滤条件
                orderBy,                // 排序条件
                5                      // 限制条数
        );

// 遍历结果获取lag1_linkkp字段值
        List<Object> linkkpValues = new ArrayList<>();
        for (DynamicObject obj : dataCollection) {
            Object linkkpValue = obj.get("lag1_linkkp.name");
            linkkpValues.add(linkkpValue);
//            lag1_data
            linkkpValue = obj.get("lag1_data");
            linkkpValues.add(linkkpValue);
        }
        this.getView().showMessage(linkkpValues.toString());
    }
}