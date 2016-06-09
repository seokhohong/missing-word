package correct;

import gnu.trove.set.TLongSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stanford.StanfordTagger;
import strComp.Dictionary;
import utils.ArrayUtils;
import utils.ListUtils;
import main.Gold;
import mwutils.Counter;
import mwutils.TaggedSentence;
import utils.Multitasker;

public class FixEntry implements Comparable<FixEntry>
{
	private TaggedSentence taggedSentence;
	private int fixIndex = 0;
	//kept in log
	private double score = 0;											public double getScore() { return score; }
																		public void setScore(double score) { this.score = score; }
																		
	public FixEntry(TaggedSentence taggedSentence, int fixIndex)
	{
		this.taggedSentence = taggedSentence;
		this.fixIndex = fixIndex;
	}
	public int getFixIndex()
	{
		return fixIndex;
	}
	public TaggedSentence getTaggedSentence()
	{
		return taggedSentence;
	}
	public String getAdded()
	{
		return taggedSentence.getToken(fixIndex)+"_"+taggedSentence.getTag(fixIndex);
	}
	public String getAddedToken()
	{
		return taggedSentence.getToken(fixIndex);
	}
	public String getAddedTag()
	{
		return taggedSentence.getTag(fixIndex);
	}
	@Override
	public int compareTo(FixEntry o) 
	{
		return Double.compare(score, o.score);
	}
	public String toString()
	{
		return score + " " + taggedSentence.toString();
	}
	public void multLogScore(double logScore) 
	{
		this.score += logScore;
		if(Double.isNaN(score))
		{
			System.out.println();
		}
	}
	
	public static double medianFrequency(List<FixEntry> fixEntries, Counter<String> unigrams)
	{
		double[] frequencies = new double[fixEntries.size()];
		for(int a = 0; a < fixEntries.size(); a++)
		{
			frequencies[a] = unigrams.getCount(fixEntries.get(a).getAddedToken());
		}
		return ArrayUtils.median(frequencies);
	}
	
	public static List<FixEntry> recalibrate(List<FixEntry> entries)
	{
		double[] scores = new double[entries.size()];
		for(int a = 0; a < scores.length; a++)
		{
			scores[a] = entries.get(a).score;
		}
		double maxScore = ArrayUtils.maxValue(scores);
		for(FixEntry entry : entries)
		{
			entry.score -= maxScore;
		}
		List<FixEntry> newEntries = new ArrayList<>();
		for(FixEntry entry : entries)
		{
			if(entry.score > -7) //e^-x is pretty small
			{
				newEntries.add(entry);
			}
		}
		if(newEntries.size() == 0)
		{
			for(FixEntry entry : entries)
			{
				System.out.println(entry.score+" "+entry);
			}
			System.out.println("EXTINCTION");
			System.exit(0);
		}
		return newEntries;
	}

	
	public static class CompareByWordFrequency implements Comparator<FixEntry>
	{
		private Counter<String> words = Counter.load("C:/MissingWord/frequentWords.txt");
		public CompareByWordFrequency(Counter<String> words)
		{
			this.words = words;
		}
		@Override
		public int compare(FixEntry o1, FixEntry o2) 
		{
			return Integer.compare(words.getCount(o1.getAddedToken()), words.getCount(o2.getAddedToken()));
		}
	}
}
