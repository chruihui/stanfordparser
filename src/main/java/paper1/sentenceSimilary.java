package paper1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.io.Files;
import com.hankcs.hanlp.HanLP;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sssd on 2017/9/7.
 */
public class sentenceSimilary {
    public static void main(String[] args) {
        String testContent = "（原标题：初中女生一学期旷课28天 开学被要求签安全承诺书） " +
                "9月1日，本是开学报名的日子，但上初二的秦同学却遭到学校“拒绝”。" +
                "班主任告诉她，因旷课太多，需要家长到学校签保证书，并要学校主管领导签字才可以上学。 " +
                "家长：女儿因常旷课 遭学校拒绝报名 “开学女儿去学校报名时，" +
                "班主任说女儿经常旷课不能报名，要我周一（9月4日）去学校写保证书，" +
                "还要找学校主管领导签字。”秦先生说。 秦先生介绍，几年前他与妻子离异，" +
                "女儿由他独自抚养。他老家是河南的，在汉中做装修生意，平时比较忙，无暇照看女儿。" +
                "女儿一直在汉中市南郑县大河坎九年制学校上学，今年9月份开学就该上初二了。" +
                "女儿学习成绩还不错，";
        String path = "C:\\Users\\sssd\\Desktop\\textsimilary\\test\\news.txt";

        String modelpath = "edu/stanford/nlp/models/lexparser/xinhuaFactoredSegmenting.ser.gz";
        String stopPath = "src/main/resources/stopWords.ml";
        StanfordParser stanfordParser = new StanfordParser(modelpath, stopPath);
        try {
            List<String> sentenceList1 = HanLP.extractSummary(testContent, 2);
//            System.out.println("测试句提出的摘要为：" + sentenceList1);
            String str1 = StringUtils.strip(sentenceList1.toString(),"[]");
            List<String> readLines = Files.readLines(new File(path), Charset.forName("utf-8"));
            ArrayList<Double> allList = new ArrayList<Double>();
            int numLine = 0;
            for (String line : readLines){
                if (StringUtils.isNotBlank(line)){
                    numLine++;
                    System.out.println("开始分析：" + numLine);
                    JSONObject jsonObject = (JSONObject) JSON.parse(line);
                    String content = (String) jsonObject.get("content");
                    if (StringUtils.isNotBlank(content)){
                        List<String> sentenceList2 = HanLP.extractSummary(content, 2);
                        String str2 = StringUtils.strip(sentenceList2.toString(),"[]");
                        System.out.println("摘要为：" + str2);
                        Double result = stanfordParser.textSimilarity(str1, str2);
                        allList.add(result);
                    }
                }
            }
            Double allMax = 0.0;
            int index = 0;
            for (int i = 0; i<allList.size(); i++){
                if (allList.get(i) > allMax){
                    allMax = allList.get(i);
                    index = i;
                }
            }
            String result = readLines.get(index);
            JSONObject parse = (JSONObject) JSON.parse(result);
            String source = (String) parse.get("entitySectionName");
            System.out.println("发现有类似新闻来源：" + source + "在第" + (index+1) + "行，相似度为：" + allMax);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
