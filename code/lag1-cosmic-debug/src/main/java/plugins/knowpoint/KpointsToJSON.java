package plugins.knowpoint;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.control.Control;
import kd.sdk.plugin.Plugin;
import com.alibaba.fastjson.JSONObject;
import kd.bos.ext.form.control.Markdown;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.dataentity.entity.DynamicObject;

import java.io.*;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static plugins.AI.BookKnowpointAI.readFileContent;
import static plugins.AI.BookKnowpointAI.processSegment;
import static plugins.AI.BookKnowpointAI.splitText;
import static plugins.test.TextToJsonConverter.convertTextToJson;

/**
 * 单据界面插件
 */
public class KpointsToJSON extends AbstractBillPlugIn implements Plugin {
    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String API_KEY = "sk-wncrewiljnvfzovvsdrqpktfgtwwpbmyndhnhqscvytnvtza"; // 替换为你的API密钥
    private static final int TOKEN_LIMIT = 30000; // 30K Tokens (<=100K
    private static final int THREAD_COUNT = 1; // 5个线程
    public static final String MODEL_NAME = "Qwen/Qwen3-Coder-30B-A3B-Instruct";

    DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("customRegion");
    @Override
    public void registerListener(EventObject e) {
        // 注册点击事件
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }
    public void itemClick(ItemClickEvent e) {
        super.itemClick(e);
        Control source = (Control) e.getSource();
        if (e.getItemKey().equalsIgnoreCase("lag1_baritemap1")) {
            downloadAndDeleteAttachment();
        }
    }
    public void downloadAndDeleteAttachment() {
        // 1. 获取附件信息
        DynamicObjectCollection attachmentFields = (DynamicObjectCollection) this.getModel().getValue("lag1_attachmentfield");
        if (attachmentFields == null || attachmentFields.isEmpty()) {
            this.getView().showTipNotification("没有找到附件");
            return;
        }
        // 2. 准备本地路径
        String fileName = "";
        String fileUrl = "";
//        attachmentFields.get(1).getDynamicObject("fbasedataid").getString("name")
        // 遍历附件字段中的所有附件
        for (DynamicObject attachmentField : attachmentFields) {
            DynamicObject fileData = attachmentField.getDynamicObject("fbasedataid");
            String currentFileName = fileData.getString("name");
            fileUrl=fileData.getString("url");
            // 检查文件扩展名是否为.txt
            if (currentFileName != null && currentFileName.toLowerCase().endsWith(".txt")) {
                fileName = currentFileName; // 返回第一个找到的.txt文件
            }else{
                this.getView().showMessage("没有符合条件的教材文本文件哦！");
                return;
            }
        }
        if(fileName == ""){
            this.getView().showMessage("没有txt教材");
            return;
        }

        String filePath = "D:\\kingdee\\downloads\\" + fileName;
        File localFile = new File(filePath);
        // 3. 确保目录存在
        File parentDir = localFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            this.getView().showErrorNotification("创建目录失败");
            return;
        }
        // 4. 下载文件
        try (InputStream in = FileServiceFactory.getAttachmentFileService()
                .getInputStream(fileUrl);
             OutputStream out = new FileOutputStream(localFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            this.getView().showSuccessNotification("文件下载完成: " + localFile.getAbsolutePath());
            // 5. 使用文件并存入缓存
            processFile(localFile.getAbsolutePath());
            this.getView().showMessage("转换JSON成功");
        } catch (IOException e) {
            this.getView().showErrorNotification("文件操作失败: " + e.getMessage());
        } finally {
            // 6. 确保文件最终被删除
            if (localFile.exists()) {
                try {
                    localFile.delete();
                } catch (SecurityException e) {
                    // 记录日志但不影响主流程
//                    log.info("删除临时文件失败", e);
                }
            }
        }
    }

    private void processFile(String  filePath) throws IOException {
        // 这里实现您的文件处理逻辑
        // 例如：读取内容、解析数据等
//        String filePath = file.getAbsolutePath();
        String fileContent = readFileContent(filePath);
        // 分割文本
        String[] segments = splitText(fileContent, TOKEN_LIMIT);
        // 预览分割结果
        for (int i = 0; i < segments.length; i++) {
            System.out.println("Segment " + (i + 1) + ": " + segments[i].substring(0, Math.min(50, segments[i].length())));
        }
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        // 用于存储所有线程的结果
        StringBuilder allKnowledgePoints = new StringBuilder();
        // 提交任务到线程池
        for (int i = 0; i < 1; i++) {
            final int segmentIndex = i;
            executorService.submit(() -> {
                try {
                    String knowledgePoints = processSegment(segments[segmentIndex], segmentIndex + 1);
                    System.out.println(segmentIndex + " completed: \n" + knowledgePoints);
                    synchronized (allKnowledgePoints) {
                        allKnowledgePoints.append(knowledgePoints).append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        // 关闭线程池并等待所有任务完成
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String jsonKnp = convertTextToJson(String.valueOf(allKnowledgePoints));
        cache.put("damn", jsonKnp);
    }
}