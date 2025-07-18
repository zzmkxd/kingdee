package plugins.test;

import kd.bos.context.RequestContext;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * 动态表单插件
 */
public class FaceVerify extends AbstractFormPlugin implements Plugin {
    private static final String VERITYBTN = "btnok";
    private static final String CANCELBTN = "btncancel";
    private static final String REGISTERBTN = "lag1_register";
    private static final String FACEVERIFYGROUP = "test_group";
    private static final String QUALITY = "LOW";
    private String stuname = "";
    private String stuid = "";
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Button verify_btn = this.getView().getControl(VERITYBTN);
        Button cancel_btn = this.getView().getControl(CANCELBTN);
        Button register_btn = this.getView().getControl(REGISTERBTN);
        verify_btn.addClickListener(this);
        cancel_btn.addClickListener(this);
        register_btn.addClickListener(this);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        stuname = this.getModel().getDataEntity().getString("lag1_name");
        stuid = this.getModel().getDataEntity().getString("lag1_stuid");
        if (StringUtils.equals(VERITYBTN, source.getKey())) {
            // 调用BaiduFaceForKd的方法
            BaiduFaceForKd faceRecognizer = new BaiduFaceForKd();
            String json = faceRecognizer.doRecognize(FACEVERIFYGROUP); // 替换为实际的group_id
            FaceResultParser.FaceResult res = FaceResultParser.parse(json);
            if(res==null){
                this.getView().showErrorNotification("识别失败，接口返回异常");
                return;
            }
            //比对是否一致
            boolean idMatch = StringUtils.equals(res.userId,stuid);
            boolean nameMatch = StringUtils.equals(res.userInfo,stuname);
            boolean pass = res.score>=80&&idMatch&&nameMatch;
            String msg = pass?"验证成功":"验证失败";
//            this.getView().showMessage("识别结果：\n" +
//                    "user_id  = " + res.userId + "\n" +
//                    "user_info= " + res.userInfo + "\n" +
//                    "score    = " + res.score + "\n" +
//                    "验证结论  = " + msg);
            this.getView().returnDataToParent(pass);
            this.getView().close();
        } else if (StringUtils.equals(CANCELBTN,source.getKey())) {
//            this.getView().showMessage("取消验证");
            this.getView().returnDataToParent(false);
            this.getView().close();
        }else if(StringUtils.equals(REGISTERBTN,source.getKey())){
            // 调用BaiduFaceForKd的方法
            BaiduFaceForKd faceRecognizer = new BaiduFaceForKd();
            String json = faceRecognizer.doRegister(FACEVERIFYGROUP,stuid,stuname,QUALITY);
            boolean isOk = FaceResultParser.isSuccess(json);
            if(isOk){
                this.getView().showMessage("注册成功");
            }else{
                this.getView().showMessage("注册失败");
            }
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        stuname = RequestContext.get().getUserName();
        stuid = String.valueOf(RequestContext.get().getCurrUserId());
        this.getModel().setValue("lag1_name",stuname);
        this.getModel().setValue("lag1_stuid",stuid);
    }
}