package simhash;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import wordsimilarity.WordSimilarity;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sssd on 2017/9/5.
 */
public class StanfordParserSimhash {

    protected  LexicalizedParser lexicalizedParser;
    protected Set STOP_WORD_SET = null;

    public StanfordParserSimhash(String modelPath, String stopPath){
        lexicalizedParser = LexicalizedParser.loadModel(modelPath);
        readStop(stopPath);
    }

    public ArrayList<String> getSplitWord(String textLine) {
        String sentence = textLine;
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<SegToken> tokens = segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX);
        ArrayList<String> list = new ArrayList<String>();
        for (SegToken token : tokens) {
            String word = token.word;
            if(StringUtils.isBlank(word) || word.length() < 1) {
                continue;
            }
            if (! STOP_WORD_SET.contains(word)){
                list.add(word);
            }
        }
        return list;
    }

    public void readStop(String stopPath){
        BufferedReader bufferedReader = null;
        STOP_WORD_SET = new HashSet();
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(stopPath)), "UTF-8"));
            String stopWord = bufferedReader.readLine();
            while (stopWord != null){
                if (!StringUtils.isBlank(stopWord)){
                    STOP_WORD_SET.add(stopWord);
                }
                stopWord = bufferedReader.readLine();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    public HashMap<String, String> getChild(String text){
        Tree tree = lexicalizedParser.parse(text);
        Tree treeChild = tree;
        while ( treeChild.numChildren() < 2 ){
            treeChild = tree.getChild(0);
        }
        HashMap<String, String> hashMap = new HashMap<String, String>();
        for (int i = 0; i<treeChild.numChildren(); i++){
            Tree temp = treeChild.getChild(i);
            if (temp.nodeString().toString().equals("IP")){
                for (int j = 0; j < temp.numChildren(); j++){
                    Tree tempChild = temp.getChild(j);
                    String str = tempChild.nodeString();   // 获取节点树中的从句类型
                    String value = tempChild.toString();
                    String tempString = value.replaceAll("[^0-9\u4e00-\u9fa5.，,。？“”]+","");
                    Pattern pattern = Pattern.compile(" {2,}");
                    Matcher mLine = pattern.matcher(tempString);
                    String substring = mLine.replaceAll(" ").trim();
                    if (!hashMap.containsKey(str)){
                        hashMap.put(str, substring);
                    }else{
                        String tempStr =  hashMap.get(str);
                        tempStr = tempStr + " " + substring;
                        hashMap.put(str, tempStr);
                    }
                }
            }else {
                String str = temp.nodeString();   // 获取节点树中的从句类型
                String value = temp.toString();
                String tempString = value.replaceAll("[^0-9\u4e00-\u9fa5.，,。？“” ]+","");
                Pattern pattern = Pattern.compile(" {2,}");
                Matcher mLine = pattern.matcher(tempString);
                String substring = mLine.replaceAll(" ").trim();
                if (!hashMap.containsKey(str)){
                    hashMap.put(str, substring);
                }else{
                    String tempStr =  hashMap.get(str);
                    tempStr = tempStr + " " + substring;
                    hashMap.put(str, tempStr);
                }
            }
        }
        return hashMap;
    }

    public Double structSim(HashMap<String, String> hashMap1, HashMap<String, String> hashMap2){
        int num1 = hashMap1.size();
        int num2 = hashMap2.size();
        int commen = 0;
        double result = 0.0;
        for(Map.Entry<String, String> entry : hashMap1.entrySet()){
            String value = entry.getValue();
            if (hashMap2.containsValue(value)){
                commen ++;
            }
        }
        result = (double) commen/(num1+num2-commen);
        return result;
    }

    public Double wordSim(ArrayList<String> splitWord1, ArrayList<String> splitWord2){
        Double result = 0.0;
        ArrayList<Integer> list1 = new ArrayList<Integer>();
        ArrayList<Integer> list2 = new ArrayList<Integer>();
        LinkedHashSet<String> hashSet = new LinkedHashSet<String>();
        hashSet.addAll(splitWord1);
        hashSet.addAll(splitWord2);
        Iterator<String> iterator = hashSet.iterator();
        while(iterator.hasNext()){
            String temp = iterator.next();
            if ( splitWord1.contains(temp)){
                list1.add(splitWord1.indexOf(temp));
            }
            if( !splitWord1.contains(temp)){
                list1.add(0);
            }
            if (splitWord2.contains(temp)){
                list2.add(splitWord2.indexOf(temp));
            }
            if( !splitWord2.contains(temp)){
                list2.add(0);
            }
        }
        double sum1 = 0;
        double sum2 = 0;
        for (int i=0; i<hashSet.size(); i++){
            sum1 += Math.pow((list1.get(i)-list2.get(i)), 2);
            sum2 += Math.pow((list1.get(i)+list2.get(i)), 2);
        }
        result = 1.0 - Math.sqrt(sum1/sum2);
        return result;
    }

    public Double splitWordSim(String[] split1, String[] split2){
        double sum_split1 = 0.0;
        for (int i = 0; i<split1.length; i++ ){
            String temp1 = split1[i];
            double max1 = 0.0;
            for (int j = 0; j < split2.length; j++ ){
                double sim1 = WordSimilarity.simWord(temp1, split2[j]);
                if(sim1 > max1){
                    max1 = sim1;
                }
            }
            sum_split1 += max1;
        }
        return sum_split1;
    }

    public Double structWordSim(HashMap<String, String> hashMap1, HashMap<String, String> hashMap2){
        double pf = 0.1;    //句法结构差异的调节因子
        double ratio = 1.0;  // 结构类型所对应的权重值
        Set<String> set1 = hashMap1.keySet();
        Set<String> set2 = hashMap2.keySet();
        Set<String> tempSet = set1;   //表示有相同的语句结构
        tempSet.contains(set2);
        int commen = tempSet.size();
        int noSameCount = set1.size() + set2.size() - commen;

        Iterator<String> iterator = tempSet.iterator();     //遍历相同语法结构
        double sim_S = 0.0;
        while (iterator.hasNext()){
            String key = iterator.next();
            String str1 = hashMap1.get(key);
            String str2 = hashMap2.get(key);
            SimHash hash1 = null;
            SimHash hash2 = null;
            try {
                hash1 = new SimHash(str1, 128);
                hash2 = new SimHash(str2, 128);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int distance = hash1.hammingDistance(hash2);
            Double hashSim = (double)(1 - distance/64.0);
            sim_S += ratio * hashSim;
        }
        double result = sim_S/tempSet.size() - pf * noSameCount;
        return result;
    }

    public Double finalSim(Double structRes, Double wordRes, Double structWordRes){
        double a = 0.9;
        double b = 0.8;
        double result = 0.0;
        result = b * structWordRes + (1-b) * wordRes;
        return result;
    }

    public static void main(String[] args) {

        String modelpath = "edu/stanford/nlp/models/lexparser/xinhuaFactoredSegmenting.ser.gz";
        String stopPath = "src/main/resources/stopWords.ml";
        StanfordParserSimhash stanfordParser = new StanfordParserSimhash(modelpath, stopPath);

        String text = "今天天气非常好";
        String text2 = "今天阳光明媚";

        HashMap<String, String> hashMap1 = stanfordParser.getChild(text);
        HashMap<String, String> hashMap2 = stanfordParser.getChild(text2);

        Double structRes = stanfordParser.structSim(hashMap1, hashMap2);
        System.out.println("structRes: " + structRes);

        ArrayList splitWord = stanfordParser.getSplitWord(text);
        ArrayList splitWord1 = stanfordParser.getSplitWord(text2);

        Double wordRes = stanfordParser.wordSim(splitWord, splitWord1);
        System.out.println("wordRes: " + wordRes);

        Double structWordRes = stanfordParser.structWordSim(hashMap1, hashMap2);
        System.out.println("structWordRes: " + structWordRes);

//        wordRes = 0.2;
        Double finalSim = stanfordParser.finalSim(structRes, wordRes, structWordRes);
        System.out.println("finalSim: " + finalSim);


       /* ChineseGrammaticalStructure gs = new ChineseGrammaticalStructure(tree);
        Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();
        String s = "";
        System.out.println(" tdl 的大小为：" + tdl.size());
        for (int i = 0; i<tdl.size(); i++){
            TypedDependency td = (TypedDependency) tdl.toArray()[i];
            String age = td.dep().toString();
            s += age +"/";
            s += " ";
        }
        System.out.println(s);*/
    }
}

