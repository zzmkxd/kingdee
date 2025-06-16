package plugins.timer;

import java.util.EventObject;

import kd.bos.form.field.RadioEdit;
import kd.bos.form.field.TextEdit;
import org.apache.commons.lang3.StringUtils;

import kd.bos.form.field.ComboEdit;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.Control;
import kd.bos.form.control.Label;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.form.events.TimerElapsedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

/**
 * 动态表单插件 - 使用系统TimerElapsed事件实现的计时器
 */
public class TimerElapsedEvent extends AbstractFormPlugin implements Plugin {
    private int seconds = 0; // 计时变量
    private boolean isPaused = false; // 是否暂停
    private boolean isRunning = false; // 是否正在运行
    private int pauseCount = 0; // 暂停次数
    private int focusLevel = 100; // 专注度

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册单选按钮值变化事件
        RadioEdit startBtn = this.getView().getControl("lag1_start_btn");
        RadioEdit pauseBtn = this.getView().getControl("lag1_pause_btn");
        RadioEdit stopBtn = this.getView().getControl("lag1_stop_btn");

        if (startBtn != null)
            this.getView().showMessage(startBtn.getItems());
        if (pauseBtn != null)
            this.getView().showMessage(pauseBtn.getItems());
        if (stopBtn != null)
            this.getView().showMessage(stopBtn.getItems());
    }


    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        String key = source.getKey();

        if (StringUtils.equals("lag1_start_btn", key)) {
            startTimer();
        } else if (StringUtils.equals("lag1_pause_btn", key)) {
            pauseTimer();
        } else if (StringUtils.equals("lag1_stop_btn", key)) {
            stopTimer();
        }
    }

    private void startTimer() {
        if (!isRunning) {
            // 首次启动计时器
            isRunning = true;
            isPaused = false;
            seconds = 0;
            pauseCount = 0;
            focusLevel = 100;

        } else if (isPaused) {
            // 从暂停状态恢复
            isPaused = false;

        }
    }

    private void pauseTimer() {
        if (isRunning && !isPaused) {
            isPaused = true;
            pauseCount++;
            focusLevel -= 5; // 每次暂停降低5%专注度
            if (focusLevel < 0)
                focusLevel = 0;

            // 更新UI状态
            // updateButtonStatus();

            // 显示暂停信息
            this.getView().showMessage("计时已暂停");
        }
    }

    private void stopTimer() {
        if (isRunning) {
            isRunning = false;
            isPaused = false;

            // 显示计时结果
            String result = String.format("计时结束！\n总时长：%s\n暂停次数：%d\n专注度：%d%%",
                    formatTime(seconds), pauseCount, focusLevel);
            this.getView().showMessage(result);

            // 重置计时器
            seconds = 0;
        }
    }


    @Override
    public void preOpenForm(PreOpenFormEventArgs e) {
        super.preOpenForm(e);
        // 要求触发TimerElapsed事件
        ((FormShowParameter) e.getSource()).setListentimerElapsed(true);
    }

    @Override
    public void TimerElapsed(TimerElapsedArgs e) {
        // 只有在运行且未暂停状态下才增加秒数
        if (isRunning && !isPaused) {
            seconds++; // 增加秒数
            String time = formatTime(seconds); // 格式化为HH:MM:SS
//            TextEdit textEdit = this.getView().getControl("textfield1");
//            textEdit.setValue("动态更新的文本");
            this.getModel().setValue("lag1_timerdisplay", time);
        }
    }


    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}