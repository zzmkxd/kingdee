package plugins.graph;

import javafx.util.Pair;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.chart.*;
import kd.bos.form.control.events.RateEvent;
import kd.bos.form.control.events.RateListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Binchart extends AbstractFormPlugin implements RateListener {
    private String lowdata="0.00", standard="0.00", highdata="0.00";
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        PieChart pieChart = this.getControl("lag1_piechartap1");
        this.drawChart(pieChart);
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
    }

    @Override
    public void update(RateEvent evt) {
        PieChart pieChart = this.getControl("lag1_piechartap1");
        this.drawChart(pieChart);
        //必须刷新，否则不触发签到渲染
        pieChart.refresh();
    }
    public void drawChart(PieChart pieChart) {
        pieChart.clearData();
        pieChart.setShowTooltip(true);
        //设置为位置
        pieChart.setMargin(Position.right, "30px");
        pieChart.setMargin(Position.top, "30px");
        pieChart.setMargin(Position.bottom, "10px");
        pieChart.setMargin(Position.left, "20px");
        //添加数据
        PieSeries series = pieChart.createPieSeries("评分");
        String rateStr = this.getPageCache().get("kdec_rateap2");
        String rate2Str = this.getPageCache().get("kdec_rateap3");
        String rate3Str = this.getPageCache().get("kdec_rateap4");
        ItemValue[] data = new ItemValue[3];
//        demo2();
        analyzeAndCalculatePercentages();
        ItemValue data1,data2,data3;
        if(Objects.equals(highdata, "0.00") && Objects.equals(standard, "0.00")  && Objects.equals(lowdata, "0.00")){
            data1 = new ItemValue("掌握较差知识点",  getRateNumber("33") , "#00008B");
            data2 = new ItemValue("基本掌握知识点", getRateNumber("33") , "#1E90FF");
            data3 = new ItemValue("掌握较好知识点",   getRateNumber("33") , "#ADD8E6");
        }else{
            data1 = new ItemValue("掌握较差知识点",  getRateNumber(lowdata) , "#00008B");
            data2 = new ItemValue("基本掌握知识点", getRateNumber(standard) , "#1E90FF");
            data3 = new ItemValue("掌握较好知识点",   getRateNumber(highdata) , "#ADD8E6");
        }
        data[0] = data1;
        data[1] = data2;
        data[2] = data3;
        series.setData(data);
        //series样式
        series.setRoseType(RoseType.radius);
    }
    private Number getRateNumber(String rateStr) {
        if (rateStr == null || rateStr.trim().isEmpty()) {
            return 0;  // 默认返回 0
        }
        try {
            // 如果是整数（无小数点），返回 Integer
            if (rateStr.matches("^-?\\d+$")) {
                return Integer.valueOf(rateStr);
            }
            // 如果是小数（含小数点），返回 Double
            else if (rateStr.matches("^-?\\d+\\.\\d{1,2}$")) {
                return Double.valueOf(rateStr);
            }
            // 其他情况（如格式错误），返回 0
            else {
                return 0;
            }
        } catch (NumberFormatException e) {
            return 0;  // 转换失败时返回默认值
        }
    }

    public void analyzeAndCalculatePercentages() {
        // 1. 构建查询条件：获取当前用户的数据
        QFilter userFilter = new QFilter("creator.id", QCP.equals, RequestContext.get().getCurrUserId());
        // 2. 执行查询（只查询需要的字段）
        DynamicObjectCollection dataCollection = QueryServiceHelper.query(
                "lag1_user_data",
                "lag1_data",
                new QFilter[]{userFilter}
        );
        // 3. 初始化统计变量（使用BigDecimal保证精度）
        BigDecimal lowSum = BigDecimal.ZERO;    // 0-4区间
        BigDecimal midSum = BigDecimal.ZERO;     // 4-7区间
        BigDecimal highSum = BigDecimal.ZERO;    // 7-10区间
        BigDecimal invalidSum = BigDecimal.ZERO; // 其他无效数据
        // 4. 遍历数据进行分类统计
        for (DynamicObject data : dataCollection) {
            BigDecimal value = data.getBigDecimal("lag1_data");
            if (value == null) {
                continue; // 跳过空值
            }
            // 根据区间进行分类累加
            if (value.compareTo(BigDecimal.ZERO) >= 0 &&
                    value.compareTo(new BigDecimal("4")) <= 0) {
                lowSum = lowSum.add(value);
            }
            else if (value.compareTo(new BigDecimal("4")) > 0 &&
                    value.compareTo(new BigDecimal("7")) <= 0) {
                midSum = midSum.add(value);
            }
            else if (value.compareTo(new BigDecimal("7")) > 0 &&
                    value.compareTo(new BigDecimal("10")) <= 0) {
                highSum = highSum.add(value);
            }
            else {
                invalidSum = invalidSum.add(value);
            }
        }

        // 5. 计算总和
        BigDecimal totalSum = lowSum.add(midSum).add(highSum).add(invalidSum);

        // 6. 计算百分比（处理除零情况）
        DecimalFormat df = new DecimalFormat("0.00");
        String lowPercent = "0.00";
        String midPercent = "0.00";
        String highPercent = "0.00";
        String invalidPercent = "0.00";

        if (totalSum.compareTo(BigDecimal.ZERO) != 0) {
            lowdata = df.format(lowSum.divide(totalSum, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            standard = df.format(midSum.divide(totalSum, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            highdata = df.format(highSum.divide(totalSum, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            invalidPercent = df.format(invalidSum.divide(totalSum, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
        }

        // 7. 输出结果
//        this.getView().showMessage("========== 数据分析报告 ==========");
//        System.out.println("数值总和: " + totalSum.setScale(2, RoundingMode.HALF_UP));
//        System.out.println("--------------------------------");
//        this.getView().showMessage("0-4区间: 总和=" + lowSum.setScale(2, RoundingMode.HALF_UP) + " | 占比=" + lowPercent + "%");
//        this.getView().showMessage("4-7区间: 总和=" + midSum.setScale(2, RoundingMode.HALF_UP) + " | 占比=" + midPercent + "%");
//        this.getView().showMessage("7-10区间: 总和=" + highSum.setScale(2, RoundingMode.HALF_UP) + " | 占比=" + highPercent + "%");
//        System.out.println("无效数据: 总和=" + invalidSum.setScale(2, RoundingMode.HALF_UP) + " | 占比=" + invalidPercent + "%");
//        System.out.println("=================================");
        // 8. 可视化展示（可选）
//        printVisualization(lowPercent, midPercent, highPercent, invalidPercent);
    }

//     可视化展示方法
//    private void printVisualization(String low, String mid, String high, String invalid) {
//        System.out.println("\n数据分布比例图:");
//        printBar("0-4", Double.parseDouble(low));
//        printBar("4-7", Double.parseDouble(mid));
//        printBar("7-10", Double.parseDouble(high));
//        printBar("无效", Double.parseDouble(invalid));
//    }
//
//    private void printBar(String label, double percent) {
//        System.out.printf("%-6s | ", label);
//        int bars = (int) (percent / 2); // 每2%显示一个█
//        System.out.print(String.format("%-" + 50 + "s", repeat("█", bars))
//                .replace(' ', ' '));
//        System.out.println(" " + percent + "%");
//    }

    private String repeat(String str, int times) {
        return new String(new char[times]).replace("\0", str);
    }
}