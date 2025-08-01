package plugins.learncloud;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import javax.json.Json;
import java.util.EventObject;

/**
 * 基础资料插件
 */
public class SendParam extends AbstractBasePlugIn implements Plugin {
    private static final String JSON_DATA = "[{\"id\":1,\"name\":\"测试888\",\"description\":\"讨论项目进度和问题\",\"difficulty\":2,\"knowledgePoints\":[\"沟通技巧\",\"项目管理\"],\"start\":\"2025-07-10\",\"end\":\"2025-07-10\",\"color\":\"blue\"}," +
            "{\"id\":2,\"name\":\"测试2\",\"description\":\"完成所有功能开发并提交\",\"difficulty\":4,\"knowledgePoints\":[\"代码审查\",\"测试\"],\"start\":\"2025-07-12\",\"end\":\"2025-07-12\",\"color\":\"red\"}," +
            "{\"id\":3,\"name\":\"测试3\",\"description\":\"向客户展示项目成果\",\"difficulty\":3,\"knowledgePoints\":[\"演示技巧\",\"产品知识\"],\"start\":\"2025-07-15\",\"end\":\"2025-07-17\",\"color\":\"green\"}]";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Button calendar_btn = this.getView().getControl("lag1_buttonap");
        calendar_btn.addClickListener(this);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        if(StringUtils.equals("lag1_buttonap",source.getKey())) {
            // 创建页面跳转参数
            FormShowParameter parameter = new FormShowParameter();
            parameter.getOpenStyle().setShowType(ShowType.Modal);
            parameter.setFormId("lag1_calendar");

            //设置宽高
            StyleCss styleCss = new StyleCss();
            styleCss.setHeight("600");
            styleCss.setWidth("1200");

            String[] str=new String[]{
                    (this.getModel().getValue("lag1_textfield")).toString()
            };

            parameter.setCustomParam("treeData",str);
//            parameter.setInlineStyleCss(styleCss);
            this.getView().showForm(parameter);
        }
    }
}