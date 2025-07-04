package plugins.course;

import com.kingdee.bos.report.filter.parameter.data.Entry;
import dm.jdbc.util.StringUtil;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.property.ComboProp;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.control.events.ItemClickListener;
import kd.bos.form.field.ComboEdit;
import kd.bos.form.field.ComboItem;
import kd.bos.list.BillList;
import kd.bos.list.events.ListRowClickEvent;
import kd.bos.list.events.ListRowClickListener;
import kd.bos.mservice.form.FormService;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.*;

/**
 * 基础资料插件
 */
public class FilterBook extends AbstractBasePlugIn implements Plugin, ListRowClickListener {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners();
        //注册工具栏点击事件
        this.addItemClickListeners("tbmain");
        //注册单据卡片点击事件
        // 获取单据列表卡片视图控件
        BillList billList = this.getControl("lag1_billlistap");
        billList.addListRowClickListener(this);

//        //上传题目按钮
//        // 先通过getControl来控制对应的控件
//        Button button1 = this.getView().getControl("lag1_uploadpro");
//        // 再给这个控件加上监听功能
//        button1.addClickListener(this);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        // 获取被点击的控件对象
        Control source = (Control) evt.getSource();
        if (StringUtils.equals("lag1_uploadpro", source.getKey())) {
            // 相关逻辑操作...
            //分别收集课程编号、书籍编号、大章节编号、小章节编号
            DynamicObject DyObject = this.getModel().getDataEntity();
            String courseNo = DyObject.getString("number");
            String bookNo, bigChaperNo, liChaperNo;
            bookNo=DyObject.getString("lag1_bookcombofield");
            bigChaperNo=DyObject.getString("lag1_bigchapcombofield");
            liChaperNo = DyObject.getString("lag1_littlechapcombofield");
            if(StringUtils.isNotBlank(courseNo) && StringUtils.isNotBlank(bookNo) &&StringUtils.isNotBlank(bigChaperNo)&&StringUtils.isNotBlank(liChaperNo)){
                //打开题目基础资料
//                this.getView().showMessage(courseNo+" "+bookNo+" "+bigChaperNo+" "+liChaperNo);
                BillShowParameter nxtList = new BillShowParameter();
                nxtList.setFormId("lag1_protest");  //基础资料列表的标准表单ID

                //设置打开方式为模态窗口
                nxtList.getOpenStyle().setShowType(ShowType.Modal);
                nxtList.setCustomParam("courseNo",courseNo);
                nxtList.setCustomParam("bookNo",bookNo);
                nxtList.setCustomParam("bigChaperNo",bigChaperNo);
                nxtList.setCustomParam("liChaperNo",liChaperNo);

                //设置宽高
                StyleCss styleCss = new StyleCss();
                styleCss.setHeight("1000");
                styleCss.setWidth("1200");
                nxtList.getOpenStyle().setInlineStyleCss(styleCss);
                this.getView().showForm(nxtList);
            }else{
                this.getView().showMessage("选择不完整");
            }
        }
    }

    //    点击跳转对应教材基础资料页
    @Override
    public void listRowClick(ListRowClickEvent evt) {
        ListRowClickListener.super.listRowClick(evt);
        BillList billList = this.getControl("lag1_billlistap");
        ListSelectedRow listSelectedRow = billList.getCurrentSelectedRowInfo();
        if (listSelectedRow!=null){
            Long pk = (Long) listSelectedRow.getPrimaryKeyValue();
            BillShowParameter parameter = new BillShowParameter();
            parameter.setFormId("lag1_book");
            parameter.setPkId(pk);
            parameter.getOpenStyle().setShowType(ShowType.Modal);
            this.getView().showForm(parameter);
        }
    }

    //    添加教材点击事件
    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
//        this.getView().showMessage(itemKey);
        if("lag1_addbook".equalsIgnoreCase(itemKey)){
            //添加教材页面
            BillShowParameter nxtList = new BillShowParameter();
            nxtList.setFormId("lag1_book");  //基础资料列表的标准表单ID

            //设置打开方式为模态窗口
            nxtList.getOpenStyle().setShowType(ShowType.Modal);
            String courseId = this.getModel().getValue("number").toString();
            String courseName = this.getModel().getValue("name").toString();
            if(StringUtils.isNotBlank(courseId)) nxtList.setCustomParam("courseId",courseId);
            if(StringUtils.isNotBlank(courseName)) nxtList.setCustomParam("courseName",courseName);

            //设置宽高
            StyleCss styleCss = new StyleCss();
            styleCss.setHeight("1000");
            styleCss.setWidth("1200");
            nxtList.getOpenStyle().setInlineStyleCss(styleCss);

            this.getView().showForm(nxtList);
        }
    }
    //   根据课程编号筛选教材列表
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        DynamicObject courseDO = this.getModel().getDataEntity();
        String courseId = this.getModel().getValue("number").toString();
        if(courseId!=null && !courseId.isEmpty()){
            setBookFilter(courseId);
        }
    }

    private void setBookFilter(String courseId) {
        BillList bookList = this.getView().getControl("lag1_billlistap");
        if(bookList!=null){
            QFilter filter = new QFilter("lag1_courseid", QCP.equals,courseId);
            bookList.setFilter(filter);
            bookList.refresh();
        }else{
            this.getView().showMessage("bookList未找到");
        }
    }

