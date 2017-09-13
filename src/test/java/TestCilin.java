import paper2.cilin.CiLin;

public class TestCilin {
	public static void main(String args[]) {
		String word1 = "标致", word2 = "漂亮";
		double sim = 0;
		sim = CiLin.calcWordsSimilarity(word1, word2);//计算两个词的相似度
		System.out.println(word1 + "  " + word2 + "的相似度为：" + sim);
	}
}
