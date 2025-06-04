//package plugins;
//
//import kd.bos.form.plugin.AbstractFormPlugin;
//import kd.sdk.plugin.Plugin;
//
///**
// * 动态表单插件
// */
//public class TimerReviewPlugin extends AbstractFormPlugin implements Plugin {
//
//}

package plugins.timer;

import java.util.*;
import java.text.SimpleDateFormat;

import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.context.RequestContext;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;


public class TimerReviewPlugin extends AbstractFormPlugin implements Plugin {
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

        // 加载用户的学习记录
        loadUserTimerRecords();
    }

    private void loadUserTimerRecords() {
        // 获取当前用户
        String currentUser = RequestContext.get().getUserName();

        // 查询用户的学习记录
        QFilter userFilter = new QFilter("creator", QCP.equals, currentUser);
        DynamicObject[] records = BusinessDataServiceHelper.load("soc_study_timers", null,
                new QFilter[] { userFilter });

        // 处理数据，生成统计信息
        processTimerRecords(records);
    }

    private void processTimerRecords(DynamicObject[] records) {
        if (records.length == 0) {
            this.getView().showMessage("暂无学习记录");
            return;
        }

        // 计算总学习时间和平均专注度
        int totalMinutes = 0;
        int totalFocus = 0;
        int totalSessions = records.length;

        for (DynamicObject record : records) {
            totalMinutes += record.getInt("lag1_num_actual");
            totalFocus += record.getInt("lag1_num_focus");
        }

        // 计算平均专注度
        int avgFocus = totalSessions > 0 ? totalFocus / totalSessions : 0;

        // 显示统计信息
        this.getModel().setValue("soc_label_total_time", totalMinutes + "分钟");
        this.getModel().setValue("soc_label_avg_focus", avgFocus + "%");
        this.getModel().setValue("soc_label_total_sessions", totalSessions + "次");

        // 生成日/周/月视图数据
        generateDailyView(records);
        generateWeeklyView(records);
        generateMonthlyView(records);

        // 分析中断原因
        analyzeInterruptions(records);
    }

    private void generateDailyView(DynamicObject[] records) {
        // 按日期分组统计学习时间
        Map<String, Integer> dailyStats = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (DynamicObject record : records) {
            Date startDate = record.getDate("lag1_date_start");
            String dateKey = dateFormat.format(startDate);
            int minutes = record.getInt("lag1_num_actual");

            dailyStats.put(dateKey, dailyStats.getOrDefault(dateKey, 0) + minutes);
        }

        // 显示日视图数据
        StringBuilder dailyView = new StringBuilder("日学习时间统计:\n");
        for (Map.Entry<String, Integer> entry : dailyStats.entrySet()) {
            dailyView.append(String.format("%s: %d分钟\n", entry.getKey(), entry.getValue()));
        }

        this.getModel().setValue("soc_multitext_daily", dailyView.toString());
    }

    private void generateWeeklyView(DynamicObject[] records) {
        // 按周分组统计学习时间
        Map<Integer, Integer> weeklyStats = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        for (DynamicObject record : records) {
            Date startDate = record.getDate("lag1_date_start");
            cal.setTime(startDate);
            int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
            int minutes = record.getInt("lag1_num_actual");

            weeklyStats.put(weekOfYear, weeklyStats.getOrDefault(weekOfYear, 0) + minutes);
        }

        // 显示周视图数据
        StringBuilder weeklyView = new StringBuilder("周学习时间统计:\n");
        for (Map.Entry<Integer, Integer> entry : weeklyStats.entrySet()) {
            weeklyView.append(String.format("第%d周: %d分钟\n", entry.getKey(), entry.getValue()));
        }

        this.getModel().setValue("soc_multitext_weekly", weeklyView.toString());
    }

    private void generateMonthlyView(DynamicObject[] records) {
        // 按月分组统计学习时间
        Map<String, Integer> monthlyStats = new HashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

        for (DynamicObject record : records) {
            Date startDate = record.getDate("lag1_date_start");
            String monthKey = monthFormat.format(startDate);
            int minutes = record.getInt("lag1_num_actual");

            monthlyStats.put(monthKey, monthlyStats.getOrDefault(monthKey, 0) + minutes);
        }

        // 显示月视图数据
        StringBuilder monthlyView = new StringBuilder("月学习时间统计:\n");
        for (Map.Entry<String, Integer> entry : monthlyStats.entrySet()) {
            monthlyView.append(String.format("%s: %d分钟\n", entry.getKey(), entry.getValue()));
        }

        this.getModel().setValue("soc_multitext_monthly", monthlyView.toString());
    }

    private void analyzeInterruptions(DynamicObject[] records) {
        // 统计中断原因
        Map<String, Integer> interruptReasons = new HashMap<>();
        int totalInterrupts = 0;

        for (DynamicObject record : records) {
            DynamicObjectCollection interrupts = record.getDynamicObjectCollection("lag1_entryentity");
            for (DynamicObject interrupt : interrupts) {
                String reason = interrupt.getString("lag1_combofield_reason");
                interruptReasons.put(reason, interruptReasons.getOrDefault(reason, 0) + 1);
                totalInterrupts++;
            }
        }

        // 生成中断分析报告
        if (totalInterrupts > 0) {
            StringBuilder analysis = new StringBuilder("中断原因分析:\n");
            for (Map.Entry<String, Integer> entry : interruptReasons.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / totalInterrupts;
                analysis.append(String.format("%s: %.1f%% (%d次)\n",
                        entry.getKey(), percentage, entry.getValue()));
            }

            // 提供改进建议
            String mainReason = interruptReasons.entrySet().stream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .get().getKey();

            analysis.append("\n改进建议:\n");
            if ("接电话".equals(mainReason)) {
                analysis.append("您的主要中断源是接电话，建议学习时将手机设为勿扰模式。");
            } else if ("休息".equals(mainReason)) {
                analysis.append("您经常因休息而中断，建议尝试番茄钟模式，规律安排休息时间。");
            } else {
                analysis.append("建议分析中断原因，找出规律并制定相应的改进策略。");
            }

            this.getModel().setValue("soc_multitext_analysis", analysis.toString());
        } else {
            this.getModel().setValue("soc_multitext_analysis", "暂无中断记录");
        }
    }
}