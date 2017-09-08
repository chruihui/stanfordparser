package wordsimilarity;

import java.util.List;
import junit.framework.TestCase;

/**
 * Created by sssd on 2017/9/6.
 */
public class PrimitiveTests extends TestCase {

    public void test_getParents(){
        String primitive = "攻打";
        List<Integer> list = Primitive.getParents(primitive);
        for(Integer i : list){
            System.out.println(i);
        }
    }
}
