package paper2;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import org.apache.commons.collections.map.StaticBucketMap;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by sssd on 2017/9/9.
 */
public class SplitWord {

    public static String getSplit(String str){
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<SegToken> tokens = segmenter.process(str, JiebaSegmenter.SegMode.INDEX);
        StringBuffer tokenizerResult = new StringBuffer();
        for (SegToken token : tokens) {
            String word = token.word;
            if(StringUtils.isBlank(word) || word.length() < 1) {
                continue;
            }
            tokenizerResult.append(word).append(" ");
        }
        tokenizerResult.deleteCharAt(tokenizerResult.length()-1);
        return tokenizerResult.toString();
    }

}
