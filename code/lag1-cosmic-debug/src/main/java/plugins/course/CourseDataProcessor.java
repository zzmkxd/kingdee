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
            this.getView().setVisible(true,"lag1_flexpanelap");
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        this.getView().setVisible(false,"lag1_flexpanelap");
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        JSONObject jsonResultObject = new JSONObject();
        for (int i = 1; i <= 12; i++) {
            String str1 = this.getModel().getValue("lag1_chapter" + i).toString();
            if(str1 != null){
                jsonResultObject.put("第" + i +"章", str1);
                for(int j=1;j<=5;j++){
                    String str2 = this.getModel().getValue("lag1_chapter" + i+"p"+j).toString();
                    if(str2 != null){
                        jsonResultObject.put("第" + i+"_"+j +"章", str2);
                    }
                }
            }
        }
        String llmValue2 = generateMarkdownMindMap(jsonResultObject);
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
    public String generateMarkdownMindMap(JSONObject jsonResultObject) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# 目录结构\n\n");  // 添加标题

        // 遍历所有章节（第一级）
        for (int i = 1; i <= 12; i++) {
            String chapterKey = "第" + i + "章";
            if (jsonResultObject.containsKey(chapterKey)) {
                // 添加父节点（标注"第X章"）
                markdown.append("- **第" + i + "章**: ").append(jsonResultObject.getString(chapterKey)).append("\n");
                // 遍历子章节（第二级）
                for (int j = 1; j <= 5; j++) {
                    String subChapterKey = "第" + i + "_" + j + "章";
                    if (jsonResultObject.containsKey(subChapterKey)) {
                        // 添加子节点（标注"第X.Y章"，缩进表示层级）
                        markdown.append("  - **第" + i + "." + j + "章**: ").append(jsonResultObject.getString(subChapterKey)).append("\n");
                    }
                }
            }
        }
        return markdown.toString();
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