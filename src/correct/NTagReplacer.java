package correct;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import strComp.Dictionary;
import strComp.MultiCompress;
import main.Gold;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.TaggedSentence;
import mwutils.Utils;

public class NTagReplacer implements AdvancedReplacer
{
	private int windowSize;
	private int offset;
	
	Dictionary tagDict;
	Counter<String> tagCounter;
	CompressedCounter orig;
	CompressedCounter mod;
	
	/** uses windowSize + 1 for mod*/
	public NTagReplacer(int windowSize, int offset)
	{
		this.windowSize = windowSize;
		this.offset = offset;
	}
	public void load()
	{
		tagDict = Dictionary.fromCounterFile("C:/MissingWord/tag/tags.txt");
		MultiCompress smallMc = new MultiCompress(tagDict, windowSize);
		MultiCompress largeMc = new MultiCompress(tagDict, windowSize + 1);
		tagCounter = Counter.load("C:/MissingWord/tag/tags.txt");
		
		orig = CompressedCounterFactory.load("D:/MissingWord/tag/"+windowSize+"tag"+offset+"offsetMod.txt", smallMc);
		mod = CompressedCounterFactory.load("D:/MissingWord/tag/"+(windowSize + 1)+"tag"+(offset + 1)+"offset5.txt", largeMc);
	}
	public void unload()
	{
		tagDict = null;
		tagCounter = null;
		orig = null;
		mod = null;
	}
	public void reweight(FixOptions fixOptions, double weight)
	{
		for(String word : fixOptions.getFixWords())
		{
			double score = score(fixOptions, word, orig, mod, tagCounter);
			fixOptions.multLogScore(word, score * weight);
		}
	}
	private double score(FixOptions fixOptions, String fixWord, CompressedCounter orig, CompressedCounter mod, Counter<String> tagsCounter)
	{
		String[] unfixedSequence = new String[windowSize];
		for(int a = 0; a < windowSize; a++)
		{
			int adj = a >= offset? 1 : 0;
			unfixedSequence[a] = fixOptions.getTag(fixWord, a + fixOptions.getFixIndex() - offset + adj);
		}
		
		String[] fixedSequence = new String[windowSize + 1];
		for(int a = 0; a < windowSize + 1; a++)
		{
			fixedSequence[a] = fixOptions.getTag(fixWord, a + fixOptions.getFixIndex() - offset);
		}
		return Math.log((double) (mod.getCount(fixedSequence) + 1) / (orig.getCount(unfixedSequence) + 1));
	}
}
