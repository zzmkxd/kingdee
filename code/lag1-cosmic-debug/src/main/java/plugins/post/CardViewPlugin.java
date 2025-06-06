package plugins.post;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickListener;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.list.IListView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.lang3.StringUtils;

public class CardViewPlugin extends AbstractBasePlugIn implements ItemClickListener {
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        if("donothing".equals(afterDoOperationEventArgs.getOperateKey())){
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
                    FormShowParameter formShowParameter = new FormShowParameter();
                    formShowParameter.setFormId(targetForm);

                    formShowParameter.getOpenStyle().setShowType(ShowType.Modal);

                    //传递帖子编码作为自定义参数
                    formShowParameter.setCustomParam("postNumber", postNumber);
                    formShowParameter.setCustomParam("pkId",rowPkId);

                    //添加浏览量
                    int view = postSingle.getInt("lag1_view");
                    view+=1;
                    postSingle.set("lag1_view",view);
                    SaveServiceHelper.update(postSingle); // 这一步至关重要！ 更新数据

                    //打开下一个表单并传递参数
                    this.getView().showForm(formShowParameter);
                }
//                String code = postSingle.getString("number");
//                this.getView().showMessage("帖子编码"+code);
            }else{
                this.getView().showMessage("错误");
            }
        }
    }
}
