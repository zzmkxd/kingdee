package plugins.knowpoint;

import kd.bos.base.BaseShowParameter;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.property.ComboProp;
import kd.bos.ext.form.control.Markdown;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Button;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * 动态表单插件
 */
public class Bindbooktoknowpoint extends AbstractFormPlugin implements Plugin {

        //全局变量
        String postNumber;
        Long pkId;
        DynamicObject postData;


        @Override
        public void registerListener(EventObject e) {
            super.registerListener(e);
            // 注册点击事件
            this.addItemClickListeners("tbmain");

//            // 先通过getControl来控制对应的控件
//            Button thumb_btn = this.getView().getControl("lag1_pointbindcourse");
//            // 再给这个控件加上监听功能
//            thumb_btn.addClickListener(this);

        }

        @Override
        public void click(EventObject evt) {
            super.click(evt);
            // 获取被点击的控件对象
            Object source = evt.getSource();
            if(source instanceof Button){
                Button button = (Button)source;
                String key = button.getKey();
                if(StringUtils.equals("lag1_pointbindcourse",key)){
                    //打开知识点列表
                    // 打开基础资料列表的标准方式
                    ListShowParameter listShowParameter = new ListShowParameter();
                    listShowParameter.setFormId("bos_list"); // 固定值，基础资料列表模板
                    listShowParameter.setBillFormId("lag1_knowpoint"); // 基础资料标识

                    //设置打开方式为模态窗口
                    listShowParameter.getOpenStyle().setShowType(ShowType.Modal);
//
                    //设置宽高
                    StyleCss styleCss = new StyleCss();
                    styleCss.setHeight("600");
                    styleCss.setWidth("1200");
                    listShowParameter.getOpenStyle().setInlineStyleCss(styleCss);

                    this.getView().showForm(listShowParameter);

                    //录入知识点到列表
                }
            }
        }


        //这部分才是接收知识点分析情况部分功能
        @Override
        public void beforeBindData(EventObject e) {
            super.beforeBindData(e);
//        这里是接收参数端在beforeBIndData里面开始先接受传过来的参数
            postNumber = this.getView().getFormShowParameter().getCustomParam("postNumber");        //这里的getCustomParam里面的标识就是在上个页面传参的时候设置的
            pkId = this.getView().getFormShowParameter().getCustomParam("pkId");        //这样就可以获取到传过来的参数
            if(StringUtils.isNotBlank(postNumber)){
//                this.getModel().setValue("lag1_postnumber",postNumber);
                //查询基础资料
                    postData = queryPostData(postNumber);
                    if(postData!=null){
                        //绑定文本内容


//                        //发帖人-->教师
//                        DynamicObject creator = (DynamicObject) postData.get("creator");
//                        String creatorName = creator.getString("name");
//                        this.getModel().setValue("lag1_creator", creatorName);


                        //课程名
                        String postTitle = postData.getString("lag1_coursename");
                        this.getModel().setValue("lag1_coursename", postTitle);

                        //课程id
                        String courseid = postData.getString("lag1_textfield");
                        this.getModel().setValue("lag1_courseid",courseid);

                        //教材id
                        String bookid = postData.getString("number");
                        this.getModel().setValue("lag1_bookid",bookid);

                        //课程名
                        String bookname = postData.getString("name");
                        this.getModel().setValue("lag1_bookname",bookname);



                        for (int i = 1; i <= 14; i++) {
                            String chapter = postData.getString("lag1_chapter" + i);
                            this.getModel().setValue("lag1_chapter" + i, chapter);
                        }
                        for (int i = 1; i <= 5; i++) {
                            String chapter1 = postData.getString("lag1_chapter1" +"p"+ i);
                            String chapter2 = postData.getString("lag1_chapter2" +"p"+ i);
                            this.getModel().setValue("lag1_chapter1" +"p"+ i, chapter1);
                            this.getModel().setValue("lag1_chapter2" +"p"+ i, chapter2);
                        }

//                        knowpoint_zzm001
                    }else{
                        this.getView().showMessage("没有找到帖子数据");
                    }
            }else{
//                this.getView().showMessage("绑定数据错误");
            }

        }


        /**
         * 寻帖子基础资料
         * @param postNumber
         * @return 帖子数据对象
         */
        private DynamicObject queryPostData(String postNumber){
            return BusinessDataServiceHelper.loadSingle("lag1_book",new QFilter[]{new QFilter("number", QCP.equals,postNumber)});
        }
        @Override
        public void beforeClosed(BeforeClosedEvent e) {
            super.beforeClosed(e);
            e.setCheckDataChange(false); // 取消修改确认弹框
        }

        @Override
        public void afterCreateNewData(EventObject e) {
            super.afterCreateNewData(e);
            IDataModel dataModel = this.getView().getModel();   //获取表单数据模型
//            //遍历所有字段并设置为只读
//            for(IDataEntityProperty property : dataModel.getDataEntityType().getProperties()){
//                String fieldName = property.getName();
////            this.getView().setVisible(false,fieldName);
//                this.getView().setEnable(false,fieldName);
//            }
        }

}