package plugins.course;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
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
            DynamicObject formData = this.getModel().getDataEntity();
            String bookName = null;

            if(formData.getString("name")!=null || !formData.getString("name").isEmpty()){
                bookName = formData.getString("name");
            }

            int nodeIdCounter=1;

            //1构建节点列表
            List<Map<String,Object>> nodes = new ArrayList<>();
            List<Map<String,Object>> links = new ArrayList<>();

            //添加根节点
            String rootId = String.valueOf(nodeIdCounter);
            nodeIdCounter++;
            nodes.add(createNode(rootId,bookName,1,null,false,"教材根节点"));

            //存储大章节ID，便于小章节引用
            Map<Integer,String> chapterIdMap = new HashMap<>();

            //动态获取大章节1-15
            for(int i=1;i<=15;i++){
                String chapterField = "lag1_chapter"+i;
                String chapterName = formData.getString(chapterField);
                if(chapterName!=null && !chapterName.isEmpty()){
                    //添加大章节节点
                    String chapterId = String.valueOf(nodeIdCounter++);
                    chapterIdMap.put(i,chapterId);  //章节序号与ID的映射
                    nodes.add(createNode(chapterId,chapterName,2,rootId,false,"大章节节点"));
                    links.add(createLink(rootId,chapterId,0.7));

                    //动态获取小章节
                    if(i<=2){
                        for(int j=1;j<=5;j++){
                            String subChapterField = "lag1_chapter"+i+"p"+j;
                            String subChapterName = formData.getString(subChapterField);
                            if(subChapterName!=null && !subChapterName.isEmpty()){
                                String subChapterId = String.valueOf(nodeIdCounter++);
                                nodes.add(createNode(subChapterId,subChapterName,3,chapterId,false,"小章节节点"));
                                links.add(createLink(chapterId,subChapterId,0.5));
                            }
                        }
                    }

                }
            }

            //构建最后JSON
            JSONObject courseJson = new JSONObject();
            courseJson.put("nodes",nodes);
            courseJson.put("links",links);

            System.out.println("json"+courseJson.toJSONString());
            this.getView().returnDataToParent(courseJson.toJSONString());
//            this.getView().showMessage(courseJson.toJSONString());

            // 创建页面跳转参数
            FormShowParameter parameter = new FormShowParameter();
            parameter.getOpenStyle().setShowType(ShowType.Modal);
            parameter.setFormId("lag1_knowpointmap");

// 将JSON字符串放入参数中
            parameter.setCustomParam("treeData", courseJson.toJSONString());
// 打开目标页面
            this.getView().showForm(parameter);
        }
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