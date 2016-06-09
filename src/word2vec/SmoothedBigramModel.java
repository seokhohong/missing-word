package word2vec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import stanford.StanfordTagger;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.ArrayUtils;
import utils.ListUtils;
import correct.FixEntry;
import correct.Replacer;

public class SmoothedBigramModel implements Replacer
{

	@Override
	public void reweight(List<FixEntry> fixEntries) 
	{
		//if words are all very frequent, don't reweight
		Counter<String> unigrams = Counter.load("C:/MissingWord/frequentWords.txt");

		SimilarityMatrix matrix = SimilarityMatrix.load("C:/MissingWord/mostRelated.txt");
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		
		MultiCompress mc = new MultiCompress(wordsDict, 2);
		CompressedCounter bigrams = CompressedCounterFactory.load("D:/MissingWord/ngrams/freqBigrams.txt", mc);
		
		List<FixEntry> noScore = new ArrayList<>();
		List<Double> scores = new ArrayList<>();
		for(FixEntry entry : fixEntries)
		{
			int fixIndex = entry.getFixIndex();
			try
			{
				double oneScore = score(entry.getTaggedSentence().getToken(fixIndex - 1), entry.getTaggedSentence().getToken(fixIndex), bigrams, unigrams, matrix, wordsDict, 1);
				double twoScore = score(entry.getTaggedSentence().getToken(fixIndex), entry.getTaggedSentence().getToken(fixIndex + 1), bigrams, unigrams, matrix, wordsDict, 0);
				entry.multLogScore(oneScore);
				entry.multLogScore(twoScore);
				scores.add(oneScore + twoScore);
			}
			catch(IllegalArgumentException e)
			{
				noScore.add(entry);
			}
		}
		for(FixEntry entry : noScore)
		{
			entry.multLogScore(ListUtils.mean(scores));
		}
	}
	private double score(String one, String two, CompressedCounter bigrams, Counter<String> unigrams, SimilarityMatrix matrix, Dictionary wordsDict, int variantWord) throws IllegalArgumentException
	{
		Map<String, Double> similarOne = matrix.similarityDataAndSelf(one);
		Map<String, Double> similarTwo = matrix.similarityDataAndSelf(two);
		double logScore = 0;
		double normalizingFactor = 0;
		for(String simOne : similarOne.keySet())
		{
			if(!unigrams.similarCount(simOne, one, 2))
			{
				continue;
			}
			for(String simTwo : similarTwo.keySet())
			{
				if(!unigrams.similarCount(simTwo, two, 2))
				{
					continue;
				}
				if(wordsDict.contains(simOne) && wordsDict.contains(simTwo) && unigrams.getCount(simOne) > 10000 && unigrams.getCount(simTwo) > 10000)
				{
					double cosine = similarOne.get(simOne) * similarTwo.get(simTwo);
					String variant = variantWord == 0 ? simOne : simTwo;
					double score = Math.log((double) (bigrams.getCount(simOne, simTwo) + 1) / unigrams.getCount(variant)) * cosine;
					logScore += score;
					normalizingFactor += cosine;
				}
			}
		}
		if(Math.abs(logScore) < 0.0001)
		{
			throw new IllegalArgumentException();
		}
		return logScore / normalizingFactor;
	}
}