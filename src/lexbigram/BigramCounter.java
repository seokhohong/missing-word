package lexbigram;

import java.util.HashSet;
import java.util.Set;

import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.TaggedSentence;
import mwutils.Utils;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.Read;
import utils.Read.LineOperation;

public class BigramCounter 
{
	private CompressedCounter bigrams;
	public static void main(String[] args)
	{
		new BigramCounter().go();
	}
	private void go()
	{
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		MultiCompress mc = new MultiCompress(wordsDict, wordsDict);
		bigrams = CompressedCounterFactory.getInstance(mc);
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("C:/MissingWord/train/taggedPart"+a+".txt", new BigramCount(true));
		}
		bigrams.export("C:/MissingWord/freqBigramsMod.txt");
	}
	class BigramCount implements LineOperation
	{
		Set<String> freqWords = new HashSet<String>(Read.from("C:/MissingWord/frequentWords.txt"));
		int numLine = 0;
		private boolean mod;
		BigramCount(boolean mod)
		{
			this.mod = mod;
		}
		@Override
		public void read(String line) 
		{
			numLine ++ ;
			String[] tokens = TaggedSentence.fromTaggedLine(line).getTokens();
			int adj = mod ? 1 : 0;
			for(int a = 0; a < tokens.length; a++)
			{
				bigrams.add(Utils.getElem(tokens, a - 1), Utils.getElem(tokens, a + adj));
			}
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+bigrams.keySize());
			}
		}
		
	}
}
