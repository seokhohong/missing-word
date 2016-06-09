package correct;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import strComp.Dictionary;
import strComp.MultiCompress;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.TaggedSentence;
import mwutils.Utils;

public class BidirTagsReplacer implements AdvancedReplacer
{
	private int windowSize;
	private int left;
	private int right;
	private int offset;
	
	
	Dictionary tagDict;
	Dictionary wordDict;
	CompressedCounter orig;
	CompressedCounter mod;
	

	public BidirTagsReplacer(int left, int right, int offset)
	{
		this.left = left;
		this.right = right;
		this.offset = offset;
	}
	public void load()
	{
		tagDict = Dictionary.fromCounterFile("C:/MissingWord/tag/tags.txt");
		wordDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		Dictionary[] smallDicts = new Dictionary[left + right + 1];
		Dictionary[] largeDicts = new Dictionary[left + right + 2];
		Arrays.fill(smallDicts, tagDict);
		smallDicts[left] = wordDict;
		Arrays.fill(largeDicts, tagDict);
		largeDicts[left] = wordDict;
		MultiCompress smallMc = new MultiCompress(smallDicts);
		MultiCompress largeMc = new MultiCompress(largeDicts);
		
		orig = CompressedCounterFactory.load("D:/MissingWord/sideTags/"+left+"l"+right+"r"+offset+"offsetMod5.txt", smallMc);
		mod = CompressedCounterFactory.load("D:/MissingWord/sideTags/"+left+"l"+(right + 1)+"r"+offset+"offset5.txt", largeMc);
	}
	public void unload()
	{
		tagDict = null;
		wordDict = null;
		orig = null;
		mod = null;
	}
	public void reweight(FixOptions fixOptions, double weight)
	{
		for(String word : fixOptions.getFixWords())
		{
			double score = score(fixOptions, word, orig, mod);
			fixOptions.multLogScore(word, score * weight);
		}
	}
	private double score(FixOptions fixOptions, String fixWord, CompressedCounter orig, CompressedCounter mod)
	{
		int tTags = left + right + 1;
		String[] unfixedSequence = new String[tTags];
		for(int b = 0; b < tTags; b ++)
		{
			int elemIndex = fixOptions.getFixIndex() - offset + b;
			if(b != left)
			{
				unfixedSequence[b] = fixOptions.getTag(fixWord, elemIndex);
			}
			else
			{
				unfixedSequence[b] = fixOptions.getToken(fixWord, elemIndex);
			}
		}
		
		String[] fixedSequence = new String[tTags + 1];
		for(int b = 0; b < tTags + 1; b ++)
		{
			int elemIndex = fixOptions.getFixIndex() - offset + b;
			if(b != left)
			{
				fixedSequence[b] = fixOptions.getTag(fixWord, elemIndex);
			}
			else
			{
				fixedSequence[b] = fixOptions.getToken(fixWord, elemIndex);
			}
		}
		
		return Math.log((double) (mod.getCount(fixedSequence) + 1) / (orig.getCount(unfixedSequence) + 1));
	}
}
