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
    private final String TKCOURSE="lag1_course";
    private final String TKBOOK="lag1_book";
        //全局变量
        String bookNumber;
        Long pkId;
        DynamicObject bookData;

        @Override
        public void registerListener(EventObject e) {
            super.registerListener(e);
            // 注册点击事件
            this.addItemClickListeners("tbmain");
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

                }
            }
        }

        //这部分才是接收知识点分析情况部分功能
        @Override
        public void beforeBindData(EventObject e) {
            super.beforeBindData(e);
//        这里是接收参数端在beforeBIndData里面开始先接受传过来的参数
            bookNumber = this.getView().getFormShowParameter().getCustomParam("bookNumber");        //这里的getCustomParam里面的标识就是在上个页面传参的时候设置的
            pkId = this.getView().getFormShowParameter().getCustomParam("pkId");        //这样就可以获取到传过来的参数
            if(StringUtils.isNotBlank(bookNumber)){
                //查询基础资料
                bookData = queryBookData(bookNumber);
                if(bookData!=null){
                    //课程id
                    String courseid = bookData.getString("lag1_courseid.number");
                    String fields = "number,name";
                    QFilter qFilter = new QFilter("number",QCP.equals,courseid);
                    DynamicObject course = BusinessDataServiceHelper.loadSingle(TKCOURSE,fields,new QFilter[]{qFilter});
                    if(course!=null){
                        this.getModel().setValue("lag1_course",course);
                    }
                    //教材id
                    String bookid = bookData.getString("number");
                    String bookfields = "number";
                    QFilter qFilter1 = new QFilter("number", QCP.equals,bookid);
                    DynamicObject book = BusinessDataServiceHelper.loadSingle(TKBOOK,bookfields,new QFilter[]{qFilter1});
                    if(book!=null){
                        this.getModel().setValue("lag1_book",book);
                    }
                }else{
                    this.getView().showMessage("没有找到章节数据");
                }
            }else{
//                this.getView().showMessage("绑定数据错误");
            }

        }

        private DynamicObject queryBookData(String bookNumber){
            return BusinessDataServiceHelper.loadSingle(TKBOOK,new QFilter[]{new QFilter("number", QCP.equals,bookNumber)});
        }
        @Override
        public void beforeClosed(BeforeClosedEvent e) {
            super.beforeClosed(e);
            e.setCheckDataChange(false); // 取消修改确认弹框
        }


}