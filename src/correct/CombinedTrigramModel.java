package correct;

import java.util.Arrays;
import java.util.List;

import main.Gold;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.Utils;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.ArrayUtils;

public class CombinedTrigramModel implements AdvancedReplacer
{
	private Dictionary wordsDict;
	private Counter<String> unigrams;
	private CompressedCounter trigrams;
	private boolean loaded = false;
	private int[][] offsets = {
			{-2, -1,  0},
			{-1,  0,  1},
			{0,   1,  2},
	};
	
	
	@Override
	public void reweight(FixOptions fixOptions, double weight) 
	{
		if(!loaded)
		{
			System.out.println("Not Loaded");
			System.exit(0);
		}
		for(int[] offset : offsets)
		{
			//if(canEvaluate(fixEntries, wordsDict, offset))
			{
				for(String fixWord : fixOptions.getFixWords())
				{
					int fixIndex = fixOptions.getFixIndex();
					//if(canEvaluate(fixEntries, wordsDict, offset))
					{
						Gold gold = fixOptions.getGold();
						String[] tokens = Arrays.copyOf(gold.getTokens(), gold.getTokens().length);
						tokens = ArrayUtils.splice(tokens, gold.removeIndex(), Utils.getToken(fixWord));
						String elem0 = Utils.getElem(tokens, fixIndex + offset[0]);
						String elem1 = Utils.getElem(tokens, fixIndex + offset[1]);
						String elem2 = Utils.getElem(tokens, fixIndex + offset[2]);
						if(wordsDict.contains(elem0) && wordsDict.contains(elem1) && wordsDict.contains(elem2))
						{
							double score = score(elem0, elem1, elem2, trigrams, unigrams);
							fixOptions.multLogScore(fixWord, score * weight);
						}
					}
				}
			}
		}
	}
	private double score(String one, String two, String three, CompressedCounter trigrams, Counter<String> unigrams)
	{
		//return Math.log((double) (trigrams.getCount(one, two, three) + 1) / (unigrams.getCount(two) + 1));
		return Math.sqrt(Math.log((double) (trigrams.getCount(one, two, three) + 1)));
	}
	private boolean canEvaluate(List<FixEntry> fixEntries, Dictionary wordsDict, int[] offset)
	{
		FixEntry entry = fixEntries.get(0);
		int fixIndex = entry.getFixIndex();
		boolean canEval = true;
		for(int a = 0; a < offset.length; a++)
		{
			if(offset[a] != 0)
			{
				canEval &= wordsDict.contains(entry.getTaggedSentence().getToken(fixIndex + offset[a]));
			}
		}
		return canEval;
	}
	@Override
	public void load() 
	{
		wordsDict = Dictionary.fromCounterFile("C:/MissingWord/2000FrequentWords.txt");
		unigrams = Counter.load("C:/MissingWord/frequentWords.txt");
		MultiCompress mc = new MultiCompress(wordsDict, 3);
		trigrams = CompressedCounterFactory.load("C:/MissingWord/ngrams/2000freqTrigrams5.txt", mc);
		loaded = true;
	}
	@Override
	public void unload() 
	{
		wordsDict = null;
		unigrams = null;
		trigrams = null;
		loaded = false;
	}
}
