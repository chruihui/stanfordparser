import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by sssd on 2017/9/8.
 */
public class testStanford {

    public  static HashMap<String, HashSet<String>> tree(HashMap<String, HashSet<String>> map ,Tree parent,Tree tree){

        if(tree.isLeaf()){
            buildData(map, parent);
            return map;
        }
        if(tree.isPhrasal()){
            final Tree[] children = tree.children();
            for (Tree child : children) {
                tree(map,tree,child);
            }
        }else {
            buildData(map, parent);
        }
        return map;
    }

    public static void buildData(HashMap<String, HashSet<String>> map, Tree parent) {
        final String value = parent.getLeaves().toString();
        final String key = parent.value();
        HashSet<String> strings;
        if(map.get(key) == null){
            strings = new HashSet<String>();
        }else {
            strings = map.get(key);
        }
        strings.add(value);
        map.put(key,strings);
    }



    public static void main(String[] args) {

        String modelpath = "edu/stanford/nlp/models/lexparser/xinhuaFactoredSegmenting.ser.gz";
        LexicalizedParser lexicalizedParser = LexicalizedParser.loadModel(modelpath);
        String text = "对于今天下午的考试，我非常有信心。";
        Tree tree = lexicalizedParser.parse(text);
        HashMap<String, HashSet<String>> hashMap = new HashMap<String, HashSet<String>>();
        HashMap<String, HashSet<String>> tree1 = testStanford.tree(hashMap, tree, tree);
        for (Map.Entry<String, HashSet<String>> entry : tree1.entrySet()) {
            System.out.println("----------------------------------------------");
            System.out.println(entry.getKey()+"#"+entry.getValue().size());
            for (String s : entry.getValue()) {
                System.out.println(s);
            }
        }

    }

}
