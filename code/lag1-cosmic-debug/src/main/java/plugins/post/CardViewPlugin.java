package plugins.post;

import com.grapecity.documents.excel.Q;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.control.events.ItemClickListener;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.list.BillList;
import kd.bos.list.IListView;
import kd.bos.list.ListCardView;
import kd.bos.mvc.card.CardView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.awt.font.OpenType;
import java.util.EventObject;

public class CardViewPlugin extends AbstractBasePlugIn implements ItemClickListener {
    //回调方法的标识符
//    private static final String CALLBACK_ID = "parent_refresh1";
    //注册点击事件

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //注册工具栏点击事件
        Toolbar toolbar = this.getView().getControl("toolbarap");
        toolbar.addItemClickListener(this);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if("lag1_sendpost".equals(itemKey)){
            //打开发帖表单
            BillShowParameter nxtList = new BillShowParameter();
            nxtList.setFormId("lag1_post");  //基础资料列表的标准表单ID

            //设置打开方式为模态窗口
            nxtList.getOpenStyle().setShowType(ShowType.Modal);

            //设置宽高
            StyleCss styleCss = new StyleCss();
            styleCss.setHeight("600");
            styleCss.setWidth("1200");
            nxtList.getOpenStyle().setInlineStyleCss(styleCss);

            this.getView().showForm(nxtList);
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        //查看帖子详情
        if("cardgetinfo".equals(afterDoOperationEventArgs.getOperateKey())){
            IListView listview = (IListView) this.getView();
            Long rowPkId = (Long)listview.getFocusRowPkId();
//            this.getView().showMessage("cardId"+rowPkId);
//            DynamicObject postSingle = BusinessDataServiceHelper.loadSingle("lag1_post", new QFilter[]{new QFilter("number", QCP.equals,rowPkId)});
            DynamicObject postSingle = BusinessDataServiceHelper.loadSingle("lag1_post", new QFilter[]{new QFilter("id", QCP.equals,rowPkId)});
            if(postSingle!=null){
                String targetForm = "lag1_postdetail2";
                String postNumber = postSingle.getString("number");
                if(StringUtils.isNotBlank(postNumber)){
                    //创建参数对象
//                    这里如果要打开的页面类型不同构造的是不一样的 typora里面有片可以抄
                    FormShowParameter formShowParameter = new FormShowParameter();
                    formShowParameter.setFormId(targetForm);
//                    formShowParameter.setCloseCallBack(new CloseCallBack(this,CALLBACK_ID));

                    formShowParameter.getOpenStyle().setShowType(ShowType.Modal);

                    //传递帖子编码作为自定义参数
                    formShowParameter.setCustomParam("postNumber", postNumber); //这里是发送端在设置要传的参数    可以替换成原lag_md的字符串？
                    formShowParameter.setCustomParam("pkId",rowPkId);

                    //设置宽高
                    StyleCss styleCss = new StyleCss();
                    styleCss.setHeight("600");      //如果设置了宽高就有点像弹窗
                    styleCss.setWidth("1200");
                    formShowParameter.getOpenStyle().setInlineStyleCss(styleCss);       //这几行是css样式

                    //添加浏览量
                    int view = postSingle.getInt("lag1_view");
                    view+=1;
                    postSingle.set("lag1_view",view);
                    SaveServiceHelper.update(postSingle); // 这一步至关重要！ 更新数据

                    //打开下一个表单并传递参数
                    this.getView().showForm(formShowParameter);     //这里是最后一步
                }
//                String code = postSingle.getString("number");
//                this.getView().showMessage("帖子编码"+code);
            }else{
                this.getView().showMessage("错误");
            }
        }else if("cardthumb".equals(afterDoOperationEventArgs.getOperateKey())){
            IListView listview = (IListView) this.getView();
            Long rowPkId = (Long)listview.getFocusRowPkId();
            DynamicObject postSingle = BusinessDataServiceHelper.loadSingle("lag1_post", new QFilter[]{new QFilter("id", QCP.equals,rowPkId)});
            //添加点赞数
            int thumb = postSingle.getInt("lag1_thumb");
            thumb+=1;
            postSingle.set("lag1_thumb",thumb);
            SaveServiceHelper.update(postSingle); //更新数据
            //调用refresh方法刷新数据
            listview.refresh();

//            // 判断当前用户是否已经点赞过
//            QFilter userFilter = new QFilter("user_id", QCP.equals, getCurrentUserId());
//            DynamicObject[] userThumbs = BusinessDataServiceHelper.load("lag1_postthumb", new QFilter[]{new QFilter("post_id", QCP.equals, rowPkId), userFilter});
//
//            if (userThumbs.length > 0) {
//                // 用户已经点赞过，执行取消点赞操作
//                // 减少点赞数
//                thumb -= 1;
//                postSingle.set("lag1_thumb", thumb);
//
//                // 删除点赞记录
//                BusinessDataServiceHelper.delete("lag1_postthumb", new QFilter[]{new QFilter("post_id", QCP.equals, rowPkId), userFilter});
//            } else {
//                // 用户未点赞过，执行点赞操作
//                // 增加点赞数
//                thumb += 1;
//                postSingle.set("lag1_thumb", thumb);
//
//                // 创建新的点赞记录
//                DynamicObject newThumb = BusinessDataServiceHelper.newDynamicObject("lag1_postthumb");
//                newThumb.set("post_id", rowPkId);
//                newThumb.set("user_id", getCurrentUserId());
//                BusinessDataServiceHelper.save(newThumb);
//            }
//
//            // 更新帖子数据
//            SaveServiceHelper.update(postSingle);
//
//            // 刷新列表视图
//            listView.refresh();
        }
    }

//    //回调方法，在子表单关闭时调用
//    public void onChildFormClosed(ClosedCallBackEvent evt){
//        if(CALLBACK_ID.equals(evt.getActionId())){
//            this.getView().updateView();
//        }
//    }
}
