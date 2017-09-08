package simhash;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.io.Files;
import com.hankcs.hanlp.HanLP;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by sssd on 2017/9/7.
 */
public class hashSimilary {
    public static void main(String[] args) {
        String testContent = "（原标题：初中女生一学期旷课28天 开学被要求签安全承诺书） \" +\n" +
                "                \"9月1日，本是开学报名的日子，但上初二的秦同学却遭到学校“拒绝”。\" +\n" +
                "                \"班主任告诉她，因旷课太多，需要家长到学校签保证书，并要学校主管领导签字才可以上学。 \" +\n" +
                "                \"家长：女儿因常旷课 遭学校拒绝报名 “开学女儿去学校报名时，\" +\n" +
                "                \"班主任说女儿经常旷课不能报名，要我周一（9月4日）去学校写保证书，\" +\n" +
                "                \"还要找学校主管领导签字。”秦先生说。 秦先生介绍，几年前他与妻子离异，\" +\n" +
                "                \"女儿由他独自抚养。他老家是河南的，在汉中做装修生意，平时比较忙，无暇照看女儿。\" +\n" +
                "                \"女儿一直在汉中市南郑县大河坎九年制学校上学，今年9月份开学就该上初二了。\" +\n" +
                "                \"女儿学习成绩还不错";
        String[] strings = testContent.split("。|，");
        String path = "C:\\Users\\sssd\\Desktop\\textsimilary\\test\\news.txt";

        try {
            List<String> sentenceList1 = HanLP.extractSummary(testContent, 6);
            String str1 = sentenceList1.toString().replaceAll("[^\u4e00-\u9fa5]", "");
            SimHash testHash = new SimHash(str1, 128);
            List<String> readLines = Files.readLines(new File(path), Charset.forName("utf-8"));
            int index = 0;
            for (String line : readLines){
                if (StringUtils.isNotEmpty(line)){
                    index ++;
                    JSONObject jsonObject = (JSONObject) JSON.parse(line);
                    String content = (String) jsonObject.get("content");
                    if (StringUtils.isNotBlank(content)){
                        List<String> sentenceList2 = HanLP.extractSummary(content, 6);
                        String str2 = sentenceList2.toString().replaceAll("[^\u4e00-\u9fa5]", "");
                        SimHash hash1 = new SimHash(str2, 128);
                        int distance = testHash.hammingDistance(hash1);
                        if (distance < 10){
                            String source = (String) jsonObject.get("entitySectionName");
                            System.out.println("发现有类似新闻来源：" + source + " 在第 " + index + "行" + "海明距离为：" + distance );
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
