package plugins.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.fileservice.FileItem;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.gpt.IGPTAction;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.url.UrlService;
import kd.bos.util.FileNameUtils;
import kd.sdk.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态表单插件
 */
public class AiTeachPlan implements IGPTAction {
    @Override
    public Map<String, String> invokeAction(String action, Map<String, String> params) {
        Map<String , String> result = new HashMap<>();
        if ("GENERATE_FILE_DOC".equalsIgnoreCase(action)) {
                //获取文件字符串
            String fileContent = null;
            try {
                fileContent = getFileContent();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //获取传入参数
            String jsonResult = params.get("damn").replaceAll("\\s*|\r|\n|\t","");
//                为我生成计算机网络（计算机学科）的教案，共计4个课时的课程，涉及的核心知识点是TCP/IP,DHCP动态分配IP,网络互通,外网端口映射
            JSONObject resultJsonObject = null;
            try {
                //若全部生成JSON字符串，则不会进入catch
                resultJsonObject = JSON.parseObject(jsonResult);
            } catch (Exception ee) {
                jsonResult = jsonResult.substring(jsonResult.indexOf("\"course_name\"")-1, jsonResult.indexOf("}]}")+3);
                resultJsonObject = JSON.parseObject(jsonResult);
            }
            //替代表格中的内容
            int time = 1;
            for(Object object : resultJsonObject.getJSONArray("teaching_plan")){
                JSONObject jsonObjectSingle = (JSONObject) object;
                fileContent = fileContent.replace("lesson"+time+"",jsonObjectSingle.getString("lesson"))
                        .replace("objectives"+time+"",jsonObjectSingle.getString("objectives"))
                        .replace("content"+time+"",jsonObjectSingle.getString("content"))
                        .replace("activities"+time+"",jsonObjectSingle.getString("activities"))
                        .replace("assessment"+time+"",jsonObjectSingle.getString("assessment"));
                time++;
            }

                fileContent = fileContent.replace("course_name", resultJsonObject.getString("course_name"));
                fileContent = fileContent.replace("topic_title", resultJsonObject.getString("topic_title"));
                fileContent = fileContent.replace("subject", resultJsonObject.getString("subject"));
                fileContent = fileContent.replace("class_hours", resultJsonObject.getString("class_hours"));
                fileContent = fileContent.replace("knowledge_objectives", resultJsonObject.getString("knowledge_objectives"));
                fileContent = fileContent.replace("ability_objectives", resultJsonObject.getString("ability_objectives"));
                fileContent = fileContent.replace("key_points", resultJsonObject.getString("key_points"));
                fileContent = fileContent.replace("difficult_points", resultJsonObject.getString("difficult_points"));
                fileContent = fileContent.replace("digital_resources", resultJsonObject.getString("digital_resources"));
                fileContent = fileContent.replace("preparation_requirements", resultJsonObject.getString("preparation_requirements"));
                fileContent = fileContent.replace("practice_activities", resultJsonObject.getString("practice_activities"));
                fileContent = fileContent.replace("extension_activities",resultJsonObject.getString("extension_activities"));
                fileContent = fileContent.replace("classroom_feedback", resultJsonObject.getString("classroom_feedback"));
                fileContent = fileContent.replace("assessment_methods", resultJsonObject.getString("assessment_methods"));

            //随机生成文件名称
            StringBuilder sb = new StringBuilder();
            for (int i = 1 ; i<=12; i++) {
                int ascii = 48+(int)(Math.random()*9);
                char c = (char) ascii;
                sb.append(c);
            }

            //创建一个临时文件，这里可以直接命名为docx文档
            File targetFile = null;
            try {
                targetFile = File.createTempFile(sb.toString(), ".docx");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!targetFile.exists()) {
                try {
                    targetFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //将字符串写入文件
            byte[] bytes =fileContent.getBytes();
            try {
                FileOutputStream fos=new FileOutputStream(targetFile);
                fos.write(bytes);
                //获取到文件服务器，并将文件上传至文件服务器
                FileService fs = FileServiceFactory.getAttachmentFileService();
                // 1. 生成路径
                RequestContext ctx = RequestContext.get();
                String path = "/User/DayEvaluate/"+ targetFile.getName();
                FileItem fi = new FileItem(targetFile.getName(), path, new FileInputStream(targetFile));
                fi.setCreateNewFileWhenExists(true);
                //获取到文件路径
                path= fs.upload(fi);
                //拼接URL，将最终的URL输出
                result.put("endUrl", System.getProperty("domain.contextUrl")+
                        "/attachment/download.do?path="+path+
                        "&method=autoJump&title=教案.docx&iconType=document");
                targetFile.delete();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取文件中的字符串
     * @return
     * @throws Exception
     */
    private static String getFileContent() throws Exception{
        File file = new File("D:\\教案生成模板.xml");
        if(!file.exists()){
            return null;
        }
        FileInputStream inputStream = new FileInputStream(file);
        int length = inputStream.available();
        byte bytes[] = new byte[length];
        inputStream.read(bytes);
        inputStream.close();
        String str =new String(bytes, StandardCharsets.UTF_8);
        return str ;
    }
}