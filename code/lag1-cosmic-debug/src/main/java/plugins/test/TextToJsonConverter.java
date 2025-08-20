package plugins.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextToJsonConverter {

    public static void main(String[] args) {
        String inputText = "1 [马克思主义基本原理概述]\n" +
                "1.1 [马克思主义的科学内涵]\n" +
                "1.1.1 [马克思主义的定义](关于自然、社会和人类思维发展一般规律的学说)\n" +
                "1.1.2 [马克思主义的三个基本组成部分](马克思主义哲学、政治经济学、科学社会主义)\n" +
                "1.1.3 [马克思主义的根本性质](体现科学性和革命性的统一)\n" +
                "\n" +
                "1.2 [马克思主义的历史发展]\n" +
                "1.2.1 [马克思主义的创立背景](19世纪40年代资本主义发展和工人运动兴起)\n" +
                "1.2.2 [马克思主义的主要创始人](马克思、恩格斯的理论贡献)\n" +
                "1.2.3 [马克思主义的发展历程](从经典到现代的发展演变)\n" +
                "\n" +
                "1.3 [马克思主义的基本特征]\n" +
                "1.3.1 [科学性特征](揭示人类社会发展规律的科学理论)\n" +
                "1.3.2 [人民性特征](以人民为中心的理论体系)\n" +
                "1.3.3 [实践性特征](指导改造世界的行动指南)\n" +
                "\n" +
                "2 [世界的物质性及发展规律]\n" +
                "2.1 [世界的物质统一性]\n" +
                "2.1.1 [物质的哲学定义](标志客观实在的哲学范畴)\n" +
                "2.1.2 [物质的根本属性](运动是物质的存在方式)\n" +
                "2.1.3 [物质的存在形式](时间与空间是物质的基本存在形式)\n" +
                "\n" +
                "2.2 [物质世界的二重分化]\n" +
                "2.2.1 [自然界与人类社会的分化](人类劳动创造了社会存在)\n" +
                "2.2.2 [客观世界与主观世界的分化](人的意识活动形成主观世界)\n" +
                "2.2.3 [二重分化的意义](使世界更加丰富多样)\n" +
                "\n" +
                "2.3 [物质与意识的辩证关系]\n" +
                "2.3.1 [物质决定意识](物质是第一性存在的观点)\n" +
                "2.3.2 [意识对物质的能动作用](意识具有反作用的特征)\n" +
                "2.3.3 [辩证统一关系](物质与意识相互依存相互影响)\n" +
                "\n" +
                "3 [实践与认识及其发展规律]\n" +
                "3.1 [实践与认识的基本问题]\n" +
                "3.1.1 [实践的本质与结构](实践是人类能动地改造世界的客观物质活动)\n" +
                "3.1.2 [认识的本质与过程](认识是主体对客体的能动反映)\n" +
                "3.1.3 [实践与认识的辩证运动](实践决定认识，认识反作用于实践)\n" +
                "\n" +
                "3.2 [真理与价值]\n" +
                "3.2.1 [真理的客观性与相对性](真理具有客观标准但受历史条件限制)\n" +
                "3.2.2 [真理的检验标准](实践是检验真理的唯一标准)\n" +
                "3.2.3 [真理与价值的统一](真理追求和价值实现的统一)\n" +
                "\n" +
                "3.3 [认识世界与改造世界]\n" +
                "3.3.1 [认识的目的](改造世界是认识的根本目的)\n" +
                "3.3.2 [实事求是的原则](一切从实际出发、主观符合客观)\n" +
                "3.3.3 [理论创新与实践创新](形成良性互动关系)\n" +
                "\n" +
                "4 [人类社会及其发展规律]\n" +
                "4.1 [社会存在与社会意识]\n" +
                "4.1.1 [社会存在决定社会意识](社会存在是社会意识的决定因素)\n" +
                "4.1.2 [社会意识的相对独立性](社会意识对社会存在具有反作用)\n" +
                "4.1.3 [社会基本矛盾](生产力与生产关系、经济基础与上层建筑的矛盾)\n" +
                "\n" +
                "4.2 [社会历史发展的动力]\n" +
                "4.2.1 [社会基本矛盾的作用](推动社会发展的根本动力)\n" +
                "4.2.2 [阶级斗争的作用](阶级社会发展的直接动力)\n" +
                "4.2.3 [科学技术的作用](科技进步推动社会发展的重要力量)\n" +
                "\n" +
                "4.3 [人民群众在历史中的作用]\n" +
                "4.3.1 [人民群众是历史创造者](人民群众是社会历史的主体)\n" +
                "4.3.2 [个人在社会历史中的作用](个人作用的局限性和条件性)\n" +
                "4.3.3 [群众、阶级、政党、领袖的关系](相互作用的统一关系)\n" +
                "\n" +
                "5 [商品经济与资本主义经济制度]\n" +
                "5.1 [商品经济的基本问题]\n" +
                "5.1.1 [商品的二因素](使用价值和价值)\n" +
                "5.1.2 [劳动的二重性](具体劳动和抽象劳动)\n" +
                "5.1.3 [价值规律的作用](价值规律是商品经济的基本规律)\n" +
                "\n" +
                "5.2 [资本主义生产方式]\n" +
                "5.2.1 [劳动力成为商品](资本主义生产关系的基础)\n" +
                "5.2.2 [剩余价值的生产](资本家占有工人创造的剩余价值)\n" +
                "5.2.3 [资本主义基本矛盾](生产社会化与生产资料私有制的矛盾)\n" +
                "\n" +
                "5.3 [资本主义的本质与趋势]\n" +
                "5.3.1 [政治制度的本质](资产阶级专政的统治形式)\n" +
                "5.3.2 [意识形态的本质](为资本主义制度辩护的思想体系)\n" +
                "5.3.3 [资本主义历史地位](由盛到衰的历史必然性)\n" +
                "\n" +
                "6 [社会主义的发展及其规律]\n" +
                "6.1 [社会主义的历史进程]\n" +
                "6.1.1 [社会主义从空想到科学](科学社会主义理论的建立)\n" +
                "6.1.2 [从理想到现实的发展](十月革命实现社会主义制度)\n" +
                "6.1.3 [中国特色社会主义的实践](中国社会主义建设的历史成就)\n" +
                "\n" +
                "6.2 [科学社会主义基本原则]\n" +
                "6.2.1 [基本原则的主要内容](无产阶级专政、按劳分配等核心原则)\n" +
                "6.2.2 [正确理解基本原则](把握原则的普遍性和具体性)\n" +
                "6.2.3 [与中国特色社会主义结合](理论与实践的统一)\n" +
                "\n" +
                "6.3 [社会主义发展规律]\n" +
                "6.3.1 [发展道路的多样性](各国社会主义建设的不同模式)\n" +
                "6.3.2 [建设过程的长期性](社会主义建设需要长期过程)\n" +
                "6.3.3 [开拓前进的要求](坚持守正创新的发展道路)\n" +
                "\n" +
                "7 [共产主义崇高理想及其实现]\n" +
                "7.1 [共产主义社会的基本特征]\n" +
                "7.1.1 [社会生产力高度发展](物质财富极大丰富)\n" +
                "7.1.2 [按需分配制度](个人消费品的分配方式)\n" +
                "7.1.3 [人自由全面发展](人的全面自由发展)\n" +
                "\n" +
                "7.2 [共产主义实现的历史必然性]\n" +
                "7.2.1 [历史发展趋势](资本主义必然被社会主义取代)\n" +
                "7.2.2 [实现的长期过程](共产主义的实现是一个历史过程)\n" +
                "7.2.3 [实践探索要求](在实践中推进共产主义事业)\n" +
                "\n" +
                "7.3 [远大理想与共同理想的关系]\n" +
                "7.3.1 [辩证统一关系](远大理想与共同理想相互关联)\n" +
                "7.3.2 [理想信念要求](坚定共产主义远大理想和中国特色社会主义共同理想)\n" +
                "7.3.3 [新时代的使命担当](投身新时代中国特色社会主义建设实践)";

        String jsonResult = convertTextToJson(inputText);
        System.out.println(jsonResult);
    }

    public static String convertTextToJson(String inputText) {
        // 定义正则表达式匹配模式
//        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)*) \\$(.*?)\\$(?:\\$(.*?)\\$)?");
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)*) \\[(.*?)\\](?:\\((.*?)\\))?");
        // 分割输入文本为行
        String[] lines = inputText.split("\n");

        // 准备结果数据
        String knowpointPlan = "";
        List<KnowledgePoint> knowledgePoints = new ArrayList<>();

        // 处理每一行
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String chap = matcher.group(1);
                String knowpName = matcher.group(2);
                String description = matcher.group(3) != null ? matcher.group(3) : "";

                System.out.println(chap);
                System.out.println(knowpName);
                System.out.println(description);


                // 生成knpId（转换为整数形式，如1.1.1 -> 111）

                // 确定父节点ID
                String knowpointParent = getParentId(chap);

                // 确定节点层级
                String knowpExpand = String.valueOf(chap.split("\\.").length);

                // 创建知识点对象
                KnowledgePoint kp = new KnowledgePoint(
                        chap,
                        knowpName,
                        knowpointParent,
                        knowpExpand,
                        chap,
                        description
                );
                knowledgePoints.add(kp);
            }
        }

        // 构建JSON对象
        JSONObject result = new JSONObject();
        result.put("knowpoint_plan", knowpointPlan);

        JSONArray pointsArray = new JSONArray();
        for (KnowledgePoint kp : knowledgePoints) {
            JSONObject kpJson = new JSONObject();
            kpJson.put("knpId", kp.getKnpId());
            kpJson.put("knowpName", kp.getKnowpName());
            kpJson.put("knowpointParent", kp.getKnowpointParent());
            kpJson.put("knowpExpand", kp.getKnowpExpand());
            kpJson.put("chap", kp.getChap());
            kpJson.put("description", kp.getDescription());
            pointsArray.add(kpJson);
        }
        result.put("knowledgePoints", pointsArray);
        return result.toString(); // 缩进2个空格，美化输出
    }



    // 获取父节点ID
    private static String getParentId(String chap) {
        String[] parts = chap.split("\\.");
        if (parts.length <= 1) return "0"; // 顶级节点没有父节点

        // 获取父节点章节（去掉最后一部分）
        StringBuilder parentChap = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) parentChap.append(".");
            parentChap.append(parts[i]);
        }

        return parentChap.toString();
    }

    // 知识点内部类
    static class KnowledgePoint {
        private String knpId;
        private String knowpName;
        private String knowpointParent;
        private String knowpExpand;
        private String chap;
        private String description;

        public KnowledgePoint(String knpId, String knowpName, String knowpointParent,
                              String knowpExpand, String chap, String description) {
            this.knpId = knpId;
            this.knowpName = knowpName;
            this.knowpointParent = knowpointParent;
            this.knowpExpand = knowpExpand;
            this.chap = chap;
            this.description = description;
        }

        // Getter方法
        public String getKnpId() { return knpId; }
        public String getKnowpName() { return knowpName; }
        public String getKnowpointParent() { return knowpointParent; }
        public String getKnowpExpand() { return knowpExpand; }
        public String getChap() { return chap; }
        public String getDescription() { return description; }
    }
}