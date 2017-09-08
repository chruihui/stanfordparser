package synonym;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.FilesystemResourceLoader;
import org.apache.lucene.util.Version;
import org.apache.uima.annotator.WhitespaceTokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sssd on 2017/9/7.
 */
public class Synonym {
    private static void displayTokens(TokenStream ts) throws IOException
    {
        CharTermAttribute termAttr = ts.addAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAttribute = ts.addAttribute(OffsetAttribute.class);
        ts.reset();
        while (ts.incrementToken())
        {
            String token = termAttr.toString();
            System.out.print(offsetAttribute.startOffset() + "-" + offsetAttribute.endOffset() + "[" + token + "] ");
        }
        System.out.println();
        ts.end();
        ts.close();
    }

    public static void main(String[] args) throws Exception
    {
        String testInput = "今天天气非常,好今天阳光明媚";
        Version ver = Version.LUCENE_46;
        Map<String, String> filterArgs = new HashMap<String, String>();
        filterArgs.put("luceneMatchVersion", ver.toString());
        filterArgs.put("synonyms", "src/main/resources/data/synonyms.txt");
        filterArgs.put("expand", "true");
        SynonymFilterFactory factory = new SynonymFilterFactory(filterArgs);
        factory.inform(new FilesystemResourceLoader());
        WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(ver);
        TokenStream ts = factory.create(whitespaceAnalyzer.tokenStream("someField", testInput));
        displayTokens(ts);
    }
}
