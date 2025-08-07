package plugins.test;

import com.alibaba.fastjson.JSONObject;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.ShowType;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import javax.json.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;

/**
 * 动态表单插件
 */
public class BindAIQuestionInfo extends AbstractFormPlugin implements Plugin {
    private final String PRONO = "lag1_prono";
    private final String PRODES = "lag1_prodes";
    private final String PROSTUANS = "lag1_stuans";

    //学生作答
//    private final String ANS_ONE = "lag1_ans1";
//    private final String ANS_TWO = "lag1_ans2";
//    private final String ANS_THREE = "lag1_ans3";
//    private final String ANS_FOUR = "lag1_ans4";
//    private final String ANS_FIVE = "lag1_ans5";

    private final String PRE_BTN = "lag1_prevbutton";
    private final String NXT_BTN = "lag1_nextbutton";
    private final String SUBMIT_BTN = "lag1_submitbtn";
    //    传参
    private String ProObject="";
    private List<JsonObject> questionJsonList = new ArrayList<>(); // 存储每道题目的JSON字符串

    //存储做题数据：用户答案
    private List<String> userAnswers = new ArrayList<>();

    //当前正在做的题目索引
    private int currentQuestionIndex=0;

    //总题目数
    private int totalQuestion=0;

    /**
     * 定义缓存对象，用于存储学生的答案
     * @param e
     */
    private DistributeSessionlessCache cache;

    private final String CANCEL_BTN = "btncancel";

    @Override
    public void initialize() {
        super.initialize();
        // 初始化缓存
        cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("customRegion");
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners();
        Button preBTN = this.getView().getControl(PRE_BTN);
        preBTN.addClickListener(this);
        Button nxtBTN = this.getView().getControl(NXT_BTN);
        nxtBTN.addClickListener(this);
        Button submitBTN = this.getView().getControl(SUBMIT_BTN);
        submitBTN.addClickListener(this);
        Button cancelBTN = this.getView().getControl(CANCEL_BTN);
        cancelBTN.addClickListener(this);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        reloadQ();
        if(StringUtils.equals(PRE_BTN,source.getKey())){
            getCurNo();
            saveAns(currentQuestionIndex);
            onQuestionJumpClick(evt,-1);
        }else if(StringUtils.equals(NXT_BTN,source.getKey())){
            getCurNo();
            saveAns(currentQuestionIndex);
            onQuestionJumpClick(evt,1);
        }else if(StringUtils.equals(SUBMIT_BTN,source.getKey())){
            //提交按钮封装作答和题目传参
            getCurNo();
            saveAns(currentQuestionIndex);
//            loadQuestionData();
            loadAnswerData();
//            this.getView().showMessage(userAnswers.toString());
            sendParameter();
        }else if(StringUtils.equals(CANCEL_BTN,source.getKey())){
            //取消，清除缓存并关闭
            //清理缓存
            clearCache();
//            this.getView().showMessage("取消");
        }
    }
    private void sendParameter() {
//        this.getView().showMessage("ans"+userAnswers);
        BillShowParameter showParameter = new BillShowParameter();
        showParameter.getOpenStyle().setShowType(ShowType.InCurrentForm); //替换当前页面内容
        showParameter.setFormId("lag1_homework_correct");
        showParameter.setCustomParam("problemList",questionJsonList);
//        this.getView().showMessage("qjl:"+questionJsonList.toString());
        showParameter.setCustomParam("ansList",userAnswers);
        showParameter.setCustomParam("aiornor","ai");
//        showParameter.setCustomParam("prolistId",prolist_id);
//        //题单id
        this.getView().showForm(showParameter);

        //清理缓存
        clearCache();
    }

