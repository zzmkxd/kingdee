package plugins.timer;

import java.util.*;
import java.text.SimpleDateFormat;

import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.form.FormShowParameter;
import kd.sdk.plugin.Plugin;

/**
 * 动态表单插件
 */
public class TimerResultPlugin extends AbstractFormPlugin implements Plugin {
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

        // 获取传递的参数
        FormShowParameter showParameter = this.getView().getFormShowParameter();
        long startTime = (long) showParameter.getCustomParam("startTime");
        long endTime = (long) showParameter.getCustomParam("endTime");
        int pauseCount = (int) showParameter.getCustomParam("pauseCount");
        long totalPauseTime = (long) showParameter.getCustomParam("totalPauseTime");
        int focusLevel = (int) showParameter.getCustomParam("focusLevel");
        List<Map<String, Object>> interruptList = (List<Map<String, Object>>) showParameter
                .getCustomParam("interruptList");
        String timerMode = (String) showParameter.getCustomParam("timerMode");
        int targetMinutes = (int) showParameter.getCustomParam("targetMinutes");

        // 显示计时结果
        displayTimerResult(startTime, endTime, pauseCount, totalPauseTime, focusLevel, timerMode, targetMinutes);

        // 显示中断记录
//        displayInterruptList(interruptList);

        // 生成专注度图表
        generateFocusChart(interruptList, startTime, endTime, focusLevel);
    }

    private void displayTimerResult(long startTime, long endTime, int pauseCount,
                                    long totalPauseTime, int focusLevel, String timerMode, int targetMinutes) {
        // 计算实际学习时间
        long actualTime = endTime - startTime - totalPauseTime;
        int minutes = (int) (actualTime / (1000 * 60));

        // 设置结果显示
        this.getModel().setValue("soc_label_mode", timerMode);
        this.getModel().setValue("soc_label_target", targetMinutes + "分钟");
        this.getModel().setValue("soc_label_start",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime)));
        this.getModel().setValue("soc_label_end",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(endTime)));
        this.getModel().setValue("soc_label_duration", minutes + "分钟");
        this.getModel().setValue("soc_label_pause", pauseCount + "次");
        this.getModel().setValue("soc_label_focus", focusLevel + "%");

        // 计算目标完成率
        int completionRate = (int) (((float) minutes / targetMinutes) * 100);
        if (completionRate > 100)
            completionRate = 100;
        this.getModel().setValue("soc_label_completion", completionRate + "%");

        // 根据专注度给出评价
        String evaluation = "";
        if (focusLevel >= 90) {
            evaluation = "太棒了！你的专注力非常出色！";
        } else if (focusLevel >= 70) {
            evaluation = "不错！保持良好的专注状态。";
        } else if (focusLevel >= 50) {
            evaluation = "还可以，但有提升空间。";
        } else {
            evaluation = "专注度较低，建议减少干扰因素。";
        }
        this.getModel().setValue("soc_label_evaluation", evaluation);
    }

//    private void displayInterruptList(List<Map<String, Object>> interruptList) {
//        // 显示中断记录列表
//        EntryGrid interruptGrid = this.getView().getControl("soc_interrupt_grid");
//
//        // 清空现有数据
//        interruptGrid.deleteControls();
//
//        // 添加中断记录
//        for (Map<String, Object> interrupt : interruptList) {
//            int row = interruptGrid.addRow();
//            interruptGrid.setValue("soc_column_time", row,
//                    new SimpleDateFormat("HH:mm:ss").format(interrupt.get("time")));
//            interruptGrid.setValue("soc_column_reason", row, interrupt.get("reason"));
//            interruptGrid.setValue("soc_column_duration", row, interrupt.get("duration") + "分钟");
//            interruptGrid.setValue("soc_column_remark", row, interrupt.get("remark"));
//        }
//    }

    private void generateFocusChart(List<Map<String, Object>> interruptList,
                                    long startTime, long endTime, int finalFocusLevel) {
        // 这里可以使用自定义控件或iframe嵌入图表
        // 简化版：使用文本描述专注度变化

        StringBuilder chartDesc = new StringBuilder();
        chartDesc.append("专注度变化:\n");
        chartDesc.append("初始专注度: 100%\n");

        // 按时间排序中断记录
        interruptList.sort(Comparator.comparing(m -> ((Date) m.get("time"))));

        // 添加每次中断的专注度变化
        int currentFocus = 100;
        for (Map<String, Object> interrupt : interruptList) {
            Date interruptTime = (Date) interrupt.get("time");
            String reason = (String) interrupt.get("reason");
            currentFocus -= 5; // 每次中断降低5%
            if (currentFocus < 0)
                currentFocus = 0;

            chartDesc.append(String.format("%s - 中断(%s): 专注度降至%d%%\n",
                    new SimpleDateFormat("HH:mm:ss").format(interruptTime),
                    reason, currentFocus));
        }

        chartDesc.append(String.format("最终专注度: %d%%", finalFocusLevel));

        // 显示专注度变化描述
        this.getModel().setValue("soc_multitext_chart", chartDesc.toString());
    }
}