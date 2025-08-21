package plugins.recomanddata;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.*;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import static plugins.recomanddata.QuestionRecommender.*;

/**
 * 基础资料插件
 */
public class SimilarRecommend extends AbstractBasePlugIn implements Plugin {

    private static final String ENTRY_ENTITY_COLLECTION = "lag1_entryentity_pigai";
    private static final String TKPROBLEM_SCORE= "lag1_problem_score";
    /**
     * 定义缓存对象
     */
    private DistributeSessionlessCache cache;
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Button mind_btn = this.getView().getControl("lag1_xiangsi");
        mind_btn.addClickListener(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        // 初始化缓存
        cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("customRegion");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        if(StringUtils.equals("lag1_xiangsi",source.getKey())){
            String.valueOf(RequestContext.get().getCurrUserId());
            // 单据体标识
            int rowCount = this.getModel().getEntryRowCount(ENTRY_ENTITY_COLLECTION);

            int selectedRowIndex = this.getModel().getEntryCurrentRowIndex(ENTRY_ENTITY_COLLECTION);// 1. 获取单据体当前选中行号（从0开始）
            DynamicObjectCollection entryEntities = this.getModel().getEntryEntity(ENTRY_ENTITY_COLLECTION);// 2. 获取单据体数据集合
            DynamicObject selectedRowData = entryEntities.get(selectedRowIndex);// 3. 获取选中行数据
            String prodes = selectedRowData.get("lag1_prodes").toString();// 4. 获取选中行的字段值
            DynamicObject record = findExistingRecord_ProblemId(prodes);
            String targetId = null;
            if (record != null) {
                targetId = record.get("number").toString();
            }
            DoneQuesition();

            try {
                recommend(targetId,DoneQuesition());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public List<String> DoneQuesition() {
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
//        this.getView().showMessage(questionIds.toString());
        // 获取记录数
        int totalCount = 0;
        if (problemScores != null) {
            totalCount = problemScores.length;
        }
        // 可用在主页更新
        System.out.println("学生已作答题目总数：" + totalCount);
        return questionIds;
    }

    private DynamicObject findExistingRecord_ProblemId(String lag1_prodes) {
        // 定义要查询的字段（可选，如果不需要特定字段可以传 null 或空字符串）
        String fields = "number"; // 或者直接传 null/"" 表示查询所有字段
        // 构建 QFilter 条件
        QFilter filter = new QFilter("lag1_prodes", QCP.equals, lag1_prodes);
        // 执行查询
        DynamicObject[] records = BusinessDataServiceHelper.load("lag1_protest", fields, new QFilter[]{filter});
        // 返回第一条记录（如果没有则返回 null）
        return records.length > 0 ? records[0] : null;
    }

    public void recommend(String targetId,List<String> excludeIds) throws Exception {
        String csvPath = "data/lag1_protest.csv";
        String word2vecPath = "data/45000-small.txt";
        // 加载词向量
        loadWord2Vec(word2vecPath);
        // 读取题目
        List<Question> questions = loadQuestions(csvPath);
        // 计算所有题目的向量
        for (Question q : questions) {
            q.vector = getSentenceVector(q.mergedText);
        }
        // 测试：传入题目ID推荐
//        String targetId = "Question001"; // 测试ID
        // 创建要排除的题目ID列表
//        List<String> excludeIds = Arrays.asList("Question019", "Question050","Question025","Question022","Question003");

        // 使用排除列表进行推荐
        List<Question> recs = recommendSimilarQuestions(targetId, questions, 3, excludeIds);

        List<String> linkkpValues = getStrings(recs);
        this.getView().showMessage(linkkpValues.toString());

        if(linkkpValues.isEmpty()) this.getView().showMessage("暂无相似题目，请稍后再试");
        else{
            String formattedQuestionIds = linkkpValues.isEmpty() ? "" : linkkpValues.toString().substring(1, linkkpValues.toString().length() - 1);
            cache.put("formattedQuestionIds",formattedQuestionIds);
            String message = "是否要跳转练习以下相似题目："+linkkpValues.toString();
            ConfirmCallBackListener confirmCallBackListener = new ConfirmCallBackListener("myCallbackId",this);

            // 正确方式：使用 showConfirm 而非 showMessage
            this.getView().showConfirm(
                    message,                  // 消息内容
                    MessageBoxOptions.YesNo,               // 按钮选项
                    ConfirmTypes.Default,
                    confirmCallBackListener
            );
        }
    }

    @NotNull
    private static List<String> getStrings(List<Question> recs) {
        List<String> linkkpValues = new ArrayList<>();
//        linkkpValues.add("=== 推荐结果 ===");
        for (Question q : recs) {
//            System.out.println("题目ID: " + q.id);
//            System.out.println("相似度: " + q.sim);
//            System.out.println("原始文本: " + q.mergedText);
//            System.out.println("-------------------");
            linkkpValues.add(q.id);
//            linkkpValues.add("相似度: " + q.sim);
//            linkkpValues.add("原始文本: " + q.mergedText);
//            linkkpValues.add("-------------------");
        }
        return linkkpValues;
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

    /**
     * 回调监听
     * @param messageBoxClosedEvent
     */
    @Override
    public void confirmCallBack(MessageBoxClosedEvent messageBoxClosedEvent) {
        super.confirmCallBack(messageBoxClosedEvent);
        if("myCallbackId".equals(messageBoxClosedEvent.getCallBackId())&&messageBoxClosedEvent.getResult() == MessageBoxResult.Yes){
            String formattedQuestionIds= cache.get("formattedQuestionIds");
            if(formattedQuestionIds==null){
                this.getView().showMessage("题目列表未初始化");
            }else{
                openWrite(formattedQuestionIds);
            }
            cache.remove("formattedQuestionIds");   //销毁缓存
        }else if("myCallbackId".equals(messageBoxClosedEvent.getCallBackId()) && messageBoxClosedEvent.getResult()==MessageBoxResult.No){
            cache.remove("formattedQuestionIds");   //销毁缓存
        }
    }
}