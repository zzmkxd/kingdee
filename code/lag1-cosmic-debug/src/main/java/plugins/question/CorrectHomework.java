package plugins.question;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.sdk.plugin.Plugin;
import net.sf.json.JSON;
import org.apache.commons.lang3.StringUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * 基础资料插件
 */
public class CorrectHomework extends AbstractBasePlugIn implements Plugin {
   //作答列表
    private List<String> userAnswers = new ArrayList<>();

    private final String TOTproblems = "lag1_entryentity_pigai";           //单据体
    private final String USERans = "lag1_useranswer";           //作答记录
    private final String PROdes = "lag1_prodes";                //题干
    private final String PROdifficulty = "lag1_difficulty";     //题目难度
    private final String PROanswer = "lag1_standard_answer";        //标准答案
    private final String PROknowpoints = "lag1_link_kpoints";   //知识点
    private String prolist_id ="";
    private final String PROid = "lag1_proid";  //题目ID
    private final String PROtype = "lag1_protype";  //题目类型
    private final String COURSEid = "lag1_courseid";    //课程id
    private String AIORNOR = "";


    //所有题目列表
    private List<JSONObject> questionObjects = new ArrayList<>();

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        DynamicObject DO = this.getModel().getDataEntity();
        DynamicObjectCollection totProblems = DO.getDynamicObjectCollection(TOTproblems);
//        this.getView().showMessage("size"+totProblems.size());
        if(totProblems.size()==1 && totProblems.get(0).getString(PROid).isEmpty()){
//            this.getView().showMessage("tot为空");
            loadQuestionData();
        }else{
//            this.getView().showMessage(totProblems.get(0).getString(PROid));
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

    }

    /**
     * 加载数据
     */
    private void loadQuestionData() {
        AIORNOR = this.getView().getFormShowParameter().getCustomParam("aiornor");
        this.getModel().getDataEntity().set("lag1_aiornor",AIORNOR);
        if (AIORNOR.equals("normal")){
            questionObjects=this.getView().getFormShowParameter().getCustomParam("problemList");
//            this.getView().showMessage("qestioinObjects:"+questionObjects);
            userAnswers = this.getView().getFormShowParameter().getCustomParam("ansList");
            prolist_id = this.getView().getFormShowParameter().getCustomParam("prolistId");
            bindQuestionData();
        }else {
            userAnswers = this.getView().getFormShowParameter().getCustomParam("ansList");
            questionObjects=this.getView().getFormShowParameter().getCustomParam("problemList");
//            this.getView().showMessage("qo2:"+questionObjects);
//            Object raw = this.getView().getFormShowParameter().getCustomParam("problemList");
//
//            if (raw instanceof List) {
//                List<?> list = (List<?>) raw;
//                questionObjects.clear();
//                for (Object o : list) {
//                    if (o instanceof String) {
//                        questionObjects.add(JSONObject.parseObject((String) o));
//                    } else if (o instanceof JSONObject) {
//                        questionObjects.add((JSONObject) o);
//                    } else if (o instanceof DynamicObject) {
//                        DynamicObject dy = (DynamicObject) o;
//                        JSONObject json = new JSONObject();
//                        json.put("id", dy.getString("id"));
//                        json.put("question", dy.getString("question"));
//                        json.put("difficulty", dy.getString("difficulty"));
//                        json.put("answer", dy.getString("answer"));
//                        Object kp = dy.get("knowledge_points");
//                        if (kp instanceof List) {
//                            json.put("knowledge_points", new JSONArray((List<Object>) kp));
//                        } else {
//                            json.put("knowledge_points", new JSONArray());
//                        }
//                        questionObjects.add(json);
//                    } else {
//                        this.getView().showMessage("未知类型：" + o.getClass());
//                    }
//                }
//            }
            bindQuestionData2();
//            this.getView().showMessage(AIORNOR);
        }
//        this.getView().showMessage("userAnswers.size:"+userAnswers.size()+"\n"+"prolist_id:"+prolist_id);
    }

    private void bindQuestionData2() {
        DynamicObject DO = this.getModel().getDataEntity();
        //获取totproblems单据体集合
        DynamicObjectCollection totProblems = DO.getDynamicObjectCollection(TOTproblems);
        totProblems.clear();
        //绑定学生id
        DO.set("lag1_textfield", RequestContext.get().getCurrUserId());
        for(int i=0;i<userAnswers.size();i++){
            DynamicObject dynamicObjectEntry = totProblems.addNew();    //单据体条目
            String userAnswer = userAnswers.get(i);
            String userAnswer2="";
            if(userAnswer.equals("")) userAnswer2="未作答";
            else userAnswer2 = userAnswer;

            //题目遍历
            if(questionObjects.size()>i) {
                JSONObject proJsonObject = questionObjects.get(i);
//                this.getView().showMessage("proJsonObject:" + proJsonObject);
                // 先拿到 "id" 对象// 再从中提取 "string"
                JSONObject idObj = proJsonObject.getJSONObject("id");
                String proid = idObj.getString("string");

                JSONObject prodesObj = proJsonObject.getJSONObject("question");
                String prodes = prodesObj.getString("string");

                JSONObject prodiffiObj = proJsonObject.getJSONObject("difficulty");
                String prodiffi = prodiffiObj.getString("string");

                JSONObject proansObj = proJsonObject.getJSONObject("answer");
                String proans = proansObj.getString("string");

                dynamicObjectEntry.set(PROid, proid);    //题目id
                dynamicObjectEntry.set(PROdes, prodes);  //题干
                dynamicObjectEntry.set(PROdifficulty, prodiffi); //难度
                dynamicObjectEntry.set(USERans, userAnswer2);    //用户作答
                dynamicObjectEntry.set(PROanswer, proans);   //标准答案

                if (proJsonObject.containsKey("knowledge_points")) {

                    JSONArray entryEntityArray = proJsonObject.getJSONArray("knowledge_points");
                    StringBuilder knowledgePoints = new StringBuilder();

                    for (int j = 0; j < entryEntityArray.size(); j++) {
                        JSONObject item = entryEntityArray.getJSONObject(j);
                        String kp = item.getString("string");
                        if (knowledgePoints.length() > 0) knowledgePoints.append(",");
                        knowledgePoints.append(kp);
                    }

                    dynamicObjectEntry.set("lag1_link_kpoints", knowledgePoints.toString());
                } else {
                    dynamicObjectEntry.set("lag1_link_kpoints", "未绑定知识点");
                }
            }
        }
    }

