package lexbigram;

import java.util.ArrayList;
import java.util.List;

import utils.Write;
import mwutils.Counter;

public class Cooccurrence 
{
	public static void main(String[] args)
	{
		new Cooccurrence().go();
	}
	private void go()
	{
		Counter<String> unigrams = Counter.load("C:/MissingWord/frequentWords.txt");
		List<String> freqUnigrams = new ArrayList<>();
		for(String word : unigrams.keySet())
		{
			if(unigrams.getCount(word) > 10000)
			{
				freqUnigrams.add(word);
			}
		}
		Write.to("C:/MissingWord/10000Words.txt", freqUnigrams);
		System.out.println(freqUnigrams.size());
	}
}
