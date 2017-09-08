package wordsimilarity;

import junit.framework.TestCase;

/**
 * Created by sssd on 2017/9/6.
 */
public class WordSimilarityTests extends TestCase {
    public void test_loadGlossary(){
        WordSimilarity.loadGlossary();
    }
    /**
     * test the method {@link WordSimilarity#disPrimitive(String, String)}.
     */
    public void test_disPrimitive(){
        int dis = WordSimilarity.disPrimitive("优秀", "优秀");
        System.out.println("优秀 and 优秀 dis : "+ dis);
    }

    public void test_simPrimitive(){
        double simP = WordSimilarity.simPrimitive("雇佣", "争斗");
        System.out.println("雇佣 and 争斗 sim : "+ simP);
    }
    public void test_simWord(){
        String word1 = "明媚";
        String word2 = "好";
        double sim = WordSimilarity.simWord(word2, word1);
        System.out.println("%%%"+sim);
    }

}