    private void loadAnswerData() {
        for(int i=0;i<questionJsonList.size();i++){
            String questionId = String.valueOf(i);
            String ans = cache.get(questionId);
            userAnswers.add(ans);
//            if (i==0){
//                String ans = (String) this.getModel().getValue(ANS_ONE);
//                userAnswers.add(ans);
//            }else if(i==1){
//                String ans = (String) this.getModel().getValue(ANS_TWO);
//                userAnswers.add(ans);
//            }else if(i==2){
//                String ans = (String) this.getModel().getValue(ANS_THREE);
//                userAnswers.add(ans);
//            }else if(i==3){
//                String ans = (String) this.getModel().getValue(ANS_FOUR);
//                userAnswers.add(ans);
//            }else if (i==4){
//                String ans = (String) this.getModel().getValue(ANS_FOUR);
//                userAnswers.add(ans);
//            }
        }
    }

    //得到当前的currentQuestionIndex
    private void getCurNo(){
        DynamicObject DO = this.getModel().getDataEntity();
        currentQuestionIndex = Integer.parseInt(DO.getString(PRONO));
    }

    /**
     * 保存当前的答案
     * @param
     */
    private void saveAns(int curNo){
        String ans="";  //用户作答
//        String ansSavePlace="";
//        if (curNo==0) ansSavePlace=ANS_ONE;
//        else if (curNo==1) ansSavePlace=ANS_TWO;
//        else if (curNo==2) ansSavePlace=ANS_THREE;
//        else if (curNo==3) ansSavePlace=ANS_FOUR;
//        else if (curNo==4) ansSavePlace=ANS_FIVE;
        ans = (String) this.getModel().getValue(PROSTUANS);
        String questionId = String.valueOf(curNo);

        //将答案存储到缓存中
        cache.put(questionId,ans);
//        this.getModel().setValue(ansSavePlace,ans);
        this.getModel().setValue(PROSTUANS,"");   //清空
    }

    /**
     * 题目跳转按钮点击事件处理
     */
    private void onQuestionJumpClick(EventObject e,int direction){
//        this.getView().showMessage("ql"+questionJsonList.size());
        getCurNo();
        if(direction==-1){
            currentQuestionIndex=Math.max(currentQuestionIndex-1,0);
            this.getModel().setValue(PRONO,currentQuestionIndex);
        }else if(direction==1){
            currentQuestionIndex=Math.min(currentQuestionIndex+1,questionJsonList.size()-1);
            this.getModel().setValue(PRONO,currentQuestionIndex);
        }
        bindCurrentQuestionData();
        bindHasAnswerData();
    }