//    //    监听下拉框值变化
//    @Override
//    public void propertyChanged(PropertyChangedArgs e) {
//        super.propertyChanged(e);
//        String propertyName = e.getProperty().getName();
//        if("lag1_bookcombofield".equals(propertyName)){
//            String selectedBookNumber = e.getChangeSet()[0].getNewValue().toString();
//            if(StringUtil.isNotEmpty(selectedBookNumber)){
//                loadChapters(selectedBookNumber);
//            }
//        }else if("lag1_bigchapcombofield".equals(propertyName)){
//            DynamicObject Do = this.getModel().getDataEntity();
//            String param1 = Do.getString("lag1_bookcombofield");
//            String selectedChapterNumber = e.getChangeSet()[0].getNewValue().toString();
//            if(StringUtil.isNotEmpty(selectedChapterNumber)){
//                loadliChapters(param1,selectedChapterNumber);
//            }
//        }
//    }

//    //    题目上传相关
////    1.绑定下拉列表的教材项
//    @Override
//    public void afterLoadData(EventObject e) {
//        super.afterLoadData(e);
//        List<ComboItem> comboItems = new ArrayList<>();
//        String courseId = this.getModel().getValue("number").toString();
//        //查询教材表
//        QFilter filter = new QFilter("lag1_courseid","=",courseId);
//        DynamicObject[] books = BusinessDataServiceHelper.load("lag1_book","number,name",new QFilter[]{filter});
//        //绑定到下拉列表
//        for(DynamicObject book:books){
//            ComboItem item = new ComboItem();
//            item.setCaption(new LocaleString(book.getString("name")));
//            item.setValue(book.getString("number"));
//            comboItems.add(item);
//        }
//        ComboEdit combo = (ComboEdit) this.getControl("lag1_bookcombofield");
//        combo.setComboItems(comboItems);
//    }
//    //    加载大章节
//    private void loadChapters(String bookNumber){
//        QFilter filter = new QFilter("number","=",bookNumber);
//        DynamicObject[] chapters=BusinessDataServiceHelper.load("lag1_book","id,lag1_chapter1,lag1_chapter2,lag1_chapter3,lag1_chapter4,lag1_chapter5,lag1_chapter6,lag1_chapter7,lag1_chapter8,lag1_chapter9,lag1_chapter10,lag1_chapter11,lag1_chapter12,lag1_chapter13,lag1_chapter14",new QFilter[]{filter});
//        //构建下拉选项
//        List<ComboItem> chapterItems = new ArrayList<>();
//        for(DynamicObject chapter : chapters) {
//            // 提取章节名称字段
//            for (int i = 1; i <= 14; i++) {
//                String chapterFieldName = "lag1_chapter" + i;
//                String chapterName = chapter.getString(chapterFieldName);
////                this.getView().showMessage(chapterName);
//                if (StringUtils.isNotBlank(chapterName)) { // 确保章节名称不为空
//                    ComboItem item = new ComboItem();
//                    item.setCaption(new LocaleString(chapterName));
//                    item.setValue(chapterFieldName); // 假设使用主键作为值
//                    chapterItems.add(item);
//                }
//            }
//        }
//        // 3. 设置到第二个下拉框
//        ComboEdit chapterCombo = (ComboEdit)this.getControl("lag1_bigchapcombofield");
//        chapterCombo.setComboItems(chapterItems);
//    }
//
//    private void loadliChapters(String selectedBookNumber,String selectedChapterNumber) {
//        QFilter filter = new QFilter("number","=",selectedBookNumber);  //筛选书
//
//        // 动态生成需要加载的字段名
//        List<String> fieldNames = new ArrayList<>();
//        for (int i = 1; i <= 5; i++) { // 假设需要加载 p1 到 p5 的字段
//            fieldNames.add(selectedChapterNumber + "p" + i);
//        }
//
//        // 将字段名列表转换为字符串，用于查询
//        String fields = String.join(",", fieldNames);
//
//        DynamicObject[] chapters=BusinessDataServiceHelper.load("lag1_book",fields,new QFilter[]{filter});
//
//        if(chapters!=null){
//            //构建下拉选项
//            List<ComboItem> chapterItems = new ArrayList<>();
//            for(DynamicObject chapter : chapters) {
//                // 提取小章节名称字段
//                for (String fieldName : fieldNames) {
//                    String chapterName = chapter.getString(fieldName);
//                    if (StringUtils.isNotBlank(chapterName)) {
//                        ComboItem item = new ComboItem();
//                        item.setCaption(new LocaleString(chapterName));
//                        item.setValue(fieldName); // 假设使用主键作为值
//                        chapterItems.add(item);
//                    }
//                }
//            }
//            // 3. 设置到第三个下拉框
//            ComboEdit chapterCombo = (ComboEdit)this.getControl("lag1_littlechapcombofield");
//            chapterCombo.setComboItems(chapterItems);
//        }
//    }
}