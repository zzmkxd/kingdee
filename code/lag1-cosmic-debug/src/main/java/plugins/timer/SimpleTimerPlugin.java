package plugins.timer;

import java.util.EventObject;

import kd.bos.ext.form.control.CountDown;
import kd.bos.form.control.Button;
import org.apache.commons.lang3.StringUtils;
import kd.bos.form.control.Control;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

/**
 * 超级简化版计时器插件
 * 使用CountDown控件实现计时功能：
 * 1. 按下确定控件"lag1_start_btn"后开始计时
 * 2. 获取输入的目标时长
 * 3. 自动更新倒计时控件中的时间显示
 */
public class SimpleTimerPlugin extends AbstractFormPlugin implements Plugin {
    private boolean isRunning = false; // 是否正在运行
    private CountDown countDown; // 倒计时控件
    private int targetSeconds = 0; // 目标时长（秒）
    // CountDown countdown = this.getView().getControl('控件标识');// 设置倒计时时间为70秒*
    // countdown.setDuration(70);
    // countdown.pause()
    // countdown.start()
    @Override
    public void initialize() {
        super.initialize();
        // 在initialize方法中初始化控件引用
        countDown = this.getView().getControl("lag1_cdownap");
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册确定按钮点击事件
        Button okBtn = this.getView().getControl("lag1_start_btn");
        if (okBtn != null) {
            okBtn.addClickListener(this);
        }
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        String key = source.getKey();

        if (StringUtils.equals("lag1_start_btn", key)) {
            startTimer();
        }
    }

    private void startTimer() {
        if (!isRunning && countDown != null) {
            // 获取目标时长（分钟）
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
                
                // 启动计时器
                isRunning = true;
                
                // 设置倒计时控件的时长并启动
                countDown.setDuration(targetSeconds);
                countDown.start();
                
                // 显示开始计时消息
                this.getView().showMessage("计时开始！目标时长：" + formatTime(targetSeconds));
            } else {
                this.getView().showMessage("请输入目标时长");
            }
        }
    }

    @Override
    public void preOpenForm(PreOpenFormEventArgs e) {
        super.preOpenForm(e);
    }

    public void countDownEnded() {
        // 倒计时结束时触发
        if (isRunning) {
            isRunning = false;
            this.getView().showMessage("计时结束！总时长：" + formatTime(targetSeconds));
        }
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}