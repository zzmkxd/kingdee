package plugins.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.fileservice.FileItem;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.gpt.IGPTAction;
import kd.bos.form.plugin.AbstractFormPlugin;
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
            try {
                //获取文件字符串
                String fileContent = getFileContent();
                //获取传入参数
                String jsonResult = params.get("jsonResult").replaceAll("\\s*|\r|\n|\t","");
                JSONObject resultJsonObject = null;
                try {
                    //若全部生成JSON字符串，则不会进入catch
                    resultJsonObject = JSON.parseObject(jsonResult);
                } catch (Exception ee) {
                    //将"dayname"的上一个字符作为开始，以}]}字符作为结束，则最后需要+3
                    jsonResult = jsonResult.substring(jsonResult.indexOf("\"course_name\"")-1 , jsonResult.indexOf("}]}")+3);
                    resultJsonObject = JSON.parseObject(jsonResult);
                }
                //将jsonResult转为JSONArray
//                JSONArray jsonArrayData = JSONArray.parseArray(jsonResult);
                Object object = jsonResult;
//                StringBuilder stringBuilder = new StringBuilder();
//                //将数据加入图表
//                for (int i = 0 ;i<jsonArrayData.size(); i++) {
//                    JSONArray jsonArraySingle = (JSONArray) jsonArrayData.get(i);
//                    if (i==jsonArrayData.size()-1) {
//                        stringBuilder.append("[&quot;").append(jsonArraySingle.getString(0)).append("&quot;").append(",").append(jsonArraySingle.getString(1)).append(",").append(jsonArraySingle.getString(2)).append("]");
//
//                    } else {
//                        stringBuilder.append("[&quot;").append(jsonArraySingle.getString(0)).append("&quot;").append(",").append(jsonArraySingle.getString(1)).append(",").append(jsonArraySingle.getString(2)).append("],");
//                    }
//                }
//                String statisticsResult = stringBuilder.toString();
//                //替代最后的生成结果
//                fileContent = fileContent.replace("{{teachplanData}}", statisticsResult);
                JSONObject jsonObject = (JSONObject) object;
                //替代表格中的内容
                for (int time = 1; time <= 4 ; time++) {
                    JSONObject jsonObjectSingle = JSONObject.parseObject(fileContent).getJSONObject("teaching_plan");
                    fileContent = fileContent.replace("{{lesson"+time+"}}",jsonObjectSingle.getString("lesson"+time))
                            .replace("{{objectives"+time+"}}",jsonObjectSingle.getString("objectives"+time))
                            .replace("{{content"+time+"}}",jsonObjectSingle.getString("content"+time))
                            .replace("{{activities"+time+"}}",jsonObjectSingle.getString("activities"+time))
                            .replace("{{assessment"+time+"}}",jsonObjectSingle.getString("assessment"+time));
                }

                //替代GPT提示中生成的内容
                fileContent = fileContent.replace("{{course_name}}", params.get("course_name"));
                fileContent = fileContent.replace("{{topic_title}}", params.get("topic_title"));
                fileContent = fileContent.replace("{{subject}}", params.get("subject"));
                fileContent = fileContent.replace("{{class_hours}}", params.get("class_hours"));
                fileContent = fileContent.replace("{{knowledge_objectives}}", params.get("knowledge_objectives"));
                fileContent = fileContent.replace("{{ability_objectives}}", params.get("ability_objectives"));
                fileContent = fileContent.replace("{{key_points}}", params.get("key_points"));
                fileContent = fileContent.replace("{{difficult_points}}", params.get("difficult_points"));
                fileContent = fileContent.replace("{{digital_resources}}", params.get("digital_resources"));
                fileContent = fileContent.replace("{{preparation_requirements}}", params.get("preparation_requirements"));
                fileContent = fileContent.replace("{{practice_activities}}", params.get("practice_activities"));
                fileContent = fileContent.replace("{{extension_activities}}", params.get("extension_activities"));
                fileContent = fileContent.replace("{{classroom_feedback}}", params.get("classroom_feedback"));
                fileContent = fileContent.replace("{{assessment_methods}}", params.get("assessment_methods"));

                resultJsonObject.getJSONArray("dayTaskList");
                //随机生成文件名称
                StringBuilder sb = new StringBuilder();
                for (int i = 1 ; i<=12; i++) {
                    int ascii = 48+(int)(Math.random()*9);
                    char c = (char) ascii;
                    sb.append(c);
                }
                //创建一个临时文件，这里可以直接命名为docx文档
                File targetFile = File.createTempFile(sb.toString(), ".docx");
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
                    String path = "/User/DayEvaluate/"+targetFile.getName();
                    FileItem fi = new FileItem(targetFile.getName(), path, new FileInputStream(targetFile));
                    fi.setCreateNewFileWhenExists(true);
                    //获取到文件路径
                    path= fs.upload(fi);
                    //拼接URL，将最终的URL输出
                    result.put("endUrl", System.getProperty("domain.contextUrl")+"/attachment/download.do?path="+path+"&method=autoJump&title=教案.docx&iconType=document");

                    targetFile.delete();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {

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
        File file = new File("D:/教案生成模板.xml");
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