
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.international.pennchinese.ChineseGrammaticalStructure;
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
public class StanfordParser {

    protected  LexicalizedParser lexicalizedParser;
    protected Set STOP_WORD_SET = null;

    public StanfordParser(String modelPath, String stopPath){
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

    public HashMap<String, HashSet<String>> tree2Map(HashMap<String, HashSet<String>> hashMap, Tree parent, Tree tree){
        if(tree.isLeaf()){
            reMap(hashMap, parent);
            return hashMap;
        }
        if (tree.isPhrasal()){
            Tree[] children = tree.children();
            for (Tree child : children) {
                tree2Map(hashMap, tree, child);
            }
        }else{
            reMap(hashMap, parent);
        }
        return hashMap;
    }

    public void reMap(HashMap<String, HashSet<String>> hashMap, Tree parent) {
        final String value = parent.getLeaves().toString();
        final String key = parent.value();
        HashSet<String> str;
        if (hashMap.get(key) == null){
            str = new HashSet<String>();
        }else{
            str = hashMap.get(key);
        }
        str.add(value);
        hashMap.put(key,str);
    }

    public HashMap<String, HashSet<String>> getChild(String text){
        Tree tree = lexicalizedParser.parse(text);
        HashMap<String, HashSet<String>> hashMap = new HashMap<String, HashSet<String>>();
        HashMap<String, HashSet<String>> resultMap = tree2Map(hashMap, tree, tree);
        return resultMap;
    }

    public Double structSim(HashMap<String, HashSet<String>> hashMap1, HashMap<String, HashSet<String>> hashMap2){
        int num1 = hashMap1.size();
        int num2 = hashMap2.size();
        int commen = 0;
        double result = 0.0;
        for(Map.Entry<String, HashSet<String>> entry : hashMap1.entrySet()){
            String key = entry.getKey();
            if (hashMap2.containsKey(key)){
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

    public Double structWordSim(HashMap<String, HashSet<String>> hashMap1, HashMap<String, HashSet<String>> hashMap2){
        double pf = 0.1;    //句法结构差异的调节因子
        double ratio = 1.0;  // 结构类型所对应的权重值
        Set<String> set1 = hashMap1.keySet();
        Set<String> set2 = hashMap2.keySet();
        HashSet<String> tempSet = new HashSet<String>(set1);//表示有相同的语句结构
        boolean b = tempSet.retainAll(set2);
        int commen = tempSet.size();
//        int noSameCount = set1.size() + set2.size() - 2*commen;

        Iterator<String> iterator = tempSet.iterator();     //遍历相同语法结构
        double sim_S = 0.0;
        while (iterator.hasNext()){
            String key = iterator.next();
            HashSet<String> value1 = hashMap1.get(key);
            HashSet<String> value2 = hashMap2.get(key);
            String[] split1 = value1.toArray(new String[value1.size()]);
            String[] split2 = value2.toArray(new String[value1.size()]);
            double sum_split1 = splitWordSim(split1, split2);
            double sum_split2 = splitWordSim(split2, split1);
            double fenSum_1 = sum_split1/split1.length;
            double fenSum_2 = sum_split2/split2.length;
            double meanSum = (fenSum_1 + fenSum_2) / 2;
            sim_S += ratio * meanSum;
        }
        double result = sim_S/tempSet.size();
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

    public Double finalSim(Double structRes, Double wordRes, Double structWordRes){
        double a = 0.1;
        double b = 0.9;
        double result = 0.0;
        result = a * structRes + (1-a)*(b * structWordRes + (1-b) * wordRes);
        return result;
    }

    public Double textSimilarity(String line1, String line2){
        HashMap<String, HashSet<String>> hashMap1 = getChild(line1);
        HashMap<String, HashSet<String>> hashMap2 = getChild(line2);
        Double structRes = structSim(hashMap1, hashMap2);
        ArrayList splitWord = getSplitWord(line1);
        ArrayList splitWord1 = getSplitWord(line2);
        Double wordRes = wordSim(splitWord, splitWord1);
        Double structWordRes = structWordSim(hashMap1, hashMap2);
        Double finalSim = finalSim(structRes, wordRes, structWordRes);
        return finalSim;
    }

    public static void main(String[] args) {

        String modelpath = "edu/stanford/nlp/models/lexparser/xinhuaFactoredSegmenting.ser.gz";
        String stopPath = "src/main/resources/stopWords.ml";
        StanfordParser stanfordParser = new StanfordParser(modelpath, stopPath);

//        String text = "对一名女警做出猥亵动作的男子";
        String text = "她长成了一位漂亮的女人.";
        String text2 = "她长成了一位漂亮的女人.";

        Double result = stanfordParser.textSimilarity(text, text2);
        System.out.println(result);

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

