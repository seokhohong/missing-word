package lexbigram;

import java.util.HashSet;
import java.util.Set;

import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Utils;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class TrigramCounter 
{
	private CompressedCounter trigrams;
	private int count = 2000;
	public static void main(String[] args)
	{
		new TrigramCounter(2, true).go();
		new TrigramCounter(2, false).go();
	}
	private int offset;
	private boolean mod;
	public TrigramCounter(int offset, boolean mod)
	{
		this.offset = offset;
		this.mod = mod;
	}
	private void go()
	{
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/"+count+"FrequentWords.txt");
		MultiCompress mc = new MultiCompress(wordsDict, 3);
		trigrams = CompressedCounterFactory.getInstance(mc);
		Multitasker multitasker = new Multitasker(5);
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new TrigramCount("D:/MissingWord/train/tokensPart"+a+".txt"));
		}
		multitasker.done();
		String modStr = mod ? "Mod" : "";
		trigrams.export("C:/MissingWord/ngrams/"+offset+"-"+count+"freqTrigrams"+modStr+".txt");
	}
	class TrigramCount implements LineOperation, Runnable
	{
		Set<String> freqWords = new HashSet<String>(Read.from("C:/MissingWord/"+count+"FrequentWords.txt"));
		int numLine = 0;
		private String filename;
		TrigramCount(String filename)
		{
			this.filename = filename;
		}
		@Override
		public void read(String line) 
		{
			numLine ++ ;
			String[] tokens = line.split(" ");
			for(int a = - offset; a < tokens.length + offset; a++)
			{
				String[] trigram = new String[3];
				for(int b = 0; b < trigram.length; b++)
				{
					int adj = mod & b >= offset ? 1 : 0;
					trigram[b] = Utils.getElem(tokens, a + b + adj); 
				}
				trigrams.add(trigram);
			}
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+trigrams.keySize());
			}
		}
		public void run()
		{
			Read.byLine(filename, this);
		}
	}
}
