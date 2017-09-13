package paper2;

import paper2.cilin.CiLin;
import com.hankcs.hanlp.HanLP;
import java.util.*;

/**
 * Created by sssd on 2017/9/9.
 */
public class MarkovSimilary {

    /**
     *  获取相同的公共词语位置
     * @param text2
     * @param text1
     * @return
     */
    public static HashMap<Integer, Integer> getCommonMap(List<String> text2, List<String> text1) {
        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        String str1 = null;
        String str2 = null;
        double simNum = 0.8;
        HashSet<Integer> hashset;
        double maxNum = 0.0;
        int index = 0;
        // 获取最接近的同义词
        for (int i = 0; i < text1.size(); i++) {
            str1 = text1.get(i);
            for (int j = 0; j < text2.size(); j++) {
                str2 = text2.get(j);
                double sim = CiLin.calcWordsSimilarity(str1, str2);
                if (sim > maxNum){
                    maxNum = sim;
                    index = j;
                }
            }
            if (!hashMap.containsValue(index)){
                hashMap.put(i,index);
            }
            maxNum = 0.0;
        }
        return hashMap;
    }

    /**
     *  权重值向量
     * @param text1
     * @param text2
     * @param hashMap
     * @return
     */
    public static ArrayList<ArrayList<Double>> getWeight(List<String> text1, List<String> text2, HashMap<Integer, Integer> hashMap){
        double beishu = 2;
        double yuzhi = 0.6;
        ArrayList<ArrayList<Double>> allWeight = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> weight1 = new ArrayList<Double>();
        ArrayList<Double> weight2 = new ArrayList<Double>();
        for (int i = 0; i < text1.size(); i++) {
            weight1.add((double)1/text1.size());
        }
        for (int i = 0; i < text2.size(); i++) {
            weight2.add((double)1/text2.size());
        }
        for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()) {
            Integer index1 = entry.getKey();
            Integer index2 = entry.getValue();
            try {
                List<String> subList1 = text1.subList(index1 - 1, index1 + 2);
                List<String> subList2 = text2.subList(index2 - 1, index2 + 2);
                if (CiLin.calcWordsSimilarity(subList1.get(0), subList2.get(0)) > yuzhi ){
                    weight1.set(index1, weight1.get(index1)*beishu);
                    weight2.set(index2, weight2.get(index2)*beishu);
                }
                if (CiLin.calcWordsSimilarity(subList1.get(2), subList2.get(2)) > yuzhi ){
                    weight1.set(index1, weight1.get(index1)*beishu);
                    weight2.set(index2, weight2.get(index2)*beishu);
                }
            } catch (Exception e) {
                continue;
            }
        };
        allWeight.add(weight1);
        allWeight.add(weight2);
        return allWeight;
    }

    /**
     *  获取 公共值向量
     * @param weight
     * @param hashMap
     * @return
     */
    public static ArrayList<ArrayList<Double>> getVector(ArrayList<ArrayList<Double>> weight, HashMap<Integer, Integer> hashMap){
        ArrayList<Double> weight1 = weight.get(0);
        ArrayList<Double> weight2 = weight.get(1);
        ArrayList<ArrayList<Double>> allVector = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> vector1 = new ArrayList<Double>();
        ArrayList<Double> vector2 = new ArrayList<Double>();
        for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()) {
            vector1.add(weight1.get(entry.getKey()));
            vector2.add(weight2.get(entry.getValue()));
        }
        allVector.add(vector1);
        allVector.add(vector2);
        return allVector;
    }

    /**
     *  获取句子长度的相似性
     * @param text1
     * @param text2
     * @return
     */
    public static Double funLenSim(List<String> text1, List<String> text2) {
        double lenSim = 0.0;
        if ( text1.size() < text2.size()){
            lenSim = (double) 2 * text1.size() / (text1.size() + text2.size());
        }else {
            lenSim = (double) 2 * text2.size() / (text1.size() + text2.size());
        }
        return lenSim;
    }

    /**
     *  获取关键词的相似性
     * @param weight
     * @param vector
     * @return
     */
    public static double funKeySim(ArrayList<ArrayList<Double>> weight, ArrayList<ArrayList<Double>> vector) {
        ArrayList<Double> weight1 = weight.get(0);
        ArrayList<Double> weight2 = weight.get(1);
        ArrayList<Double> vector1 = vector.get(0);
        ArrayList<Double> vector2 = vector.get(1);
        double sum1 = 0.0;
        double sum2 = 0.0;
        double sum3 = 0.0;
        double sum4 = 0.0;
        double keySim = 0.0;
        for (Double v1 : vector1) {
            sum1 += v1;
        }
        for (Double v2 : vector2) {
            sum2 +=v2;
        }
        for (Double w1 : weight1) {
            sum3 += w1;
        }
        for (Double w2 : weight2) {
            sum4 +=w2;
        }
        keySim = (sum1 + sum2) / (sum3 + sum4);
        return keySim;
    }

    public static Double funWordOrder(String content1, String content2){
        String[] array1= SplitWord.getSplit(content1).split(" ");
        ArrayList<String> text1 = new ArrayList<String>(Arrays.asList(array1));
        String[] array2= SplitWord.getSplit(content2).split(" ");
        ArrayList<String> text2 = new ArrayList<String>(Arrays.asList(array2));
        double wordSim = 0.0;
        int numOrder = 0;
        HashSet<String> comWord = new LinkedHashSet<String>();
        comWord.addAll(text1);
        comWord.retainAll(text2);
        HashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (String s : comWord) {
            if (text1.contains(s)){
                map.put(s, text1.indexOf(s));
            }
        }
        for (String s : text2) {
            if (map.containsKey(s)){
                list.add(map.get(s));
            }
        }
        for (int i = 0; i < list.size(); i++) {
            if (i>0){
                if (list.get(i) < list.get(i-1)){
                    numOrder +=1;
                }
            }
        }

        int p = comWord.size();
        if (p > 1 ){
            wordSim = (double) (1 - (double)numOrder/(p -1));
        }
        if ( p == 1){
            wordSim = 0.5;
        }
        if ( p == 0 ){
            wordSim = 0.0;
        }
        return wordSim;
    }

    public Double markovSimilary(String content1, String content2){
        int numKey = 10;
        List<String> text1 = HanLP.extractKeyword(content1, numKey);
        List<String> text2 = HanLP.extractKeyword(content2, numKey);
        HashMap<Integer, Integer> hashMap = getCommonMap(text2, text1);
        ArrayList<ArrayList<Double>> weight = getWeight(text1, text2, hashMap);
        ArrayList<ArrayList<Double>> vector = getVector(weight, hashMap);
        Double lenSim = funLenSim(text1, text2);
        Double wordOrderSim = funWordOrder(content1, content2);
        double keySim = funKeySim(weight, vector);
        double finalSim = lenSim * keySim;  // 不考虑词序相似度的计算方法
//        double finalSim = 0.1 * lenSim + 0.5 * wordOrderSim + 0.4 * keySim;
        return finalSim;
    }

    public static void main(String[] args) {

        String content1 = "选择比赛场地附近的酒店";
        String content2 = "女儿因常旷课 遭学校拒绝报名";
        //各个文本向量
        int numKey = 10;
        List<String> text1 = HanLP.extractKeyword(content1, numKey);
        List<String> text2 = HanLP.extractKeyword(content2, numKey);
        //公共词向量
        HashMap<Integer, Integer> hashMap = getCommonMap(text2, text1);
        // 权重值向量
        ArrayList<ArrayList<Double>> weight = getWeight(text1, text2, hashMap);
        // 公共值向量
        ArrayList<ArrayList<Double>> vector = getVector(weight, hashMap);
        //语句长度的相似性计算
        Double lenSim = funLenSim(text1, text2);
        System.out.println("语句长度的相似度：" + lenSim);
        //计算词序相似度：
        Double wordOrderSim = funWordOrder(content1, content2);
        System.out.println("词序相似度：" + wordOrderSim);
        //计算关键词
        double keySim = funKeySim(weight, vector);
        System.out.println("关键词的相似度：" + keySim);
        // 计算总的相似度：
//        double finalSim = lenSim * keySim;  // 不考虑词序相似度的计算方法
        double finalSim = 0.1 * lenSim + 0.5 * wordOrderSim + 0.4 * keySim;
        System.out.println(finalSim);
    }
}
