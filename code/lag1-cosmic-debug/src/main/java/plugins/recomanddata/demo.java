package plugins.recomanddata;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

public class demo extends AbstractBasePlugIn implements Plugin {
    private static final String D3JS_BIAODAN = "lag1_d3js_knowpoints";
    private static final String USERDATA_BIAODAN = "lag1_user_data";
    private static final String ENTRY_ENTITY = "lag1_knp";

    private void Click(){
        String coursename = "马克思主义基本原理";
        //选课
        DynamicObject tempdynamicObject = findExistingD3js(coursename);
        DynamicObjectCollection dataEntities = tempdynamicObject.getDynamicObjectCollection(ENTRY_ENTITY);
        for (DynamicObject dynamicObjectSingle : dataEntities) {
            String knpName = dynamicObjectSingle.getString("lag1_knowpname");//相关知识点
            NewUserData(knpName,coursename);
        }
        //退课


    }
    private void NewUserData(String knpName , String coursename){
/*
lag1_user_data 用户数据记录表

name 课程名称
creator 选课学生id
lag1_linkkp.name 知识点名称
lag1_ans_num 该知识点已答题数量
lag1_sum_score 该知识点总分
lag1_data 学生掌握程度百分比
*/
        //new一个DynamicObject表单对象
        DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject(USERDATA_BIAODAN);
        StringBuilder sb1 = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            int ascii = 48 + (int) (Math.random() * 9);
            char c = (char) ascii;
            sb1.append(c);
        }
        //设置对应属性
        dynamicObject.set("number", sb1.toString());
        dynamicObject.set("name", coursename);
        dynamicObject.set("status", "C");
        dynamicObject.set("enable", 1);
        dynamicObject.set("creator", RequestContext.get().getCurrUserId());
        dynamicObject.set("lag1_ans_num", 0);
        dynamicObject.set("lag1_sum_score", 0);
        dynamicObject.set("lag1_linkkp", knpName);
        SaveServiceHelper.saveOperate(USERDATA_BIAODAN, new DynamicObject[] {dynamicObject}, null);
    }
    private DynamicObject findExistingD3js(String coursename) {
        // 定义要查询的字段（可选，如果不需要特定字段可以传 null 或空字符串）
        String fields = "name"; // 或者直接传 null/"" 表示查询所有字段
        // 构建 QFilter 条件
        QFilter filter1 = new QFilter("name", QCP.equals, coursename);
        // 执行查询返回第一条记录（如果没有则返回 null）
        return BusinessDataServiceHelper.loadSingle(D3JS_BIAODAN,fields,new QFilter[]{filter1});
    }
}

