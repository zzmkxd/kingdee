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

    private static final String TKPROBLEM_SCORE= "lag1_problem_score";
    private static final String TKPROBLEM= "lag1_protest";
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
            test();
        }
        if ("lag1_baritemap1".equals(itemKey)) {
            test2("社会主义实践的探索");
        }
        if ("lag1_baritemap2".equals(itemKey)) {
            demo();
        }
    }
    public void test() {
//        已作答过的要排除题目跑一轮成绩关联表，用字符串存 （用","分隔的题目id）(按userid找，并只留下一列字段-题目id 用其转换成列表存储)
        QFilter filter = new QFilter("lag1_studentid", QCP.equals, String.valueOf(RequestContext.get().getCurrUserId()));
        // 查询所有数据 加载具体的题号
        DynamicObject[] problemScores = BusinessDataServiceHelper.load(
                TKPROBLEM_SCORE,  // 成绩关联表基础资料表名
                "lag1_proid",      // 查询基础资料字段引用的题目ID属性 - 不确定可不可行
                new QFilter[]{filter}     // 过滤条件 确认学生id所作所有题目
        );
        // 提取题目ID集合
        List<String> questionIds = new ArrayList<>();
        if (problemScores != null) {
            for (DynamicObject score : problemScores) {
                DynamicObject problem = score.getDynamicObject("lag1_proid");
                if (problem != null) {
                    questionIds.add(problem.getString("number")); // 基础资料主键通常叫id
                }
            }
        }

        System.out.println("当前用户题目ID列表：" + questionIds);
        this.getView().showMessage(questionIds.toString());
        // 获取记录数
        int totalCount = 0;
        if (problemScores != null) {
            totalCount = problemScores.length;
        }
        // 可用在主页更新
        System.out.println("学生已作答题目总数：" + totalCount);
    }
    public void test2(String knpname) {
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
//        题库用遍历保存的方式，遍历一遍后整张表后保存为csv文件
        // 1. 查询数据
        DynamicObject[] protests = BusinessDataServiceHelper.load(
                "lag1_protest",
                "number,lag1_prodes,lag1_difficulty,lag1_standard_answer,lag1_itema,lag1_itemb,lag1_itemc,lag1_itemd",
                null
        );

        if (protests == null || protests.length == 0) {
            System.out.println("未找到数据");
            return;
        }

        // 2. 定义CSV文件路径
        String filePath = "E:/lag1_protest.csv";

        // 3. 使用BufferedWriter和Stream API处理CSV内容
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 写入CSV头部
            writer.write("number,lag1_prodes,lag1_difficulty,lag1_standard_answer,lag1_itema,lag1_itemb,lag1_itemc,lag1_itemd");
            writer.newLine();

            // 处理每一行数据
            for (DynamicObject protest : protests) {
                // 收集一行中的所有字段
                List<String> rowData = new ArrayList<>();
                rowData.add(protest.getString("number"));
                rowData.add(protest.getString("lag1_prodes"));
                rowData.add(protest.getString("lag1_difficulty"));
                rowData.add(protest.getString("lag1_standard_answer"));
                rowData.add(protest.getString("lag1_itema"));
                rowData.add(protest.getString("lag1_itemb"));
                rowData.add(protest.getString("lag1_itemc"));
                rowData.add(protest.getString("lag1_itemd"));

                // 使用Stream API处理CSV行
                String csvLine = rowData.stream()
                        .map(s -> s == null ? "" : (s.contains(",") ? "\"" + s.replace("\"", "\"\"") + "\"" : s))
                        .collect(Collectors.joining(","));

                writer.write(csvLine);
                writer.newLine();
            }

            this.getView().showMessage("CSV 文件已生成：" + filePath);
        } catch (IOException e) {
            this.getView().showMessage("保存文件失败：" + e.getMessage());
        }
    }
}