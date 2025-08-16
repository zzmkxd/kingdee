package plugins.recomanddata;

import com.opencsv.CSVReader;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class QuestionRecommender {

    // 题目对象
    static class Question {
        public double sim;
        String id;           // number
        String stem;         // lag1_prodes
        String standardAnswer; // lag1_standard_answer
        String optionA;     // lag1_itema
        String optionB;     // lag1_itemb
        String optionC;     // lag1_itemc
        String optionD;     // lag1_itemd
        String mergedText;  // 题干 + 选项合并文本
        double[] vector;
    }

    // Word2Vec 词向量字典
    static Map<String, double[]> wordVecMap = new HashMap<>();
    static int vecDim = 0;


    // 停用词表
    // 停用词表（从文件加载）
    static final Set<String> STOP_WORDS = loadStopWords("data/stopwords_baidu.txt");

    private static Set<String> loadStopWords(String path) {
        Set<String> stopwords = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    stopwords.add(word);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopwords;
    }


    public static void main(String[] args) throws Exception {
        String csvPath = "data/lag1_protest.csv";
        String word2vecPath = "data/45000-small.txt";

        // 加载词向量
        loadWord2Vec(word2vecPath);

        // 读取题目
        List<Question> questions = loadQuestions(csvPath);

        // 计算所有题目的向量
        for (Question q : questions) {
            q.vector = getSentenceVector(q.mergedText);
        }

        // 测试：传入题目ID推荐
        String targetId = "Question001"; // 测试ID

        // 创建要排除的题目ID列表
        List<String> excludeIds = Arrays.asList("Question019", "Question050","Question025","Question022","Question003");

        // 使用排除列表进行推荐
        List<Question> recs = recommendSimilarQuestions(targetId, questions, 3, excludeIds);

        // 打印推荐结果
        System.out.println("=== 推荐结果 ===");
        for (Question q : recs) {
            System.out.println("题目ID: " + q.id);
            System.out.println("相似度: " + q.sim);
            System.out.println("原始文本: " + q.mergedText);
            System.out.println("-------------------");
        }
    }

    // 读取CSV文件
    public static List<Question> loadQuestions(String csvPath) throws Exception {
        List<Question> list = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8))) {
            String[] line;
            boolean first = true;
            while ((line = reader.readNext()) != null) {
                if (first) {
                    first = false;
                    continue;
                } // 跳过表头
                Question q = new Question();
                q.id = line[0].trim();              // number
                q.stem = line[1].trim();            // lag1_prodes
                q.standardAnswer = line[3].trim();  // lag1_standard_answer
                q.optionA = line.length > 4 ? line[4].trim() : "";  // lag1_itema
                q.optionB = line.length > 5 ? line[5].trim() : "";  // lag1_itemb
                q.optionC = line.length > 6 ? line[6].trim() : "";  // lag1_itemc
                q.optionD = line.length > 7 ? line[7].trim() : "";  // lag1_itemd

                // 合并题干 + 标准答案 + 选项
                q.mergedText = q.stem;

                // 加入标准答案（对所有题型都有益）
                if (!q.standardAnswer.isEmpty()) q.mergedText += " " + q.standardAnswer;

                // 判断是否为选择题（有选项的题目）
                boolean hasOptions = !q.optionA.isEmpty() || !q.optionB.isEmpty() ||
                        !q.optionC.isEmpty() || !q.optionD.isEmpty();

                // 加入选项（如果有）
                if(hasOptions) {
                    if (!q.optionA.isEmpty()) q.mergedText += " " + q.optionA;
                    if (!q.optionB.isEmpty()) q.mergedText += " " + q.optionB;
                    if (!q.optionC.isEmpty()) q.mergedText += " " + q.optionC;
                    if (!q.optionD.isEmpty()) q.mergedText += " " + q.optionD;
                }

                list.add(q);
            }
        }
        return list;
    }

    // 加载Word2Vec模型
    public static void loadWord2Vec(String path) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (first) {
                    try {
                        vecDim = Integer.parseInt(parts[1]);
                        first = false;
                        continue;
                    } catch (NumberFormatException e) {
                        vecDim = parts.length - 1;
                        first = false;
                    }
                }
                String word = parts[0];
                double[] vec = new double[vecDim];
                for (int i = 0; i < vecDim; i++) {
                    vec[i] = Double.parseDouble(parts[i + 1]);
                }
                wordVecMap.put(word, vec);
            }
        }
        System.out.println("已加载词向量：" + wordVecMap.size() + " 词, 维度=" + vecDim);
    }

    static JiebaSegmenter segmenter = new JiebaSegmenter();

    // 分词 + 停用词过滤
    public static List<String> segmentAndFilter(String text) {
        List<SegToken> tokens = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
        List<String> result = new ArrayList<>();
        for (SegToken token : tokens) {
            String w = token.word.trim();
            if (!STOP_WORDS.contains(w) && !w.matches("\\p{Punct}") && !w.isEmpty()) {
                result.add(w);
            }
        }
        return result;
    }


    // 获取句子向量（取分词后的词向量平均值）
    public static double[] getSentenceVector(String text) {
        List<String> tokens = segmentAndFilter(text);
        List<double[]> vecs = new ArrayList<>();
        for (String token : tokens) {
            double[] vec = wordVecMap.get(token);
            if (vec != null) {
                vecs.add(vec);
            }
        }
        if (vecs.isEmpty()) return new double[vecDim];
        double[] avg = new double[vecDim];
        for (double[] v : vecs) {
            for (int i = 0; i < vecDim; i++) {
                avg[i] += v[i];
            }
        }
        for (int i = 0; i < vecDim; i++) {
            avg[i] /= vecs.size();
        }
        return avg;
    }

    // 余弦相似度
    public static double cosineSim(double[] v1, double[] v2) {
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // 推荐函数
    public static List<Question> recommendSimilarQuestions(String qid, List<Question> all, int topN) {
        return recommendSimilarQuestions(qid, all, topN, Collections.emptyList());
    }

    // 推荐函数（带排除列表）
    public static List<Question> recommendSimilarQuestions(String qid, List<Question> all, int topN, List<String> excludeIds) {
        Question target = all.stream().filter(q -> q.id.equals(qid)).findFirst().orElse(null);
        if (target == null) {
            System.err.println("未找到题目ID: " + qid);
            return Collections.emptyList();
        }
        System.out.println("调试：分词前文本 => " + target.mergedText);

        // 创建一个包含目标ID和排除ID的集合
        Set<String> allExcludeIds = new HashSet<>();
        allExcludeIds.add(qid); // 添加目标ID（不推荐自己）
        if (excludeIds != null) {
            allExcludeIds.addAll(excludeIds); // 添加所有需要排除的ID
        }

        System.out.println("排除的题目ID: " + allExcludeIds);

        all.stream()
                .filter(q -> !allExcludeIds.contains(q.id))
                .forEach(q -> q.sim = cosineSim(q.vector, target.vector));  // 赋相似度

        return all.stream()
                .filter(q -> !allExcludeIds.contains(q.id))
                .sorted((a, b) -> Double.compare(b.sim, a.sim))  // 根据 sim 排序
                .limit(topN)
                .collect(Collectors.toList());
    }
}
