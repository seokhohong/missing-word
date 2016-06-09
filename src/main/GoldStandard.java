package main;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

import utils.Multitasker;
import utils.Read;
import utils.Write;
import utils.Read.LineOperation;

public class GoldStandard 
{
	private List<Gold> golds = new ArrayList<>();			public List<Gold> getGolds() { return golds; }
	public static void main(String[] args)
	{
		//GoldStandard.makeFrame();
		for(int a = 1; a < 50; a++)
		{
			if(new File("C:/MissingWord/gold/"+a+".txt").exists())
			{
				GoldStandard.makeSentence(a);
			}
		}
	}
	private static void makeSentence(int length)
	{
		List<String> goldStrings = new ArrayList<>();
		
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new MakeNSentence("D:/MissingWord/train/taggedPart"+a+".txt", length, goldStrings));
		}
		multitasker.done();
		
		//new MakeNSentence("C:/MissingWord/train/taggedPart2.txt", length, goldStrings).run();
		//Write.to("C:/MissingWord/"+length+"Gold.txt", goldStrings);
		Write.to("C:/MissingWord/gold/"+length+".txt", goldStrings);
	}
	static class MakeNSentence implements LineOperation, Runnable
	{
		int length;
		private String filename;
		private List<String> goldStrings;
		public MakeNSentence(String filename, int length, List<String> goldStrings)
		{
			this.length = length;
			this.filename = filename;
			this.goldStrings = goldStrings;
		}
		private int numLine = 0;
		@Override
		public void read(String line)
		{
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine);
			}
			numLine ++;
			if(line.length() > 0 && line.split(" ").length >= Math.max(length, 3))
			{
				line = line.replaceAll("  ", " ");
				line = line.replaceAll("- -", "--");
				Gold gold = Gold.makeFrom(line);
				if(gold.length() == length || length == -1)
				{
					synchronized(goldStrings)
					{
						goldStrings.add(gold.toString());
					}
				}
			}
		}
		@Override
		public void run()
		{
			Read.byLine(filename, this);
		}
	}
	public GoldStandard(int sentenceLength)
	{
		List<String> goldStrings = Read.from("C:/MissingWord/gold/"+sentenceLength+".txt");
		for(String gold : goldStrings)
		{
			golds.add(Gold.parse(gold));
		}
	}
	public GoldStandard(int sentenceLength, int num)
	{
		List<String> goldStrings = Read.from("C:/MissingWord/gold/"+sentenceLength+".txt");
		for(String gold : goldStrings)
		{
			golds.add(Gold.parse(gold));
			if(golds.size() >= num)
			{
				break;
			}
		}
	}
	
	public List<Gold> getGolds(int length)
	{
		if(length == -1)
		{
			return getGolds();
		}
		List<Gold> lengthGolds = new ArrayList<>();
		for(Gold gold : golds)
		{
			if(gold.length() == length)
			{
				lengthGolds.add(gold);
			}
		}
		return lengthGolds;
	}

}
