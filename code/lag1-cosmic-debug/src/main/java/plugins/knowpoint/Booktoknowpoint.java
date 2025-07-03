package plugins.knowpoint;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.base.BaseShowParameter;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Button;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.control.events.ItemClickListener;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.IListView;
import kd.bos.mvc.base.BaseView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * 动态表单插件
 */
public class Booktoknowpoint extends AbstractBasePlugIn implements ItemClickListener {
        //回调方法的标识符
//    private static final String CALLBACK_ID = "parent_refresh1";
        //注册点击事件


        @Override
        public void registerListener(EventObject e) {
            // 注册点击事件
            super.registerListener(e);
            this.addItemClickListeners("tbmain");
        }

        @Override
        public void itemClick(ItemClickEvent evt) {
            super.itemClick(evt);
            String itemKey = evt.getItemKey();
            if("lag1_button_toknowpoint".equals(itemKey)){
                IDataModel formDataModel = this.getModel();
                DynamicObject formDataEntity = formDataModel.getDataEntity();
                Long formPkId = (Long) formDataEntity.getPkValue();

                DynamicObject postSingle = BusinessDataServiceHelper.loadSingle("lag1_book", new QFilter[]{new QFilter("id", QCP.equals,formPkId)});
                if(postSingle!=null){
                    String targetForm = "lag1_knowpoint";//知识点-基础资料表单
                    String postNumber = postSingle.getString("number");//教材编码
                    if(StringUtils.isNotBlank(postNumber)){
                        //创建参数对象
//                    这里如果要打开的页面类型不同构造的是不一样的 typora里面有片可以抄
                        BaseShowParameter baseShowParameter = new BaseShowParameter();
                        baseShowParameter.setFormId(targetForm);
                        baseShowParameter.getOpenStyle().setShowType(ShowType.Modal);

                        // 关键点：必须设置表单状态
                        baseShowParameter.setStatus(OperationStatus.ADDNEW); // 或 OperationStatus.VIEW/EDIT/ADDNEW
                        this.getView().showForm(baseShowParameter);
//                        FormShowParameter formShowParameter = new FormShowParameter();
//                        formShowParameter.setFormId(targetForm);
//
//                        formShowParameter.getOpenStyle().setShowType(ShowType.Modal);

                        //传递帖子编码作为自定义参数
                        baseShowParameter.setCustomParam("postNumber", postNumber); //这里是发送端在设置要传的参数    可以替换成原lag_md的字符串？
                        baseShowParameter.setCustomParam("pkId",formPkId);

                        //设置宽高
                        StyleCss styleCss = new StyleCss();
                        styleCss.setHeight("600");      //如果设置了宽高就有点像弹窗
                        styleCss.setWidth("1200");
                        baseShowParameter.getOpenStyle().setInlineStyleCss(styleCss);       //这几行是css样式


                        SaveServiceHelper.update(postSingle); // 这一步至关重要！ 更新数据

                        //打开下一个表单并传递参数
                        this.getView().showForm(baseShowParameter);     //这里是最后一步
                    }else{
                        this.getView().showMessage("错误");
                    }

                }else{
                    this.getView().showMessage("错误");
                }
            }

        }


//    @Override
//    public void click(EventObject e) {
//        super.click(e);
//        if ("btnOpenModal".equalsIgnoreCase(e.getItemKey())) {
//            FormShowParameter showParameter = new FormShowParameter();
//            showParameter.setFormId("targetFormId");
//            showParameter.getCustomParam().put("paramName", "paramValue");
//            showParameter.getOpenStyle().setShowType(ShowType.Modal);
//            showParameter.setCloseCallBack(new CloseCallBack(this, "kdec_sfform"));
//            this.getView().showForm(showParameter);
//        }
//    }

    @Override
    public void afterCreateNewData(EventObject e) {
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Object text = formShowParameter.getCustomParam("paramName");
        this.getModel().setValue("kdec_textfield", text);
    }

//    //回调方法，在子表单关闭时调用
//    public void onChildFormClosed(ClosedCallBackEvent evt){
//        if(CALLBACK_ID.equals(evt.getActionId())){
//            this.getView().updateView();
//        }
//    }
}
