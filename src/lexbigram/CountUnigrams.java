package lexbigram;

import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.Read;
import utils.Read.LineOperation;

public class CountUnigrams 
{
	public static void main(String[] args)
	{
		new CountUnigrams().go();
	}
	private void go()
	{
		Dictionary wordDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		CompressedCounter counter = CompressedCounterFactory.getInstance(new MultiCompress(wordDict));
		Count count = new Count(counter);
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("D:/MissingWord/train/tokensPart"+a+".txt", count);
		}
		count.counter.export("C:/MissingWord/depUnigram.txt");
	}
	class Count implements LineOperation
	{
		int count = 0;
		private CompressedCounter counter;
		public Count(CompressedCounter counter)
		{
			this.counter = counter;
		}
		@Override
		public void read(String line) 
		{
			String[] words = line.split(" ");
			for(String word : words)
			{
				counter.add(word);
			}
		}
		
	}
}
