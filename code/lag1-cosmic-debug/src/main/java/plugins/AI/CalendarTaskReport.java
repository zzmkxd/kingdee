package plugins.AI;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.fileservice.FileItem;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.gpt.IGPTAction;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态表单插件
 */
public class CalendarTaskReport implements IGPTAction {
    @Override
    public Map<String, String> invokeAction(String action, Map<String, String> params) {
        Map<String , String> result = new HashMap<>();
        if ("GENERATE_FILE_DOC".equalsIgnoreCase(action)) {
            try {
                //获取文件字符串
                String fileContent = getFileContent();
                //获取传入参数
                String statisticsData = params.get("statisticsData");
                String dayDateObject = params.get("dayDataObject");
                //将statisticsData转为JSONArray
                JSONArray jsonArrayData = JSONArray.parseArray(statisticsData);
                StringBuilder stringBuilder = new StringBuilder();
                //将数据加入图表
                for (int i = 0 ;i<jsonArrayData.size(); i++) {
                    JSONArray jsonArraySingle = (JSONArray) jsonArrayData.get(i);
                    if (i==jsonArrayData.size()-1) {
                        stringBuilder.append("[&quot;").append(jsonArraySingle.getString(0)).append("&quot;").append(",").append(jsonArraySingle.getString(1)).append(",").append(jsonArraySingle.getString(2)).append("]");

                    } else {
                        stringBuilder.append("[&quot;").append(jsonArraySingle.getString(0)).append("&quot;").append(",").append(jsonArraySingle.getString(1)).append(",").append(jsonArraySingle.getString(2)).append("],");
                    }
                }
                String statisticsResult = stringBuilder.toString();
                //替代最后的生成结果
                fileContent = fileContent.replace("{{statisticsData}}", statisticsResult);

                //替代表格中的内容
                for (int day = 1; day <= 3 ; day++) {
                    JSONObject jsonObjectSingle = JSONObject.parseObject(dayDateObject).getJSONObject("day"+day+"Data");
                    fileContent = fileContent.replace("{{day"+day+"}}",jsonObjectSingle.getString("day"+day))
                            .replace("{{day"+day+"Expect}}",jsonObjectSingle.getInteger("day"+day+"Expect")+"")
                            .replace("{{day"+day+"Finish}}",jsonObjectSingle.getInteger("day"+day+"Finish")+"")
                            .replace("{{day"+day+"IsOvertime}}",jsonObjectSingle.getString("day"+day+"IsOvertime"));
                }

                //替代GPT提示中生成的内容
                fileContent = fileContent.replace("{{dayEvaluate}}", params.get("dayPrompt"));
                fileContent = fileContent.replace("{{threeDayFinishTimeEvaluate}}", params.get("threeDayPrompt"));

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
        File file = new File("D:/日任务完成报告.xml");
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