package plugins.question;

import dm.jdbc.util.StringUtil;
import javafx.scene.control.RadioButton;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.util.ByteArray;

import java.util.*;

/**
 * 动态表单插件
 */
public class BindQuestionInfo extends AbstractFormPlugin implements Plugin {
    private static final String TKPROBLEMS = "lag1_problems";
    private static final String TKPROBLEM = "lag1_protest";
    //定义题目是否选中的标识
    private final String UNCOM_PRO = "lag1_uncom";
    private final String COM_PRO = "lag1_com";
    private final String PROTYPE = "lag1_questiontype";

    private final String PRE_BTN = "lag1_prevbutton";
    private final String NXT_BTN = "lag1_nextbutton";
    private final String SUBMIT_BTN = "lag1_buttonap2";
    private final String CUR_NO = "lag1_currentquestionindex";

    private final String ANS_ONE = "lag1_ans1";
    private final String ANS_TWO = "lag1_ans2";
    private final String ANS_THREE = "lag1_ans3";
    private final String ANS_FOUR = "lag1_ans4";
    private final String ANS_TXT="lag1_textareafield";  //答案多行文本
    private String prolist_id = ""; //组卷表单id


    //存储做题数据：用户答案
    private List<String> userAnswers = new ArrayList<>();
    //当前正在做的题目索引
    private int currentQuestionIndex=0;
    //所有题目列表
    private List<DynamicObject> questionObjects = new ArrayList<>();

    private List<String> pronoList=new ArrayList<>();

    @Override
    public void initialize() {
        super.initialize();
    }

    private void loadQuestionData() {
        //获取pkid
        Long pkid = this.getView().getFormShowParameter().getCustomParam("prolistPKID");
        if (StringUtils.isNotBlank(pkid.toString())) {
            QFilter qFilter = new QFilter("id", QCP.equals, pkid);
            DynamicObject dys = BusinessDataServiceHelper.loadSingle(TKPROBLEMS, "id,lag1_prolist,number,lag1_combofield", new QFilter[]{qFilter});
            prolist_id = dys.getString("number");
            String prolistType = dys.getString("lag1_combofield");
            Integer prolistTypeNum = Integer.valueOf(prolistType)-1;
            this.getModel().setValue("lag1_combofield",prolistTypeNum);
//            this.getView().showMessage("prolistType"+prolistType);
//            this.getModel().getDataEntity().set("lag1_combofield",prolistType);
            this.getModel().getDataEntity().set("lag1_prolistid",prolist_id);
            String prolist = dys.getString("lag1_prolist");
            String[] pronoArray = prolist.split(",");
//            List<String> pronoList = new ArrayList<>();
            for (String prono : pronoArray) {
                String trimmedProno = prono.trim();
                if (!trimmedProno.isEmpty()) {
                    pronoList.add(trimmedProno);
//                    updateAnswerStatusButtons(trimmedProno);
                }
            }
            questionObjects = new ArrayList<>();
            // 根据题目ID列表查询题目数据
            for (String prono : pronoList) {
                QFilter filter = new QFilter("number", QCP.equals, prono);
                DynamicObject question = BusinessDataServiceHelper.loadSingle(TKPROBLEM, new QFilter[]{filter});
                if (question != null) {
                    questionObjects.add(question);
                }
            }

            //默认显示第一道题目
            if (!questionObjects.isEmpty()) {
                bindCurrentQuestionData();
            }
        }
    }

    //得到当前的currentQuestionIndex
    private void getCurNo(){
        DynamicObject DO = this.getModel().getDataEntity();
        currentQuestionIndex = Integer.parseInt(DO.getString(CUR_NO));
    }

    /**
     * 得到当前已保存的答案
     */
    private String getCurAns(int curNo){
        DynamicObject DO = this.getModel().getDataEntity();
        String ans = "";
        switch (curNo){
            case 0:
                ans = DO.getString(ANS_ONE);
                break;
            case 1:
                ans = DO.getString(ANS_TWO);
                break;
            case 2:
                ans = DO.getString(ANS_THREE);
                break;
            case 3:
                ans = DO.getString(ANS_FOUR);
                break;
        }
        return ans;
    }

    private String getRadioCheck(){
        //单选按钮组标识lag1_radiogroupfield
        String value= (String) this.getModel().getValue("lag1_radiogroupfield");
        return value;
    }

