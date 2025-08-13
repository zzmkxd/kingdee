package plugins.timer;

// package Unit10;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式进化版
 * 主题：超链接a标记匹配
 * zjh@scnu
 **/
import java.io.File;
import java.io.FileInputStream;
// import java.nio.charset.StandardCharsets;
// import java.util.Scanner;

public class Lag1_1 {
    public static void main(String[] args) throws Exception {
        String fileContent = getFileContent();
        System.out.println(fileContent);
    }

    /**
     * 获取文件中的字符串
     * @return
     * @throws Exception
     */
    private static String getFileContent() throws Exception{
        File file = new File("E:\\Tool_zone\\Tencent\\WeChat\\xwechat_files\\wxid_xx1zzo5hut8e22_8c04\\msg\\file\\2025-07\\教案生成模板(2).xml");
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