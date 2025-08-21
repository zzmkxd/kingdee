package plugins.recomanddata;

import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.mq.MQFactory;
import kd.bos.mq.MessagePublisher;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import plugins.MQ.MulThreadsEdit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import plugins.MQ.MulThreadsEdit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class WriteCSV  extends AbstractBasePlugIn implements Plugin {
/*
lag1_protest

number
lag1_prodes
lag1_difficulty
lag1_standard_answer

//如果有
lag1_itema
lag1_itemb
lag1_itemc
lag1_itemd

lag1_standard_answer
*/
    @Override
    public void registerListener(EventObject e){
        // 注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("toolbarap");
    }
    Log Logger = LogFactory.getLog(MulThreadsEdit.class);
    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if ("lag1_baritemap".equals(itemKey)) {
            toCSV();
            this.getView().showMessage("导出成功，路径为 E:/lag1_protest.csv ");
        }
    }
    public void toCSV() {
        // 1. 查询数据
        DynamicObject[] protests = BusinessDataServiceHelper.load(
                "lag1_protest",
                "number,lag1_prodes,lag1_difficulty,lag1_standard_answer,lag1_itema,lag1_itemb,lag1_itemc,lag1_itemd",
                null
        );

        if (protests == null || protests.length == 0) {
            System.out.println("未找到数据");
            return;
        }

        // 2. 定义CSV文件路径
        String filePath = "E:/lag1_protest.csv";

        // 3. 使用BufferedWriter和Stream API处理CSV内容
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 写入CSV头部
            writer.write("number,lag1_prodes,lag1_difficulty,lag1_standard_answer,lag1_itema,lag1_itemb,lag1_itemc,lag1_itemd");
            writer.newLine();

            // 处理每一行数据
            for (DynamicObject protest : protests) {
                // 收集一行中的所有字段
                List<String> rowData = new ArrayList<>();
                rowData.add(protest.getString("number"));
                rowData.add(protest.getString("lag1_prodes"));
                rowData.add(protest.getString("lag1_difficulty"));
                rowData.add(protest.getString("lag1_standard_answer"));
                rowData.add(protest.getString("lag1_itema"));
                rowData.add(protest.getString("lag1_itemb"));
                rowData.add(protest.getString("lag1_itemc"));
                rowData.add(protest.getString("lag1_itemd"));

                // 使用Stream API处理CSV行
                String csvLine = rowData.stream()
                        .map(s -> s == null ? "" : (s.contains(",") ? "\"" + s.replace("\"", "\"\"") + "\"" : s))
                        .collect(Collectors.joining(","));

                writer.write(csvLine);
                writer.newLine();
            }

            System.out.println("CSV 文件已生成：" + filePath);
        } catch (IOException e) {
            System.err.println("保存文件失败：" + e.getMessage());
        }
    }
}
