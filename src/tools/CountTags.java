package tools;

import mwutils.Counter;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class CountTags 
{
	private Counter<String> tags = new Counter<>();
	public static void main(String[] args)
	{
		new CountTags().go();
	}
	private void go()
	{
		Multitasker multitasker = new Multitasker(5);
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new Count("C:/MissingWord/train/tagsPart"+a+".txt"));
		}
		multitasker.done();
		tags.export("C:/MissingWord/tagCounter.txt");
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
				System.out.println(numLine+" "+tags.keySet().size());
			}
			for(String token : line.split(" "))
			{
				tags.add(token);
			}
		}
	}
}
