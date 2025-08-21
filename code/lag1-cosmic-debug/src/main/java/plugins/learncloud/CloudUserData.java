package plugins.learncloud;

import akka.stream.impl.fusing.Delay;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.CustomEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.sdk.plugin.Plugin;
import org.bytedeco.javacv.Parallel;

import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import static kd.bos.list.ListShowParameter.BILLLISTID;

/**
 * 动态表单插件
 */
public class CloudUserData extends AbstractFormPlugin implements Plugin {
    private static final String TKPROBLEM = "lag1_protest";
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        //创建过滤器
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

        if(dataCollection.isEmpty()){   //若本用户未选择课程,选择表中有的数据
            QFilter filter1 = new QFilter("lag1_data", QCP.not_equals,null);
            //查询数据，限制返回10条
            dataCollection = QueryServiceHelper.query(
                    "lag1_user_data",
                    "lag1_linkkp.name,lag1_data",
                    new QFilter[]{filter1},
                    orderBy,
                    10
            );
        }

        //颜色数组
        List<String> colorValues = new ArrayList<>();
        colorValues.add("#FF5252");
        colorValues.add("#FF7043");
        colorValues.add("#FFCA28");
        colorValues.add("#66BB6A");
        colorValues.add("#26C6DA");
        colorValues.add("#42A5F5");
        colorValues.add("#7E57C2");
        colorValues.add("#EC407A");
        colorValues.add("#AB47BC");
        colorValues.add("#5C6BC0");

        //遍历结果获取lag1_linkkp字段值
        List<Object> linkkpValues = new ArrayList<>();
        for (DynamicObject obj : dataCollection) {
            Object linkkpValue = obj.get("lag1_linkkp.name");
            linkkpValues.add(linkkpValue);
        }
//        this.getView().showMessage(linkkpValues.toString());
        StringBuilder jsonString = new StringBuilder("[\n");
        int weight=10;

        for(int i=0;i<linkkpValues.size();i++){
            String color = colorValues.get(i);
            String linkkpValue = linkkpValues.get(i).toString();
            jsonString.append("    {\"text\": \"").append(linkkpValue).append("\", \"weight\": ").append(weight).append(", \"color\": \"").append(color).append("\"}");
            if(i<linkkpValues.size()-1){
                jsonString.append(",\n");
            }
            weight-=1;  //递减，因为是按升序
        }
        jsonString.append("\n]");

        //获取自定义控件
        CustomControl customControl = this.getView().getControl("lag1_customcontrolap");
        customControl.setData(jsonString.toString());
    }

    @Override
    public void customEvent(CustomEventArgs e) {
        // 检查事件名称
        if ("wordClicked".equals(e.getEventName())) {
            String wordText = e.getEventArgs();
            selectProFromKNP(wordText);
//            this.getView().showMessage("点击的词云内容是: " + wordText);
            // 在这里处理点击事件
//            handleWordClick(wordText);
        }
    }

    private void selectProFromKNP(String knpname) {
        // 1. 构建过滤条件（lag1_knpoint1 或 lag1_knpoint2 = "社会主义实践的探索"）
        QFilter filter1 = new QFilter("lag1_entryentity_linkp.lag1_knpoint1.name", QCP.equals, knpname)
                .and(new QFilter("lag1_entryentity_linkp.lag1_knpoint1", QCP.is_notnull, null));
        QFilter filter2 = new QFilter("lag1_entryentity_linkp.lag1_knpoint2.name", QCP.equals, knpname)
                .and(new QFilter("lag1_entryentity_linkp.lag1_knpoint2", QCP.is_notnull, null));;
        QFilter orFilter = filter1.or(filter2);

        DynamicObject[] protestList = BusinessDataServiceHelper.load(
                TKPROBLEM,  // 基础资料元数据标识
                "name",  // 选择字段（包含单据体）
                new QFilter[]{orFilter}  // 传入 OR 条件
        );
        if(protestList.length==0){
            this.getView().showMessage("此知识点下暂时未上传题目，请稍后再试！");
        }else{
            List<String> questionIds = new ArrayList<>();
            if (protestList != null) {
                for (DynamicObject problem : protestList) {
                    if(questionIds.size()<4) questionIds.add(problem.getString("number")); // 基础资料主键通常叫id
                    else break; //设置最多4条
//                questionIds.add(problem.getString("number")); // 基础资料主键通常叫id
                }

            }
                        this.getView().showMessage("准备跳转练习知识点: "+"'"+knpname+"'下的题目："+questionIds.toString());
            // 去除前 [ 和后 ]
            String formattedQuestionIds = questionIds.isEmpty() ? "" : questionIds.toString().substring(1, questionIds.toString().length() - 1);
            openWrite(formattedQuestionIds);
//            this.getView().showMessage("准备跳转练习知识点: "+"'"+knpname+"'下的题目："+formattedQuestionIds);
        }
    }

    /**
     * 打开做题表单
     */
    private void openWrite(String prolist){
        FormShowParameter nxtList = new FormShowParameter();
        nxtList.getOpenStyle().setShowType(ShowType.Modal);
        nxtList.setFormId("lag1_quesition_write");
        nxtList.setCustomParam("prolist",prolist);
        nxtList.setCustomParam("isWordCloud","true");
        this.getView().showForm(nxtList);
    }
}