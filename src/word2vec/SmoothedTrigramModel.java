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
import utils.ListUtils;
import correct.FixEntry;
import correct.Replacer;

public class SmoothedTrigramModel implements Replacer
{
	
	@Override
	public void reweight(List<FixEntry> fixEntries) 
	{
		SimilarityMatrix matrix = SimilarityMatrix.load("C:/MissingWord/mostRelated.txt", 20);
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/2000FrequentWords.txt");
		Counter<String> unigrams = Counter.load("C:/MissingWord/2000FrequentWords.txt");
		MultiCompress mc = new MultiCompress(wordsDict, 3);
		CompressedCounter trigrams = CompressedCounterFactory.load("C:/MissingWord/ngrams/2000freqTrigrams5.txt", mc);
		
		List<FixEntry> noScore = new ArrayList<>();
		List<Double> scores = new ArrayList<>();
		
		for(FixEntry entry : fixEntries)
		{
			try
			{
				int fixIndex = entry.getFixIndex();
				double score = score(entry.getTaggedSentence().getToken(fixIndex - 1), entry.getTaggedSentence().getToken(fixIndex), entry.getTaggedSentence().getToken(fixIndex + 1), trigrams, unigrams, matrix, wordsDict, 1);
				entry.multLogScore(score);
				scores.add(score);
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
	private double score(String one, String two, String three, CompressedCounter trigrams, Counter<String> unigrams, SimilarityMatrix matrix, Dictionary wordsDict, int variantWord) throws IllegalArgumentException
	{
		int marginSimilarity = 2; //log10
		Map<String, Double> similarOne = matrix.similarityDataAndSelf(one);
		Map<String, Double> similarTwo = matrix.similarityDataAndSelf(two);
		Map<String, Double> similarThree = matrix.similarityDataAndSelf(three);
		double logScore = 0;
		double normalizingFactor = 0;
		for(String simOne : similarOne.keySet())
		{
			if(!unigrams.similarCount(simOne, one, marginSimilarity))
			{
				continue;
			}
			for(String simTwo : similarTwo.keySet())
			{
				if(!unigrams.similarCount(simTwo, two, marginSimilarity))
				{
					continue;
				}
				for(String simThree : similarThree.keySet())
				{
					if(!unigrams.similarCount(simThree, three, marginSimilarity))
					{
						continue;
					}
					if(wordsDict.contains(simOne) && wordsDict.contains(simTwo) && wordsDict.contains(simThree) && unigrams.getCount(simOne) > 10000 && unigrams.getCount(simTwo) > 10000 && unigrams.getCount(simThree) > 10000)
					{
						double cosine = similarOne.get(simOne) * similarTwo.get(simTwo) * similarThree.get(simThree);
						//freq bigram / freq unigram / freq unigram * cos(one) * cos(two)
						String varyingWord = simTwo; //could change
						double score = Math.log((double) (trigrams.getCount(simOne, simTwo, simThree) + 1) / unigrams.getCount(varyingWord)) * cosine;
						logScore += score;
						normalizingFactor += cosine;
					}
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