package plugins.timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;

import javax.swing.SwingUtilities;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import java.util.EventObject;
import kd.bos.form.field.ComboEdit;
import org.apache.commons.lang3.StringUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Label;
import kd.bos.context.RequestContext;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

/**
 * 动态表单插件
 */
public class TimerMainPlugin extends AbstractFormPlugin implements Plugin {
    private Timer timer; // 计时器
    private long startTime; // 开始时间
    private long pauseStartTime; // 暂停开始时间
    private int pauseCount; // 暂停次数
    private long totalPauseTime; // 总暂停时间
    private List<Map<String, Object>> interruptList; // 中断记录列表
    private int focusLevel; // 当前专注度

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 注册按钮点击事件
        ComboEdit startBtn = this.getView().getControl("lag1_start_btn");
        ComboEdit pauseBtn = this.getView().getControl("lag1_pause_btn");
        ComboEdit stopBtn = this.getView().getControl("lag1_stop_btn");

        startBtn.addClickListener(this);
        pauseBtn.addClickListener(this);
        stopBtn.addClickListener(this);
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
        // 初始化计时数据
        startTime = System.currentTimeMillis();
        pauseCount = 0;
        totalPauseTime = 0;
        interruptList = new ArrayList<>();
        focusLevel = 100; // 初始专注度为100%

        // 创建计时器，每秒更新一次
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTimerDisplay();
            }
        }, 0, 1000);

         // 更新UI状态
//         this.getView().getControl("lag1_start_btn").setEnabled(false);
//         this.getView().getControl("lag1_pause_btn").setEnabled(true);
//         this.getView().getControl("lag1_stop_btn").setEnabled(true);
    }

    private void pauseTimer() {
        pauseStartTime = System.currentTimeMillis();
        pauseCount++;
        focusLevel -= 5; // 每次暂停降低5%专注度
        if (focusLevel < 0)
            focusLevel = 0;

        // 弹出中断原因输入框
        this.getView().showMessage("请选择中断原因");
        // 这里可以弹出一个动态表单让用户选择中断原因
        Control pauseBtn = this.getView().getControl("lag1_pause_btn");
        Control startBtn = this.getView().getControl("lag1_start_btn");
        // // 更新UI状态
        // this.getView().getControl("lag1_pause_btn").setEnabled(false);
        // this.getView().getControl("lag1_start_btn").setEnabled(true);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // 计算最终数据
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime - totalPauseTime;

        // 保存计时记录到数据库
        saveTimerRecord(totalTime);

        // 显示结算页面
        showResultPage();
    }

    private void saveTimerRecord(long totalTime) {
        // 创建新的计时记录
        // 使用 "soc_study_timers" 作为表单标识符
        DynamicObject timerRecord = BusinessDataServiceHelper.newDynamicObject("soc_study_timers");

        // 设置基本信息
        String mode = this.getModel().getValue("lag1_combofield_mode").toString(); // 更新字段标识符
        int targetMinutes = Integer.parseInt(this.getModel().getValue("lag1_num_target").toString()); // 更新字段标识符
        int actualMinutes = (int) (totalTime / (1000 * 60));

        timerRecord.set("name",
                RequestContext.get().getUserName() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        // 注意：基础资料中没有直接定义 'creator' 字段，但通常系统会自动记录创建者。
        // 如果需要显式设置，请确保表单中有此字段。
        timerRecord.set("lag1_combofield_mode", mode); // 更新字段标识符
        timerRecord.set("lag1_date_start", new Date(startTime)); // 更新字段标识符
        timerRecord.set("lag1_date_end", new Date(System.currentTimeMillis())); // 更新字段标识符
        timerRecord.set("lag1_num_target", targetMinutes); // 更新字段标识符
        timerRecord.set("lag1_num_actual", actualMinutes); // 更新字段标识符
        timerRecord.set("lag1_num_pause", pauseCount); // 更新字段标识符
        timerRecord.set("lag1_num_focus", focusLevel); // 更新字段标识符
        // 关于 lag1_multitext (中断原因) 的处理，需要根据您的决定来添加逻辑
        // 如果是统一的多行文本，可以在这里设置
        // timerRecord.set("lag1_multitext", "一些汇总的中断原因说明");

        // 保存中断记录
        // 使用 "lag1_entryentity" 作为单据体标识符
        DynamicObjectCollection entryEntity = timerRecord.getDynamicObjectCollection("lag1_entryentity");
        for (Map<String, Object> interrupt : interruptList) {
            DynamicObject entry = entryEntity.addNew();
            entry.set("lag1_date_interrupt", interrupt.get("time")); // 更新字段标识符
            entry.set("lag1_combofield_reason", interrupt.get("reason")); // 更新字段标识符
            entry.set("lag1_num_duration", interrupt.get("duration")); // 更新字段标识符
            // 如果决定不使用 remark 字段，可以注释或删除下面这行
            // entry.set("ozwe_textfield_remark", interrupt.get("remark"));
        }

        // 保存记录
        SaveServiceHelper.save(new DynamicObject[] { timerRecord });
    }

    private void showResultPage() {
        // 打开结算页面
        FormShowParameter resultPage = new FormShowParameter();
        resultPage.setFormId("ozwe_timer_result");
        resultPage.setCaption("学习计时结果");
        resultPage.getOpenStyle().setShowType(ShowType.Modal);

        // 传递参数
        resultPage.setCustomParam("startTime", startTime);
        resultPage.setCustomParam("endTime", System.currentTimeMillis());
        resultPage.setCustomParam("pauseCount", pauseCount);
        resultPage.setCustomParam("totalPauseTime", totalPauseTime);
        resultPage.setCustomParam("focusLevel", focusLevel);
        resultPage.setCustomParam("interruptList", interruptList);

        this.getView().showForm(resultPage);
    }

    private void updateTimerDisplay() {
        // 更新计时显示
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime - totalPauseTime;

        // 格式化时间显示
        int hours = (int) (elapsedTime / (1000 * 60 * 60));
        int minutes = (int) (elapsedTime / (1000 * 60) % 60);
        int seconds = (int) (elapsedTime / 1000 % 60);

        final String timeDisplay = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        // 在UI线程中更新显示
        SwingUtilities.invokeLater(() -> {
            Label timerLabel = getView().getControl("ozwe_timer_label");
            timerLabel.setText(timeDisplay);
        });
    }
}