    /**
     * 保存当前的答案
     * @param
     */
    private void saveAns(int curNo){
        String ans="";  //用户作答
        String ansSavePlace="";
        if (curNo==0) ansSavePlace=ANS_ONE;
        else if (curNo==1) ansSavePlace=ANS_TWO;
        else if (curNo==2) ansSavePlace=ANS_THREE;
        else if (curNo==3) ansSavePlace=ANS_FOUR;

        //判断是哪种类型的题目
        loadQuestionData();
        DynamicObject currentQuestion = questionObjects.get(curNo);
        String questionType = currentQuestion.getString(PROTYPE);
//        this.getView().showMessage("questionType"+questionType);
//        this.getView().showMessage(questionType);
        if(questionType.equals("1")){
            String num = getRadioCheck();
            if(num.equals("1")) this.getModel().setValue(ansSavePlace,"A");
            else if(num.equals("2")) this.getModel().setValue(ansSavePlace,"B");
            else if(num.equals("3")) this.getModel().setValue(ansSavePlace,"C");
            else if(num.equals("4")) this.getModel().setValue(ansSavePlace,"D");
        }else if(questionType.equals("3")||questionType.equals("4") || questionType.equals("6")||questionType.equals("7")){
            ans = (String) this.getModel().getValue(ANS_TXT);
//            this.getView().showMessage(ans);
            this.getModel().setValue(ansSavePlace,ans);
        }
        this.getModel().setValue(ANS_TXT,"");   //清空
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        for(int i=1;i<=4;i++){
            this.addClickListeners(UNCOM_PRO+i);
            this.addClickListeners(COM_PRO+i);
        }
        this.addClickListeners();
        Button preBTN = this.getView().getControl(PRE_BTN);
        preBTN.addClickListener(this);
        Button nxtBTN = this.getView().getControl(NXT_BTN);
        nxtBTN.addClickListener(this);
        Button submitBTN = this.getView().getControl(SUBMIT_BTN);
        submitBTN.addClickListener(this);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        if(StringUtils.equals(PRE_BTN,source.getKey())){
            //保存当前作答
//            this.getView().showMessage("prebtn");
            getCurNo();
            saveAns(currentQuestionIndex);
            onQuestionJumpClick(evt,-1);
        }else if(StringUtils.equals(NXT_BTN,source.getKey())){
//            this.getView().showMessage("nxtbtn");
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
        }
    }

    /**
     * 传参：题目+作答给结算页面
     */
    private void sendParameter() {
        BillShowParameter showParameter = new BillShowParameter();
        showParameter.getOpenStyle().setShowType(ShowType.InCurrentForm); //替换当前页面内容
        showParameter.setFormId("lag1_homework_correct");
        showParameter.setCustomParam("problemList",questionObjects);
        showParameter.setCustomParam("ansList",userAnswers);
        showParameter.setCustomParam("prolistId",prolist_id);

        this.getView().showForm(showParameter);
    }

    private void loadAnswerData() {
        for(int i=0;i<pronoList.size();i++){
            if (i==0){
                String ans = (String) this.getModel().getValue(ANS_ONE);
                userAnswers.add(ans);
            }else if(i==1){
                String ans = (String) this.getModel().getValue(ANS_TWO);
                userAnswers.add(ans);
            }else if(i==2){
                String ans = (String) this.getModel().getValue(ANS_THREE);
                userAnswers.add(ans);
            }else if(i==3){
                String ans = (String) this.getModel().getValue(ANS_FOUR);
                userAnswers.add(ans);
            }
        }
    }

    /**
     * 题目跳转按钮点击事件处理
     */
    private void onQuestionJumpClick(EventObject e,int direction){
        getCurNo();
//        DynamicObject DO = this.getModel().getDataEntity();
//        currentQuestionIndex = Integer.parseInt(DO.getString(CUR_NO));
//        this.getView().showMessage(String.valueOf(pronoList.size()));
//        this.getView().showMessage(String.valueOf(currentQuestionIndex));
        if(direction==-1){
            currentQuestionIndex=Math.max(currentQuestionIndex-1,0);
            this.getModel().setValue(CUR_NO,currentQuestionIndex);
        }else if(direction==1){
            currentQuestionIndex=Math.min(currentQuestionIndex+1,questionObjects.size()-1);
            this.getModel().setValue(CUR_NO,currentQuestionIndex);
        }
        bindCurrentQuestionData();
        bindHasAnswerData();
    }

