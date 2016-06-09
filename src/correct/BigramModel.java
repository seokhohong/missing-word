package correct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dep.ProcessDeps;
import main.Gold;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.Utils;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.ArrayUtils;


public class BigramModel implements AdvancedReplacer
{
	private Dictionary wordsDict;
	private Counter<String> unigrams;
	private CompressedCounter bigrams;
	private int offset;
	private boolean lowercase = true;

	public BigramModel(int offset, boolean lowercase)
	{
		this.offset = offset;
		this.lowercase = lowercase;
	}
	public void reweight(FixOptions fixOptions, double weight) 
	{
		for(String fixWord : fixOptions.getFixWords())
		{
			Gold gold = fixOptions.getGold();
			int fixIndex = fixOptions.getGold().removeIndex();
			String[] tokens = Arrays.copyOf(gold.getTokens(), gold.getTokens().length);
			tokens = ArrayUtils.splice(tokens, gold.removeIndex(), Utils.getToken(fixWord));
			String leftElem = caseProperly(Utils.getElem(tokens, fixIndex + offset), unigrams);
			String centerElem = caseProperly(Utils.getElem(tokens, fixIndex + offset + 1), unigrams);
			if(wordsDict.contains(leftElem) && wordsDict.contains(centerElem))
			{
				double oneScore = score(leftElem, centerElem, bigrams, unigrams);
				//double twoScore = score(entry.getTaggedSentence().getToken(fixIndex), entry.getTaggedSentence().getToken(fixIndex + 1), bigrams, unigrams);
				fixOptions.multLogScore(fixWord, oneScore * weight);
				//entry.multLogScore(twoScore);
			}
		}
	}
	private String caseProperly(String word, Counter<String> unigrams)
	{
		if(unigrams.getCount(word) > unigrams.getCount(word.toLowerCase()))
		{
			return word;
		}
		return word.toLowerCase();
	}
	private double score(String one, String two, CompressedCounter bigrams, Counter<String> unigrams)
	{
		return Math.log((double) (bigrams.getCount(one, two) + 1));
	}
	@Override
	public void load() 
	{
		wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		unigrams = Counter.load("C:/MissingWord/frequentWords.txt");
		MultiCompress mc = new MultiCompress(wordsDict, 2);
		bigrams = CompressedCounterFactory.load("D:/MissingWord/ngrams/freqBigrams.txt", mc);
	}
	
	@Override
	public void unload() 
	{
		wordsDict = null;
		unigrams = null;
		bigrams = null;
	}
}
