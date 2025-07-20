package plugins.timer;

import kd.bos.dataentity.OperateOption;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.ext.form.control.CountDown;
import kd.bos.ext.form.control.events.CountDownEvent;
import kd.bos.ext.form.control.events.CountDownListener;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

public class AutoSaveBillPlugin extends AbstractFormPlugin implements CountDownListener {
    private boolean isRunning = false; // 是否正在运行
    private CountDown countDown; // 倒考试计时控件
    private int targetSeconds = 0; // 考试时长（秒）
    @Override
    public void registerListener(EventObject e) {
//注册倒考试计时控件的监听
        CountDown countdown = this.getView().getControl("lag1_countdownap");
        countdown.addCountDownListener(this);
        super.registerListener(e);
    }
    @Override
    public void initialize() {
        super.initialize();
        // 在initialize方法中初始化控件引用
        countDown = this.getView().getControl("lag1_cdownap");
    }
    //打开单据时，根据组卷页面设置的倒考试计时时长设置倒考试计时
    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        String key = source.getKey();
//这里是考试开始的逻辑，最好替换成动态表单弹窗-点击开始考试按钮后触发,也可以换为打开作答界面自动触发
        if (StringUtils.equals("xxx", key)) {
            startTimer();
        }
    }

    private void startTimer() {
        if (!isRunning && countDown != null) {
            // 获取考试时长（分钟）
            Object targetValue = this.getModel().getValue("lag1_num_target");
            if (targetValue != null) {
                int targetMinutes = 0;
                if (targetValue instanceof Integer) {
                    targetMinutes = (Integer) targetValue;
                } else if (targetValue instanceof Number) {
                    targetMinutes = ((Number) targetValue).intValue();
                } else {
                }
                targetSeconds = targetMinutes * 60; // 转换为秒
                // 启动考试计时器
                isRunning = true;
                // 设置倒考试计时控件的时长并启动
                countDown.setDuration(targetSeconds);
                countDown.start();
                // 显示开始考试计时消息
                this.getView().showMessage("考试计时开始！考试时长：" + formatTime(targetSeconds));
            }
        }
    }

    @Override
    public void preOpenForm(PreOpenFormEventArgs e) {
        super.preOpenForm(e);
    }

    public void countDownEnded() {
        // 考试计时结束时触发
        if (isRunning) {
            isRunning = false;
            this.getView().showMessage("考试计时结束！总时长：" + targetSeconds + "分钟");
        }
        //此处加入提交按钮的点击触发逻辑


        //倒考试计时结束时，触发该提交事件
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
