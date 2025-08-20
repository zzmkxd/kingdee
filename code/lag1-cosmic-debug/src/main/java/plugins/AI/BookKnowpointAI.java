package plugins.AI;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @说明 TODO
 * @Author Tian_Yumi
 * @Date 2025/8/17 11:37
 * @Version 1.0
 */

public class BookKnowpointAI {

    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String API_KEY = "sk-wncrewiljnvfzovvsdrqpktfgtwwpbmyndhnhqscvytnvtza"; // 替换为你的API密钥
    private static final int TOKEN_LIMIT = 30000; // 30K Tokens (<=100K
    private static final int THREAD_COUNT = 1; // 5个线程
    public static final String MODEL_NAME = "Qwen/Qwen3-Coder-30B-A3B-Instruct";

    public static void main(String[] args) {

        String filePath = "D:\\桌面\\· 马克思主义理论研究和建设工程重点教材 ·.txt";
//        String filePath = "D:\\桌面\\Java EE企业级应用开发教程（Spring+Spring MVC+MyBatis）（第2版） (黑马程序员) (Z-Library).txt"; // 替换为你的文件路径
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
        // 输出最终结果
        System.err.println("最终结果：" + allKnowledgePoints);
    }

    private static String readFileContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static String[] splitText(String text, int tokenLimit) {
        if (text.isEmpty()) {
            return new String[0];
        }

        // 只截取前 tokenLimit 个字符
        String segment = text.substring(0, Math.min(tokenLimit, text.length()));

        return new String[]{segment};
    }
    private static String processSegment(String segment, int segmentIndex) {
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建JSON请求体
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("model", MODEL_NAME);

        ObjectNode messageNode = objectMapper.createObjectNode();
        messageNode.put("role", "user");
        messageNode.put("content", "在下面的数万字大学教材切片找到目录，整理目录粗略对应的知识点。" +
                "格式要求：序号[知识点名称]:(知识点简要描述)，知识点分别以a a.b a.b.c 的格式编号(其中abc都代表阿拉伯数字)，分别代表一 二 三 层级的知识点，示例：1 2.3 6.1.2；" +
                "内容要求：将知识点分为3个层级，如果目录只有第一、第二两个层级，没有第三个层级，可以概括两到三个教材目录或标题，来作为大层级知识点，并且在每个第二层级下自己填充第三层级的知识点。" +
                "每个第二层级最多填充3个第三层级知识点(a.b.c中c<=3)。如果目录多于2个层级，可概括目录或标题的第一、第二层级作为知识点的第一第二层级，但必须把第二层级数量控制在4个以内(a.b中b<=4)，并且在每个第二层级下自己填充第三层级的知识点。" +
                "数量要求：一二层级数量小于对应的教材目录，每个第二层级最多填充3个第三层级知识点(a.b.c中c<=3)。" +
                "示例输出：1 [初识框架]\n" +
                "1.1 [框架概述]\n" +
                "1.1.1 [框架的优势](提高开发效率、代码规范性和软件性能)\n" +
                "1.1.2 [当前主流框架](Spring框架、Hibernate框架、Struts2框架)\n" +
                "要求用中文回答；一个知识点一行。" +
                "现在正式开始工作，除了格式要求的内容：序号[知识点名称]:(知识点简要描述) 以外不要输出多余的任何内容。" +
                "教材文本：" + segment);

        rootNode.set("messages", objectMapper.createArrayNode().add(messageNode));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建POST请求
            HttpPost post = new HttpPost(API_URL);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + API_KEY);
            post.setEntity(new StringEntity(rootNode.toString(), "UTF-8"));

            // 发送请求并获取响应
            org.apache.http.HttpResponse response = httpClient.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            // 调试：打印API响应内容
//          System.out.println("API Response: " + responseBody);

            // 解析响应并提取知识点内容
            JsonNode responseJson = objectMapper.readTree(responseBody);
            String content = responseJson.path("choices").get(0).path("message").path("content").asText();

            return content; // 返回知识点内容

        } catch (IOException e) {
            e.printStackTrace();
            return "Error processing segment: " + e.getMessage();
        }
    }
}