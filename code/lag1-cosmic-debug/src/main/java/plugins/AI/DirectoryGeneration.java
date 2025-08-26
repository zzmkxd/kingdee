package plugins.AI;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.Objects;

/**
 * 基础资料插件
 */
public class DirectoryGeneration extends AbstractBasePlugIn implements Plugin {
    @Override
    public void afterBindData(EventObject e) {
      super.afterBindData(e);
        JSONObject jsonResultObject = new JSONObject();
        for (int i = 1; i <= 12; i++) {
            String str1 = this.getModel().getValue("lag1_chapter" + i).toString();
            if(!Objects.equals(str1, "")){
                jsonResultObject.put("第" + i +"章", str1);
                for(int j=1;j<=5;j++){
                    String str2 = this.getModel().getValue("lag1_chapter" + i+"p"+j).toString();
                    if(!Objects.equals(str2, "")){
                        jsonResultObject.put("第" + i+"_"+j +"章", str2);
                    }
                }
            }
        }
        String llmValue2 = generateMarkdownMindMap(jsonResultObject);
    }
    // 获取GPT提示的Fid
    public long getPromptFid(String billNo) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("gai_prompt",
                "number," + "id",
                (new QFilter("number", QCP.equals, billNo)).toArray());
        return (dynamicObject).getLong("id");
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
}