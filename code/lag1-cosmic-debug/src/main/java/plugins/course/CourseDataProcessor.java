package plugins.course;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 基础资料插件
 */
public class CourseDataProcessor extends AbstractBasePlugIn implements Plugin {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Button mind_btn = this.getView().getControl("lag1_buttonap");
        mind_btn.addClickListener(this);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        if(StringUtils.equals("lag1_buttonap",source.getKey())){
//            DynamicObject formData = this.getModel().getDataEntity();
//            String bookName = null;
//
//            if(formData.getString("name")!=null || !formData.getString("name").isEmpty()){
//                bookName = formData.getString("name");
//            }
//
//            int nodeIdCounter=1;
//
//            //1构建节点列表
//            List<Map<String,Object>> nodes = new ArrayList<>();
//            List<Map<String,Object>> links = new ArrayList<>();
//
//            //添加根节点
//            String rootId = String.valueOf(nodeIdCounter);
//            nodeIdCounter++;
//            nodes.add(createNode(rootId,bookName,1,null,false,"教材根节点"));
//
//            //存储大章节ID，便于小章节引用
//            Map<Integer,String> chapterIdMap = new HashMap<>();
//
//            //动态获取大章节1-15
//            for(int i=1;i<=15;i++){
//                String chapterField = "lag1_chapter"+i;
//                String chapterName = formData.getString(chapterField);
//                if(chapterName!=null && !chapterName.isEmpty()){
//                    //添加大章节节点
//                    String chapterId = String.valueOf(nodeIdCounter++);
//                    chapterIdMap.put(i,chapterId);  //章节序号与ID的映射
//                    nodes.add(createNode(chapterId,chapterName,2,rootId,false,"大章节节点"));
//                    links.add(createLink(rootId,chapterId,0.7));
//
//                    //动态获取小章节
//                    if(i<=2){
//                        for(int j=1;j<=5;j++){
//                            String subChapterField = "lag1_chapter"+i+"p"+j;
//                            String subChapterName = formData.getString(subChapterField);
//                            if(subChapterName!=null && !subChapterName.isEmpty()){
//                                String subChapterId = String.valueOf(nodeIdCounter++);
//                                nodes.add(createNode(subChapterId,subChapterName,3,chapterId,false,"小章节节点"));
//                                links.add(createLink(chapterId,subChapterId,0.5));
//                            }
//                        }
//                    }
//
//                }
//            }
//
//            //构建最后JSON
//            JSONObject courseJson = new JSONObject();
//            courseJson.put("nodes",nodes);
//            courseJson.put("links",links);
//
//            System.out.println("json"+courseJson.toJSONString());
//            this.getView().returnDataToParent(courseJson.toJSONString());

//            this.getView().showMessage(courseJson.toJSONString());

            // 创建页面跳转参数
//            FormShowParameter parameter = new FormShowParameter();
//            parameter.getOpenStyle().setShowType(ShowType.Modal);
//            parameter.setFormId("lag1_bookcontent");

// 将JSON字符串放入参数中
//            parameter.setCustomParam("treeData", courseJson.toJSONString());

//            String[] markdownDataArray = new String[]{
//                    "# 默认内容\n" +
//                            "\n" +
//                            "1. 标题1\n" +
//                            " - 子标题1\n" +
//                            " - 子标题2\n" +
//                            "3. 标题2\n" +
//                            "4. 标题3\n" +
//                            "- beautiful\n" +
//                            "- useful\n" +
//                            "- easy\n" +
//                            "- interactive"
//            };
//            CustomControl customControl = this.getView().getControl("lag1_customcontrolap");
//            customControl.setData(markdownDataArray);
            this.getView().setVisible(true,"lag1_flexpanelap");

//            parameter.setCustomParam("treeData", markdownDataArray);
//// 打开目标页面
//            this.getView().showForm(parameter);
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        this.getView().setVisible(false,"lag1_flexpanelap");
//        String[] markdownDataArray = new String[]{
//                "# 默认内容\n" +
//                        "\n" +
//                        "1. 标题1\n" +
//                        " - 子标题1\n" +
//                        " - 子标题2\n" +
//                        "3. 标题2\n" +
//                        "4. 标题3\n" +
//                        "- beautiful\n" +
//                        "- useful\n" +
//                        "- easy\n" +
//                        "- interactive"
//        };
//        CustomControl customControl = this.getView().getControl("lag1_customcontrolap");
//        customControl.setData(markdownDataArray);
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        JSONObject jsonResultObject = new JSONObject();
        for (int i = 1; i <= 15; i++) {
            String tem1 = this.getModel().getValue("lag1_chapter" + i).toString();
            if(tem1==null || !tem1.equals("")){
                jsonResultObject.put("lag1_chapter" + i , tem1);
            }

            if(i<16){
                for(int j=1;j<=5;j++){
                    String tem2=this.getModel().getValue("lag1_chapter" + i+"p"+j).toString();
                    if(tem2==null || !tem1.equals("")){
                        jsonResultObject.put("lag1_chapter" + i+"p"+j, tem2);
                    }
                }
            }


        }//目前没有对小标题分析
        //----下面是正常提取教材代码----
        // 调用GPT开发平台微服务
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("courseinfo", jsonResultObject.toJSONString());
        Object[] params = new Object[]{
                //GPT提示编码
                getPromptFid("prompt-250708D5B99E7D"),
                "开始分析这些知识点",
                variableMap
        };
        Map<String, Object> result = DispatchServiceHelper.invokeBizService("ai", "gai", "GaiPromptService", "syncCall", params);

        JSONObject jsonObjectResult2 = new JSONObject(result);
        JSONObject jsonObjectData2 = jsonObjectResult2.getJSONObject("data");//微服务的输出，即代填入单据体的知识点JSON及正常微服务输出的各个键值对

//      this.getView().showMessage(jsonObjectData2.getString("llmValue"));

        String llmValue2 = jsonObjectData2.getString("llmValue");//代填入单据体的知识点JSON}
        String proMarkdown = processMarkdown(llmValue2);
        String[] markdownDataArray = new String[]{
                proMarkdown
        };

        CustomControl customControl = this.getView().getControl("lag1_customcontrolap");
        customControl.setData(markdownDataArray);
    }

    private String processMarkdown(String llmValue2) {
        // 去除开头的 ```markdown
        String noStart = llmValue2.startsWith("```markdown") ? llmValue2.substring(11).trim() : llmValue2.trim();

        // 找到注释部分的起始位置
        int commentStart = noStart.indexOf("```");
        if (commentStart != -1) {
            // 截取注释前的内容
            return noStart.substring(0, commentStart).trim();
        }

        return noStart;
    }

    // 获取GPT提示的Fid
    public long getPromptFid(String billNo) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("gai_prompt",
                "number," + "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        return (dynamicObject).getLong("id");
    }

    @Override
    public void afterLoadData(EventObject e) {
        super.afterLoadData(e);
    }

    private Map<String,Object> createNode(String id, String name, int group, String parentId, boolean expanded, String desc){
        Map<String, Object> node = new HashMap<>();
        node.put("id", id);
        node.put("name", name);
        node.put("group", group);
        node.put("expanded", expanded);
        node.put("description", desc);
        if(parentId!=null){
            node.put("parent",parentId);
        }
        return node;
    }

    // 创建连接辅助方法
    private Map<String, Object> createLink(String source, String target, double strength) {
        Map<String, Object> link = new HashMap<>();
        link.put("source", source);
        link.put("target", target);
        link.put("strength", strength);
        return link;
    }
}