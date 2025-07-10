package plugins.question;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.sdk.plugin.Plugin;
import net.sf.json.JSON;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * 基础资料插件
 */
public class CorrectHomework extends AbstractBasePlugIn implements Plugin {
   //作答列表
    private List<String> userAnswers = new ArrayList<>();

    private final String TOTproblems = "lag1_entryentity_problms";           //单据体
    private final String USERans = "lag1_useranswer";           //作答记录
    private final String PROdes = "lag1_prodes";                //题干
    private final String PROdifficulty = "lag1_difficulty";     //题目难度
    private final String PROanswer = "lag1_standard_answer";        //标准答案
    private final String PROknowpoints = "lag1_link_kpoints";   //知识点
    private String prolist_id ="";

    //所有题目列表
    private List<JSONObject> questionObjects = new ArrayList<>();
    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        if(questionObjects.isEmpty() || userAnswers.isEmpty()){
            loadQuestionData();
            bindQuestionData();
        }
    }

    /**
     * 加载数据
     */
    private void loadQuestionData() {
        questionObjects=this.getView().getFormShowParameter().getCustomParam("problemList");
        userAnswers = this.getView().getFormShowParameter().getCustomParam("ansList");
        prolist_id = this.getView().getFormShowParameter().getCustomParam("prolistId");
//        this.getView().showMessage(String.valueOf(questionObjects.get(0)));
    }

    /**
     * 绑定数据
     */
    private void bindQuestionData() {
        this.getModel().getDataEntity().set("lag1_textfield2",prolist_id);
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
            dynamicObjectEntry.set(USERans,userAnswer2);

            //题目遍历
            if(questionObjects.size()>i){
                JSONObject proJsonObject = questionObjects.get(i);
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
                dynamicObjectEntry.set(PROdes,prodes);
                dynamicObjectEntry.set(PROdifficulty,prodiffi);
                dynamicObjectEntry.set(PROanswer,proans);

                if(proJsonObject.containsKey("lag1_entryentity")){
                    JSONArray entryEntityArray=proJsonObject.getJSONArray("lag1_entryentity");
                    StringBuilder knowledgePoints = new StringBuilder();
                    if(entryEntityArray!=null && !entryEntityArray.isEmpty()){
                        for(int j=0;j<entryEntityArray.size();j++){
                            JSONObject entryEntity = entryEntityArray.getJSONObject(j);
                            if(entryEntity.containsKey("lag1_basedatafield2")){
                                JSONObject baseDataField2 = entryEntity.getJSONObject("lag1_basedatafield2");
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
                            if(entryEntity.containsKey("lag1_basedatafield1")){
                                JSONObject baseDataField2 = entryEntity.getJSONObject("lag1_basedatafield1");
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
            }

        }
    }


}