    private void reloadQ(){
        if(ProObject.equals("") || questionJsonList.isEmpty()){
            loadQuestionData();
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        reloadQ();
    }
    private void loadQuestionData() {
//        this.getView().showMessage("loadq");
        ProObject=this.getView().getFormShowParameter().getCustomParam("questionlist");
//        ProObject= "{\"questions\":[{\"id\":\"1\",\"question\":\"简述自由竞争向垄断资本主义的转变过程。\",\"difficulty\":\"2\",\"answer\":\"自由竞争向垄断资本主义的转变过程主要包括：1)生产和资本的高度集中，企业规模扩大；2)竞争导致部分企业被淘汰或被兼并；3)少数大企业通过协议或联合形成垄断组织；4)垄断组织控制生产和市场，获取高额垄断利润。\",\"knowledge_points\":[\"垄断资本主义的形成\"]},{\"id\":\"2\",\"question\":\"全球化与科技革命对当代资本主义有哪些影响？\",\"difficulty\":\"3\",\"answer\":\"全球化与科技革命对当代资本主义的影响包括：1)全球化促进了资本、商品和劳动力的跨国流动，扩大了市场范围；2)科技革命提高了生产效率，改变了生产方式；3)加剧了国际竞争，推动了资本主义生产关系的调整；4)带来了新的社会问题和矛盾，如数字鸿沟和就业结构变化。\",\"knowledge_points\":[\"当代资本主义的新变化\"]},{\"id\":\"3\",\"question\":\"资本主义经济制度的剥削性与矛盾性体现在哪些方面？\",\"difficulty\":\"4\",\"answer\":\"资本主义经济制度的剥削性体现在资本家通过占有生产资料剥削工人的剩余价值；矛盾性体现在：1)生产社会化与生产资料私人占有之间的矛盾；2)个别企业生产的有组织性与整个社会生产的无政府状态之间的矛盾；3)生产能力无限扩大与劳动人民有支付能力需求相对缩小之间的矛盾。\",\"knowledge_points\":[\"资本主义经济制度的特征\"]}]}";
        // 使用javax.json解析
        JsonReader jsonReader = Json.createReader(new StringReader(ProObject));
        JsonObject rootObject = jsonReader.readObject();
        JsonArray questions = rootObject.getJsonArray("questions");
//        this.getView().showMessage(questions.toString());
        // 遍历questions数组，将每道题目转为字符串存储
        for (JsonValue value : questions) {
            if (value.getValueType() == JsonValue.ValueType.OBJECT) {
                questionJsonList.add((JsonObject) value);
//                JsonObject questionObj = (JsonObject) value;
//                questionJsonList.add(questionObj.toString());
//                this.getView().showMessage(questionObj.toString());
            }
        }

        // 赋值总题目数
        totalQuestion = questionJsonList.size();

        //默认显示第一道题目
        if (!questionJsonList.isEmpty()) {
            bindCurrentQuestionData();
        }

        // 可选：打印调试信息
//        this.getView().showMessage("已加载题目数: " + totalQuestion);
    }

    /**
     * 绑定题目
     */
    private void bindCurrentQuestionData() {
        currentQuestionIndex= Integer.parseInt(this.getModel().getDataEntity().getString(PRONO));
        if (questionJsonList.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questionJsonList.size()) {
            return;
        }

//        // 解析当前题目的JSON字符串
//        JsonReader jsonReader = Json.createReader(new StringReader(questionJsonList.get(currentQuestionIndex)));
//        JsonObject questionObj = jsonReader.readObject();

        JsonObject questionObj = questionJsonList.get(currentQuestionIndex);
        // 提取题目描述（question字段）并绑定到PRODES字段
        String questionText = questionObj.getString("question");
        this.getModel().setValue(PRODES, questionText); // 绑定到动态表单字段
    }

    /**
     * 绑定当前作答数据到作答区域
     */
    private void bindHasAnswerData() {
        if (questionJsonList.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questionJsonList.size()) {
            return;
        }

//        // 解析当前题目的JSON字符串
//        JsonReader jsonReader = Json.createReader(new StringReader(questionJsonList.get(currentQuestionIndex)));
//        JsonObject questionObj = jsonReader.readObject();
//        JsonObject questionObj = questionJsonList.get(currentQuestionIndex);
        // 提取题目描述（question字段）并绑定到PRODES字段
//        String questionText = questionObj.getString("question");
//        this.getModel().setValue(PRODES, questionText); // 绑定到动态表单字段
//        DynamicObject currentQuestion = questionJsonList.get(currentQuestionIndex);
//        String questionId = currentQuestion.getString("number"); // 假设billno是题目ID
//        String questionType = currentQuestion.getString(PROTYPE);
        // 绑定作答(可选)
            String stuAns="";
            String questionId = String.valueOf(currentQuestionIndex);   //当前题目ID
            stuAns = cache.get(questionId);
//            if(currentQuestionIndex==0){
//                stuAns= (String) this.getModel().getValue(ANS_ONE);
//            }else if(currentQuestionIndex==1){
//                stuAns = (String) this.getModel().getValue(ANS_TWO);
//            }else if(currentQuestionIndex==2){
//                stuAns = (String) this.getModel().getValue(ANS_THREE);
//            }else if(currentQuestionIndex==3){
//                stuAns = (String) this.getModel().getValue(ANS_FOUR);
//            }else if(currentQuestionIndex==4){
//                stuAns = (String) this.getModel().getValue(ANS_FIVE);
//            }
            this.getModel().setValue(PROSTUANS,stuAns);

    }

    private void clearCache() {
        // 清除缓存
        for (int i = 0; i < questionJsonList.size(); i++) {
            String questionId = String.valueOf(i);
            cache.remove(questionId);
        }
    }
}