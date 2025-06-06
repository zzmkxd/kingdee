package plugins.post;

import dm.jdbc.util.StringUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.control.Button;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 动态表单插件
 *
 * beforeBindData
 * 1根据postNumber查询基础资料
 * 2绑定查询结果到表单
 * 3处理富文版和附件绑定
 */
public class BindPost extends AbstractFormPlugin implements Plugin {

    //全局变量
    String postNumber;
    Long pkId;
    DynamicObject postData;


    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        // 先通过getControl来控制对应的控件
        Button thumb_btn = this.getView().getControl("lag1_makethumb");
        // 再给这个控件加上监听功能
        thumb_btn.addClickListener(this);

        // 先通过getControl来控制对应的控件
        Button comment_btn = this.getView().getControl("lag1_makecomment");
        // 再给这个控件加上监听功能
        comment_btn.addClickListener(this);
//        this.addClickListeners("lag1_make_thumb");
//        this.addItemClickListeners("lag1_make_comment");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        // 获取被点击的控件对象
        Object source = evt.getSource();
        if(source instanceof Button){
            Button button = (Button)source;
            String key = button.getKey();
            if(StringUtils.equals("lag1_makethumb",key)){
                //增加点赞数
//                postData = queryPostData(postNumber);
//                if (postData == null) {
//                    this.getView().showMessage("帖子数据为空，无法点赞");
//                    return;
//                }
                postNumber = this.getView().getFormShowParameter().getCustomParam("postNumber");
                if(StringUtils.isNotBlank(postNumber))
                        postData = queryPostData(postNumber);

                int thumb_num = postData.getInt("lag1_thumb");
                thumb_num+=1;
                postData.set("lag1_thumb",thumb_num);
                SaveServiceHelper.update(postData); // 这一步至关重要！
                this.getModel().setValue("lag1_thumb",thumb_num);
            }else if(StringUtil.equals("lag1_makecomment",key)){
                //打开评论基础资料
                FormShowParameter nxtList = new FormShowParameter();
                nxtList.setFormId("lag1_commentlist");

                //设置宽高
                StyleCss styleCss = new StyleCss();
                styleCss.setHeight("600");
                styleCss.setWidth("1200");
                nxtList.getOpenStyle().setInlineStyleCss(styleCss);

                nxtList.getOpenStyle().setShowType(ShowType.Modal);
                this.getView().showForm(nxtList);
            }
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        postNumber = this.getView().getFormShowParameter().getCustomParam("postNumber");
        pkId = this.getView().getFormShowParameter().getCustomParam("pkId");
        if(StringUtils.isNotBlank(postNumber)){
            this.getModel().setValue("lag1_postnumber",postNumber);
            //查询基础资料
            try{
                postData = queryPostData(postNumber);
                if(postData!=null){
                    //绑定富文本内容
                    String content = postData.getString("lag1_content");
                    this.getModel().setValue("lag1_content",content);

                    //所属板块
                    String field = postData.getString("lag1_field");
                    this.getModel().setValue("lag1_textfield", field);

                    //发帖时间
                    String createTime = postData.getString("createtime");
                    this.getModel().setValue("lag1_date",createTime);

                    //发帖人
                    String creator = postData.getString("creator");
                    this.getModel().setValue("lag1_creator", creator);

                    //
                    int viewNum = postData.getInt("lag1_view");
                    this.getModel().setValue("lag1_view",viewNum);

                    int thumbNum = postData.getInt("lag1_thumb");
                    this.getModel().setValue("lag1_thumb",thumbNum);

                    int commentNum = postData.getInt("lag1_comment");
                    this.getModel().setValue("lag1_comment", commentNum);

                    //详情页面附件面板
                    AttachmentPanel attachmentPanel = this.getControl("lag1_attachmentpanelap");

                    //绑定附件
                   DynamicObjectCollection attachmentField = (DynamicObjectCollection) postData.get("lag1_attachmentfield");
                   for(DynamicObject attachment : attachmentField){
                       DynamicObject fbasedataId = attachment.getDynamicObject("fbasedataId");
                       long id = fbasedataId.getLong("id");
                       String url = (String) fbasedataId.get("url");
                       System.out.println("附件字段"+id+url);
                   }

                }else{
                    this.getView().showMessage("没有找到帖子数据");
                }
            }catch (Exception ex){
                this.getView().showMessage("查询帖子数据出错");
            }
        }else{
            this.getView().showMessage("绑定数据错误");
        }
    }


    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
    }



    /**
     * 嘻哈寻帖子基础资料
     * @param postNumber
     * @return 帖子数据对象
     */
    private DynamicObject queryPostData(String postNumber){
        return BusinessDataServiceHelper.loadSingle("lag1_post",new QFilter[]{new QFilter("number", QCP.equals,postNumber)});
    }
}