    /**
     * 绑定数据
     */
    private void bindQuestionData() {
        this.getModel().getDataEntity().set("number",prolist_id);
        DynamicObject DO = this.getModel().getDataEntity();
        //获取totproblems单据体集合
        DynamicObjectCollection totProblems = DO.getDynamicObjectCollection(TOTproblems);
        totProblems.clear();
        //绑定学生id
        DO.set("lag1_textfield", RequestContext.get().getCurrUserId());
        for(int i=0;i<userAnswers.size();i++){
            DynamicObject dynamicObjectEntry = totProblems.addNew();    //单据体条目
            String userAnswer = userAnswers.get(i);
            String userAnswer2="";
            if(userAnswer.equals("")) userAnswer2="未作答";
            else userAnswer2 = userAnswer;

            //题目遍历
            if(questionObjects.size()>i){
                JSONObject proJsonObject = questionObjects.get(i);
//                this.getView().showMessage("proJsonObject:"+proJsonObject);
                String proid = proJsonObject.getString("number");   //题目id
                String courseid = proJsonObject.getString("lag1_courseidtxt");  //课程id
                String protypeid = proJsonObject.getString("lag1_questiontype");
                String protype = "";
                if(protypeid.equals("1")) protype="单选题";
                if (protypeid.equals("2")) protype="多选题";
                if (protypeid.equals("3")) protype="填空题";
                if (protypeid.equals("4")) protype="名词解释题";
                if (protypeid.equals("5")) protype="判断题";
                if (protypeid.equals("6")) protype="简答题";
                if (protypeid.equals("7")) protype="应用题";


                String prodes = proJsonObject.getString("lag1_prodes");
                String prodiffi = proJsonObject.getString("lag1_difficulty");
//                this.getView().showMessage(prodiffi);
//                String difficultyDes = "";
//                if(prodiffi.equals("1")) difficultyDes="简单";
//                else if(prodiffi.equals("2")) difficultyDes="基础";
//                else if(prodiffi.equals("3")) difficultyDes="中等";
//                else if(prodiffi.equals("4")) difficultyDes="困难";
//                this.getView().showMessage("diffi "+difficultyDes);

                String proans = proJsonObject.getString("lag1_standard_answer");

                dynamicObjectEntry.set(PROid,proid);    //题目id
                dynamicObjectEntry.set(PROtype,protype);    //题目类型
                dynamicObjectEntry.set(COURSEid,courseid);  //课程id
                dynamicObjectEntry.set(PROdes,prodes);  //题干
                dynamicObjectEntry.set(PROdifficulty,prodiffi); //难度
                dynamicObjectEntry.set(USERans,userAnswer2);    //用户作答
                dynamicObjectEntry.set(PROanswer,proans);   //标准答案

                if(proJsonObject.containsKey("lag1_entryentity_linkp")){
                    JSONArray entryEntityArray=proJsonObject.getJSONArray("lag1_entryentity_linkp");
                    StringBuilder knowledgePoints = new StringBuilder();
                    if(entryEntityArray!=null && !entryEntityArray.isEmpty()){
                        for(int j=0;j<entryEntityArray.size();j++){
                            JSONObject entryEntity = entryEntityArray.getJSONObject(j);
                            if(entryEntity.containsKey("lag1_knpoint1") && entryEntity.get("lag1_knpoint1") != null){
                                JSONObject baseDataField2 = entryEntity.getJSONObject("lag1_knpoint1");
                                if(baseDataField2.containsKey("name")){
                                    JSONObject name = baseDataField2.getJSONObject("name");
                                    if(name.containsKey("zh_CN")){
                                        if(knowledgePoints.length()>0){
                                            knowledgePoints.append(",");
                                        }
                                        knowledgePoints.append(name.getString("zh_CN"));
                                    }
                                }
                            }
                            if(entryEntity.containsKey("lag1_knpoint2") && entryEntity.get("lag1_knpoint2") != null){
                                JSONObject baseDataField2 = entryEntity.getJSONObject("lag1_knpoint2");
                                if(baseDataField2.containsKey("name")){
                                    JSONObject name = baseDataField2.getJSONObject("name");
                                    if(name.containsKey("zh_CN")){
                                        if(knowledgePoints.length()>0){
                                            knowledgePoints.append(",");
                                        }
                                        knowledgePoints.append(name.getString("zh_CN"));
                                    }
                                }
                            }
                        }
                    }
                    if(knowledgePoints.length()==0){
                        dynamicObjectEntry.set("lag1_link_kpoints","未绑定知识点");
                    }else{
                        dynamicObjectEntry.set("lag1_link_kpoints",knowledgePoints.toString());
                    }
                }else{
                    dynamicObjectEntry.set("lag1_link_kpoints","未绑定知识点");
                }
//                this.getView().showMessage("dynamicObjectEntry:"+dynamicObjectEntry);
            }

        }
    }


}