package plugins.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class FaceResultParser {
    /**
     * 解析百度返回的 JSON
     * @param jsonStr BaiduFaceForKd.doRecognize(...) 得到的原始字符串
     * @return 封装后的结果，解析失败返回 null
     */
    public static FaceResult parse(String jsonStr) {
        try {
            JSONObject root = JSON.parseObject(jsonStr);
            if (root.getIntValue("error_code") != 0) {
                return null;
            }
            JSONObject result = root.getJSONObject("result");
            if (result == null) return null;

            JSONObject user = result.getJSONArray("user_list").getJSONObject(0);
            FaceResult r = new FaceResult();
            r.userId   = user.getString("user_id");
            r.userInfo = user.getString("user_info");
            r.score    = user.getFloatValue("score");
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断是否注册成功
     * @param jsonStr 原始返回串
     * @return true 成功；false 失败
     */
    public static boolean isSuccess(String jsonStr) {
        try {
            JSONObject root = JSON.parseObject(jsonStr);
            return root.getIntValue("error_code") == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static class FaceResult {
        public String userId;
        public String userInfo;
        public float  score;
    }
}