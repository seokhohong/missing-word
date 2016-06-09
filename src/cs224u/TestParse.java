package cs224u;

import java.util.List;

import utils.Read;

public class TestParse 
{
	public static void main(String[] args)
	{
		new TestParse().go();
	}
	private void go()
	{
		List<String> sentences = Read.from("sicktrain.txt");
		List<String> relations = Read.from("relationsTrain.txt");
		for(int a = 0; a < 10; a++)
		{
			System.out.println(sentences.get(a)+" "+relations.get(2 * a) + " "+ relations.get(2 * a + 1));
		}
	}
}
