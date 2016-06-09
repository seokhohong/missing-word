package strComp;

public class TestCompressor 
{
	public static void main(String[] args)
	{
		Dictionary tagDict = Dictionary.fromCounterFile("C:/MissingWord/partialLex.txt");
		MultiCompress mc = new MultiCompress(tagDict, tagDict, tagDict, tagDict);
		MultiCompress mc1 = new MultiCompress(tagDict);
		System.out.println((long) mc.encode("you_PRP", "MD", "one_CD", "of_IN"));
		System.out.println((long) mc.encode("you_PRP", "WRB", "one_CD", "of_IN"));
		System.out.println((long) mc1.encode("MD"));
		System.out.println((long) mc1.encode("WRB"));
	}
}