    /**
     * 获取题目数据
     * @param e
     */
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        //检查是否已经加载过数据
        if(questionObjects.isEmpty() || pronoList.isEmpty()){
            loadQuestionData();
        }
    }

    /**
     * 绑定当前作答数据到作答区域
     */
    private void bindHasAnswerData() {
        if (questionObjects.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questionObjects.size()) {
            return;
        }

        DynamicObject currentQuestion = questionObjects.get(currentQuestionIndex);
        String questionId = currentQuestion.getString("number"); // 假设billno是题目ID
        String questionType = currentQuestion.getString(PROTYPE);
        // 绑定作答(可选)
        if(questionType.equals("3")||questionType.equals("4")||questionType.equals("6")||questionType.equals("7")){
            String stuAns="";
            if(currentQuestionIndex==0){
                stuAns= (String) this.getModel().getValue(ANS_ONE);
            }else if(currentQuestionIndex==1){
                stuAns = (String) this.getModel().getValue(ANS_TWO);
            }else if(currentQuestionIndex==2){
                stuAns = (String) this.getModel().getValue(ANS_THREE);
            }else if(currentQuestionIndex==3){
                stuAns = (String) this.getModel().getValue(ANS_FOUR);
            }
            this.getModel().setValue(ANS_TXT,stuAns);
        }
    }

    /**
     * 绑定当前题目数据到表单
     */
    private void bindCurrentQuestionData() {
        if (questionObjects.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questionObjects.size()) {
            return;
        }

        DynamicObject currentQuestion = questionObjects.get(currentQuestionIndex);
        String questionId = currentQuestion.getString("number"); // 假设billno是题目ID
        String questionType = currentQuestion.getString(PROTYPE);

        if ("1".equals(questionType)) {
            hideAnsText();
            showButton();
            //单选题
            StringBuilder prodesBuilder = new StringBuilder();
            prodesBuilder.append(currentQuestion.getString("lag1_prodes"));
            prodesBuilder.append("\n\n");
            //添加选项A
            String itemA = currentQuestion.getString("lag1_itema");
            prodesBuilder.append("A. ").append(itemA).append("\n");

            //添加选项B
            String itemB = currentQuestion.getString("lag1_itemb");
            prodesBuilder.append("B. ").append(itemB).append("\n");

            //添加选项C
            String itemC = currentQuestion.getString("lag1_itemc");
            prodesBuilder.append("C. ").append(itemC).append("\n");

            //添加选项D
            String itemD = currentQuestion.getString("lag1_itemd");
            prodesBuilder.append("D. ").append(itemD).append("\n");

            String formattedProdes = prodesBuilder.toString();
            this.getModel().setValue("lag1_prodes",formattedProdes);

            // 绑定单选按钮字段(初始状态可以都未选中)
            this.getModel().setValue("lag1_radiofielda", false);
            this.getModel().setValue("lag1_radiofieldb", false);
            this.getModel().setValue("lag1_radiofieldc", false);
            this.getModel().setValue("lag1_radiofieldd", false);

//            // 恢复用户之前的选择(如果有)
//            if (userAnswers.containsKey(questionId)) {
//                String userAnswer = userAnswers.get(questionId);
//                // 根据用户答案设置对应的单选按钮
//                switch (userAnswer) {
//                    case "A":
//                        this.getModel().setValue("lag1_radiofielda", true);
//                        break;
//                    case "B":
//                        this.getModel().setValue("lag1_radiofieldb", true);
//                        break;
//                    case "C":
//                        this.getModel().setValue("lag1_radiofieldc", true);
//                        break;
//                    case "D":
//                        this.getModel().setValue("lag1_radiofieldd", true);
//                        break;
//                }
//            }
//            // 根据用户是否有答案控制uncom_pro/com_pro图片显示
//            for(int i=0;i<pronoList.size();i++){
//                updateAnswerStatusButtons(pronoList.get(i));
//            }
        }else if("2".equals(questionType)){
            //多选题

        }else if("3".equals(questionType)){
            //填空题
            showAnsText();
            hideButton();
            StringBuilder prodesBuilder = new StringBuilder();
            prodesBuilder.append(currentQuestion.getString("lag1_prodes"));
            String formattedProdes = prodesBuilder.toString();
            this.getModel().setValue("lag1_prodes",formattedProdes);
        }else if("4".equals(questionType)){
            //名词解释题
            showAnsText();
            hideButton();
            StringBuilder prodesBuilder = new StringBuilder();
            prodesBuilder.append(currentQuestion.getString("lag1_prodes"));
            String formattedProdes = prodesBuilder.toString();
            this.getModel().setValue("lag1_prodes",formattedProdes);
        }else if("5".equals(questionType)){
            //判断题
            showAnsText();
            hideButton();
        } else if("6".equals(questionType)){
            //简答题
            showAnsText();
            hideButton();
            StringBuilder prodesBuilder = new StringBuilder();
            prodesBuilder.append(currentQuestion.getString("lag1_prodes"));
            String formattedProdes = prodesBuilder.toString();
            this.getModel().setValue("lag1_prodes",formattedProdes);
        }else if("7".equals(questionType)){
            //应用题
            showAnsText();
            hideButton();
            StringBuilder prodesBuilder = new StringBuilder();
            prodesBuilder.append(currentQuestion.getString("lag1_prodes"));
            prodesBuilder.append("\n\n");
            //添加小问1
            String ask1 = currentQuestion.getString("lag1_textareafield1");
            if(ask1!="" || !ask1.isEmpty()){
                prodesBuilder.append("1. ").append(ask1).append("\n");
            }

            //添加小问2
            String ask2 = currentQuestion.getString("lag1_textareafield2");
            if(ask2!="" || !ask2.isEmpty()){
                prodesBuilder.append("2. ").append(ask2).append("\n");
            }

            //添加小问3
            String ask3 = currentQuestion.getString("lag1_textareafield3");
            if(ask3!="" || !ask3.isEmpty()){
                prodesBuilder.append("3. ").append(ask3).append("\n");
            }

            String formattedProdes = prodesBuilder.toString();
            this.getModel().setValue("lag1_prodes",formattedProdes);
        }

    }
