package correct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import stanford.StanfordTagger;
import utils.ArrayUtils;
import utils.ListUtils;
import main.Gold;
import mwutils.Counter;
import mwutils.TaggedSentence;
import mwutils.Utils;

public class FixOptions implements Serializable
{
	private static final int MIN_SCORE = -7;
	private Gold gold;											public Gold getGold() { return gold; }
																public int getFixIndex() { return gold.removeIndex(); }							
	private Map<String, Double> scores = new HashMap<>();		public Map<String, Double> getScores() { return scores; }
																public double getScore(String fixWord) { return scores.get(fixWord); }
																public Set<String> getFixWords() { return scores.keySet(); }
	private String[] newChunks;
	public FixOptions(Gold gold, List<String> fixWords, StanfordTagger tagger)
	{
		this.gold = gold;
		make(tagger, fixWords);
	}
	private void make(StanfordTagger tagger, List<String> fixWords)
	{
		String[] oldTokens = gold.getTokens();
		newChunks = ArrayUtils.splice(oldTokens, gold.removeIndex(), Utils.getToken(fixWords.get(0)));
		newChunks = tagger.tagSentence(ArrayUtils.join(newChunks, " ")).getChunks();
		
		for(String fixWord : fixWords)
		{
			scores.put(fixWord, 0d);
		}
	}
	public String getToken(String fixWord, int index)
	{
		if(index == gold.removeIndex())
		{
			return Utils.getToken(fixWord);
		}
		return Utils.getToken(Utils.getElem(newChunks, index));
	}
	public String getTag(String fixWord, int index)
	{
		if(index == gold.removeIndex())
		{
			return Utils.getTag(fixWord);
		}
		return Utils.getTag(Utils.getElem(newChunks, index));
	}
	public String fill(String fixWord)
	{
		String[] copy = fillArray(fixWord);
		return ArrayUtils.join(copy, " ");
	}
	public String[] fillArray(String fixWord)
	{
		String[] copy = Arrays.copyOf(newChunks, newChunks.length);
		copy[gold.removeIndex()] = fixWord;
		return copy;
	}
	public List<FixEntry> expandFixes(StanfordTagger tagger)
	{
		List<FixEntry> fixes = new ArrayList<>();
		String[] oldTokens = gold.getTokens();
		String[] newChunks = ArrayUtils.splice(oldTokens, gold.removeIndex(), "FILLER");
		String[] taggedChunks = tagger.tag(ArrayUtils.join(newChunks, " ")).split(" ");
		for(String word : scores.keySet())
		{
			String[] taggedChunksCopy = Arrays.copyOf(taggedChunks, taggedChunks.length);
			taggedChunksCopy[gold.removeIndex()] = word;
			fixes.add(new FixEntry(TaggedSentence.fromTaggedLine(ArrayUtils.join(taggedChunksCopy, " ")), gold.removeIndex()));
		}
		return fixes;
	}
	public void resetWeights()
	{
		for(String fixWord : scores.keySet())
		{
			scores.put(fixWord, 0d);
		}
	}
	public List<Double> makeFeatures(List<String> tagsList, Counter<String> unigramCounter)
	{
		List<Double> features = new ArrayList<>();
	
		for(String tag : tagsList)
		{
			double tagWeight = 0;
			for(String fixWord : getFixWords())
			{
				if(Utils.getTag(fixWord).equals(tag))
				{
					tagWeight += Math.exp(scores.get(fixWord));
				}
			}
			features.add(tagWeight);
		}
		ListUtils.normalize(features);
		for(String tag : tagsList)
		{
			double tagCount = 0;
			for(String fixWord : getFixWords())
			{
				if(Utils.getTag(fixWord).equals(tag))
				{
					tagCount += unigramCounter.getCount(Utils.getToken(fixWord));
				}
			}
			features.add(Math.log(tagCount + 1));
		}
		List<String> fixWords = new ArrayList<>(getFixWords());
		Collections.sort(fixWords, new WordFrequency(unigramCounter));
		//word frequency quintiles
		int tiles = 5;
		for(int a = 0; a < tiles; a++)
		{
			int index = (int) ( (double) getFixWords().size() / tiles * a );
			features.add(Math.log(unigramCounter.getCount(Utils.getToken(fixWords.get(index)))));
		}
		for(int a = -2; a <= 2; a++)
		{
			if(a != 0)
			{
				features.add(Math.log(unigramCounter.getCount(Utils.getElem(gold.getTokens(), gold.removeIndex() + a)) + 1));
			}
		}
		features.addAll(tagToIndicators(tagsList, Utils.getElem(gold.getTags(), gold.removeIndex() - 2)));
		features.addAll(tagToIndicators(tagsList, Utils.getElem(gold.getTags(), gold.removeIndex() - 1)));
		features.addAll(tagToIndicators(tagsList, Utils.getElem(gold.getTags(), gold.removeIndex() + 1)));
		features.addAll(tagToIndicators(tagsList, Utils.getElem(gold.getTags(), gold.removeIndex() + 2)));
		//neighboring tags
		return features;
	}
	class ScoredWord implements Comparable<ScoredWord>
	{
		private String word;				public String getWord() { return word; }
		private double score;				public double getScore() { return score; }
		ScoredWord(String word, double score)
		{
			this.word = word;
			this.score = score;
		}
		@Override
		public int compareTo(ScoredWord o) 
		{
			return Double.compare(score, o.score);
		}
	}
	public List<ScoredWord> sortedScores()
	{
		List<ScoredWord> list = new ArrayList<>();
		for(String word : scores.keySet())
		{
			list.add(new ScoredWord(word, scores.get(word)));
		}
		Collections.sort(list);
		return list;
	}
	class WordFrequency implements Comparator<String>
	{
		Counter<String> unigramCounter;
		WordFrequency(Counter<String> unigramCounter)
		{
			this.unigramCounter = unigramCounter;
		}
		@Override
		public int compare(String arg0, String arg1) 
		{
			return Integer.compare(unigramCounter.getCount(Utils.getToken(arg0)), unigramCounter.getCount(Utils.getToken(arg1)));
		}
	}
	private List<Double> tagToIndicators(List<String> tagsList, String tag)
	{
		List<Double> indicators = new ArrayList<>();
		for(String aTag : tagsList)
		{
			if(!aTag.equals(tag))
			{
				indicators.add(0d);
			}
			else
			{
				indicators.add(1d);
			}
		}
		return indicators;
	}
	public boolean keepingTrackOf(String word)
	{
		return scores.containsKey(word);
	}
	public String guessWord()
	{
		Map<String, Double> words = new HashMap<>();
		for(String word : scores.keySet())
		{
			words.put(Utils.getToken(word), scores.get(word));
		}
		return new WeightedEditDistance(words).optimize(20);
	}
	public String bestWord()
	{
		String bestWord = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for(String word : scores.keySet())
		{
			if(scores.get(word) > bestScore)
			{
				bestScore = scores.get(word);
				bestWord = word;
			}
		}
		return bestWord;
	}
	public double evaluateGuess(List<Gold> goldList)
	{
		String guessedWord = guessWord();
		String removedWord = Utils.getToken(gold.removed());
		int score = removedWord.length() - StringUtils.getLevenshteinDistance(removedWord, guessedWord);
		System.out.println(goldList.indexOf(gold)+" "+removedWord+" "+guessedWord+" "+score+" "+gold.incorrectSentence());
		return score;
	}
	public void recalibrate()
	{
		double maxScore = Collections.max(scores.values());
		for(String fixWord : scores.keySet())
		{
			scores.put(fixWord, scores.get(fixWord) - maxScore);
		}
		List<String> newWords = new ArrayList<>();
		for(String fixWord : scores.keySet())
		{
			if(scores.get(fixWord) > MIN_SCORE) //e^-x is pretty small
			{
				newWords.add(fixWord);
			}
		}
		if(newWords.size() == 0)
		{
			for(String fixWord : scores.keySet())
			{
				System.out.println(scores.get(fixWord)+" "+fixWord);
			}
			System.out.println("EXTINCTION");
			System.exit(0);
		}
		Map<String, Double> newScores = new HashMap<>();
		for(String newWord : newWords)
		{
			newScores.put(newWord, scores.get(newWord));
		}
		scores = newScores;
	}
	public void multLogScore(String fixWord, double value)
	{
		if(!scores.containsKey(fixWord))
		{
			scores.put(fixWord, 0d);
		}
		scores.put(fixWord, scores.get(fixWord) + value);
	}
	public double scoreOf(String token)
	{
		for(String fixWord : getFixWords())
		{
			if(Utils.getToken(fixWord).equals(token))
			{
				return getScore(fixWord);
			}
		}
		return MIN_SCORE;
	}
	public String toString()
	{
		return gold.toString();
	}
}
