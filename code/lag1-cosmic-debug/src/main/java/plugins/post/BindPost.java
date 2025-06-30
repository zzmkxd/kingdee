package plugins.post;

import kd.bos.bill.BillShowParameter;
import kd.bos.cache.CacheFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.property.ComboProp;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.*;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.control.Button;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;

import static kd.bos.cache.ehcache.syncstatus.MemoryCacheSyncStatusMange.getUid;

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

        // 先通过getControl来控制对应的控件
        Button re_btn = this.getView().getControl("lag1_rebtn");
        // 再给这个控件加上监听功能
        re_btn.addClickListener(this);
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
                postNumber = this.getView().getFormShowParameter().getCustomParam("postNumber");
                if(StringUtils.isNotBlank(postNumber))
                    postData = queryPostData(postNumber);

                int thumb_num = postData.getInt("lag1_thumb");
                thumb_num+=1;
                postData.set("lag1_thumb",thumb_num);
                SaveServiceHelper.update(postData); // 这一步至关重要！
                this.getModel().setValue("lag1_thumb",thumb_num);
            } else if(StringUtils.equals("lag1_makecomment",key)){
                //打开评论基础资料
                BillShowParameter nxtList = new BillShowParameter();
                nxtList.setFormId("lag1_commentlist");  //基础资料列表的标准表单ID

                //设置打开方式为模态窗口
                nxtList.getOpenStyle().setShowType(ShowType.Modal);
                postNumber = this.getView().getFormShowParameter().getCustomParam("postNumber");
                if(StringUtils.isNotBlank(postNumber)) nxtList.setCustomParam("postNumber",postNumber);

                //设置宽高
                StyleCss styleCss = new StyleCss();
                styleCss.setHeight("600");
                styleCss.setWidth("1200");
                nxtList.getOpenStyle().setInlineStyleCss(styleCss);
//                nxtList.setCloseCallBack(new CloseCallBack(this,"recnum"));

                this.getView().showForm(nxtList);
            }else if(StringUtils.equals("lag1_rebtn",key)){
                IFormView parentView = this.getView().getParentView();
                parentView.invokeOperation("refresh");
                this.getView().sendFormAction(parentView);
                this.getView().close();
            }
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
//        这里是接收参数端在beforeBIndData里面开始先接受传过来的参数
        postNumber = this.getView().getFormShowParameter().getCustomParam("postNumber");        //这里的getCustomParam里面的标识就是在上个页面传参的时候设置的
        pkId = this.getView().getFormShowParameter().getCustomParam("pkId");        //这样就可以获取到传过来的参数
        if(StringUtils.isNotBlank(postNumber)){
            this.getModel().setValue("lag1_postnumber",postNumber);
            //查询基础资料
            try{
                postData = queryPostData(postNumber);
                if(postData!=null){
                    //绑定富文本内容
                    String content = postData.getString("lag1_content");
                    this.getModel().setValue("lag1_content",content);

                    //所属板块,多选下拉列表取值，赋值到文本字段
                    String field = postData.getString("lag1_field");
                    ComboProp comboProp = (ComboProp) postData.getDataEntityType().getProperties().get("lag1_field");
                    String fieldName = comboProp.getItemByName(field);
                    this.getModel().setValue("lag1_textfield", fieldName);

                    //发帖时间
                    String createTime = postData.getString("createtime");
                    this.getModel().setValue("lag1_date",createTime);
//                    然后像这里一样，定位到这个控件，就可以直接绑定数据了
//                    你懂了吗okok 原来你实现的这个功能要两个插件 好像是吧，因为绑定在不同的页面，我不知道一个可不可以

                    //发帖人
                    DynamicObject creator = (DynamicObject) postData.get("creator");
                    String creatorName = creator.getString("name");
                    this.getModel().setValue("lag1_creator", creatorName);

                    //标题
                    String postTitle = postData.getString("name");
                    this.getModel().setValue("lag1_posttitle",postTitle);

                    //
                    int viewNum = postData.getInt("lag1_view");
                    this.getModel().setValue("lag1_view",viewNum);

                    int thumbNum = postData.getInt("lag1_thumb");
                    this.getModel().setValue("lag1_thumb",thumbNum);

                    int commentNum = postData.getInt("lag1_comment");
                    this.getModel().setValue("lag1_comment", commentNum);

                    bindAttachments(postData);  //绑定附件数据
                }else{
                    this.getView().showMessage("没有找到帖子数据");
                }
            }catch (Exception ex){
                this.getView().showMessage(ex.getMessage());
            }
        }else{
            this.getView().showMessage("绑定数据错误");
        }
    }

    private void bindAttachments(DynamicObject postData) {
        //获取源附件字段的值
        DynamicObjectCollection sourceAttachCol3 = (DynamicObjectCollection) postData.get("lag1_attachmentfield");
        List<Long> attchIdSet = new ArrayList<>();
        sourceAttachCol3.forEach(attach -> {
            attchIdSet.add(attach.getDynamicObject("fbasedataId").getLong("id"));
        });
        this.getModel().setValue("lag1_attachmentfield",attchIdSet.toArray());
    }


    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        //绑定评论数据
        String postId = this.getModel().getValue("lag1_postnumber").toString();
        //查询对应帖子的评论
        if(postId!= null && !postId.isEmpty()){
            //设置过滤条件
            setCommentListFilter(postId);
        }
    }

    private void setCommentListFilter(String postId) {
        BillList commentList = this.getView().getControl("lag1_billlistap");
        if(commentList!=null){
            QFilter filter = new QFilter("lag1_postnumber",QCP.equals,postId);
            commentList.setFilter(filter);
            commentList.refresh();  //重新刷新才能生效
        }else{
            this.getView().showMessage("commentList未找到");
        }
    }

    /**
     * 寻帖子基础资料
     * @param postNumber
     * @return 帖子数据对象
     */
    private DynamicObject queryPostData(String postNumber){
        return BusinessDataServiceHelper.loadSingle("lag1_post",new QFilter[]{new QFilter("number", QCP.equals,postNumber)});
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        IDataModel dataModel = this.getView().getModel();   //获取表单数据模型
        //遍历所有字段并设置为只读
        for(IDataEntityProperty property : dataModel.getDataEntityType().getProperties()){
            String fieldName = property.getName();
//            this.getView().setVisible(false,fieldName);
            this.getView().setEnable(false,fieldName);
        }
    }
}