//    private void updateAnswerStatusButtons(String questionId) {
//        if(questionId == null) return;
//
//        // 检查用户是否有答案
//        boolean hasAnswer = userAnswers.containsKey(questionId);
//
//        // 更新uncom_pro按钮显示(未完成状态)
//        if(hasAnswer){
//            this.getView().setVisible(false,UNCOM_PRO + (currentQuestionIndex + 1));
//        }else{
//            this.getView().setVisible(false,COM_PRO + (currentQuestionIndex + 1));
//        }
//    }

    /**
     * 更新导航按钮状态
     */
//    private void updateNavigationButtons() {
//        // 确保导航按钮数量与题目数量一致
//        int questionCount = questionObjects.size();
//
//        // 可以在这里禁用超出范围的导航按钮
//        // 例如，如果只有3道题，禁用第4题的导航按钮
//        for(int i=1; i<=4; i++) {
//            boolean visible = (i <= questionCount);
//            this.getModel().setValue(UNCOM_PRO + i, visible);
//            this.getModel().setValue(COM_PRO + i, visible);
//        }
//    }

    /**
     * 处理用户答题事件(单选题)
     */
//    public void onAnswerSelected(EventObject e) {
//        // 获取当前题目ID
//        if(questionObjects.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questionObjects.size()) {
//            return;
//        }
//        DynamicObject currentQuestion = questionObjects.get(currentQuestionIndex);
//        String questionId = currentQuestion.getString("billno");
//
//        // 获取用户选择的答案(需要根据实际表单设计获取)
//        String selectedAnswer = getSelectedAnswer();
//        if(selectedAnswer == null) return;
//
//        // 保存用户答案
//        userAnswers.put(questionId, selectedAnswer);
//
//        // 更新答案状态按钮显示
//        updateAnswerStatusButtons(questionId);
//
//        // 可以在这里添加自动跳转到下一题的逻辑(可选)
//        // if(currentQuestionIndex < questionObjects.size() - 1) {
//        //     currentQuestionIndex++;
//        //     bindCurrentQuestionData();
//        //     updateNavigationButtons();
//        // }
//    }

    /**
     * 获取用户选择的答案(A/B/C/D)
     */
    private String getSelectedAnswer() {
//        if(this.getModel().get("lag1_radiofielda") == true) return "A";
//        if(this.getView().getFieldValue("lag1_radiofieldb") == true) return "B";
//        if(this.getView().getFieldValue("lag1_radiofieldc") == true) return "C";
//        if(this.getView().getFieldValue("lag1_radiofieldd") == true) return "D";
        return null;
    }

    /**
     * 隐藏按钮
     */
    private void hideButton(){
        this.getView().setVisible(false,"lag1_radiofielda");
        this.getView().setVisible(false,"lag1_radiofieldb");
        this.getView().setVisible(false,"lag1_radiofieldc");
        this.getView().setVisible(false,"lag1_radiofieldd");
    }

    /**
     * 显示按钮
     */
    private void showButton(){
        this.getView().setVisible(true,"lag1_radiofielda");
        this.getView().setVisible(true,"lag1_radiofieldb");
        this.getView().setVisible(true,"lag1_radiofieldc");
        this.getView().setVisible(true,"lag1_radiofieldd");
    }

    /**
     * 隐藏富文本框
     */
    private void hideAnsText(){
        this.getView().setVisible(false,ANS_TXT);
    }

    /**
     * 隐藏富文本框
     */
    private void showAnsText(){
        this.getView().setVisible(true,ANS_TXT);
    }
}

