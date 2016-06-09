package tools;

import mwutils.Counter;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class CountWords 
{
	private Counter<String> words = new Counter<>();
	public static void main(String[] args)
	{
		new CountWords().go();
	}
	private void go()
	{
		int minCount = 50;
		Multitasker multitasker = new Multitasker(5);
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new Count("C:/MissingWord/train/taggedPart"+a+".txt"));
		}
		multitasker.done();
		Counter<String> trimmedCounter = new Counter<>();
		for(String word : words.keySet())
		{
			if(words.getCount(word) >= minCount)
			{
				trimmedCounter.put(word, words.getCount(word));
			}
		}
		trimmedCounter.export("C:/MissingWord/dep/"+minCount+"FrequentChunks.txt");
	}
	class Count implements LineOperation, Runnable
	{
		private String filename;
		private int numLine = 0;
		public Count(String filename)
		{
			this.filename = filename;
		}
		public void run()
		{
			Read.byLine(filename, this);
		}
		@Override
		public void read(String line) 
		{
			numLine ++ ;
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+words.keySet().size());
			}
			for(String token : line.split(" "))
			{
				words.add(token);
			}
		}
	}
}
