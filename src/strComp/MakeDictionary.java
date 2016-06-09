package strComp;

import java.util.HashSet;

import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;
import mwutils.Counter;

public class MakeDictionary 
{
	private int THRESHOLD = 100000;
	private Counter<String> dict = new Counter<>();
	public static void main(String[] args)
	{
		new MakeDictionary().checkDup();
	}
	private void checkDup()
	{
		Counter<String> counter = Counter.load("C:/MissingWord/partialLex/partialLex.txt");
		Dictionary dict = Dictionary.fromCounterFile("C:/MissingWord/partialLex/partialLex.txt");
		System.out.println(counter.keySet().size()+" "+dict.size());
	}
	private void trim()
	{
		Counter<String> dict = Counter.load("C:/MissingWord/partialLex.txt");
		threshold(dict);
		dict.export("C:/MissingWord/partialLex.txt");
	}
	private void threshold(Counter<String> dict)
	{
		for(String key : new HashSet<>(dict.keySet()))
		{
			if(key.contains("_") && (key.contains("NN") || key.contains("CD") || key.contains("JJ") || dict.getCount(key) < 10000))
			{
				System.out.println("Removed "+key);
				dict.remove(key);
			}
		}
	}
	private void go()
	{
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			new ReadTagged("C:/MissingWord/train/taggedPart"+a+".txt").run();
		}
		multitasker.done();
		dict.export("C:/MissingWord/partialLex.txt");
	}
	class ReadTagged implements LineOperation, Runnable
	{
		private String filename;
		public ReadTagged(String filename)
		{
			this.filename = filename;
		}
		public void run()
		{
			Read.byLine(filename, this);
		}
		Counter<String> freqWords = Counter.load("C:/MissingWord/frequentWords.txt");
		private int numLine = 0;
		@Override
		public void read(String line) 
		{
			numLine += 1;
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+dict.keySet().size());
			}
			try
			{
				String[] bigTokens = line.split(" ");
				for(int b = 0; b < bigTokens.length; b++)
				{
					String token = bigTokens[b].split("_")[0];
					String tag = bigTokens[b].split("_")[1];
					if(freqWords.getCount(token) > THRESHOLD)
					{
						dict.add(token+"@"+tag);
					}
					else
					{
						dict.add(tag);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
}
