import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import org.junit.Test;

import java.util.List;

/**
 * Created by sssd on 2017/9/7.
 */
public class HanLp {

    @Test
    public void extractKeyword() {
        String content = "（程序员(英文Programmer)是从事程序开发、维护的专业人员。一般将程序员分为程序设计人员和程序编码人员，" +
                "但两者的界限并不非常清楚，特别是在中国。软件从业人员分为初级程序员、高级程序员、系统分析员和项目经理四大类。";
        List<String> keywordList = HanLP.extractKeyword(content, 10);
        System.out.println(keywordList);
    }

    @Test
    public void wordDistance(){
        String[] wordArray = new String[]{"漂亮", "美丽"};
        for (String a : wordArray) {
            for (String b : wordArray) {
                System.out.println(a + "\t" + b + "\t之间的距离是\t" + CoreSynonymDictionary.distance(a, b));
            }
        }
    }
}
