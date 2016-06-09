package correct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;


public class WeightedEditDistance 
{
	public static void main(String[] args)
	{
		Map<String, Double> weights = new HashMap<>();
		weights.put("'s", 0.0);
		weights.put("in", -8.0);
		weights.put("on", -8.4);
		System.out.println(new WeightedEditDistance(weights).optimize(50));
	}
	private Map<String, Double> weights;
	public WeightedEditDistance(Map<String, Double> weights)
	{
		this.weights = weights;
	}
	public String optimize(int retain)
	{
		Set<Character> charBank = getCharBank(weights.keySet());
		List<ScoredWord> oldWords = new ArrayList<>();
		for(Character c : charBank)
		{
			oldWords.add(new ScoredWord(Character.toString(c)));
		}
		Collections.sort(oldWords);
		while(true)
		{
			Set<ScoredWord> newWordsSet = new HashSet<>();
			for(ScoredWord oldWord : oldWords)
			{
				for(Character c : charBank)
				{
					newWordsSet.add(new ScoredWord(oldWord.word + c));
					newWordsSet.add(new ScoredWord(c + oldWord.word));
				}
			}
			List<ScoredWord> newWords = new ArrayList<>(newWordsSet);
			Collections.sort(newWords);
			if(newWords.get(0).score > oldWords.get(0).score)
			{
				break;
			}
			oldWords = newWords.subList(0, Math.min(newWords.size(), retain));
		}
		return oldWords.get(0).word;
	}
	public Set<Character> getCharBank(Collection<String> words)
	{
		Set<Character> characters = new HashSet<>();
		for(String word : words)
		{
			for(char c : word.toCharArray())
			{
				characters.add(c);
			}
		}
		return characters;
	}
	class ScoredWord implements Comparable<ScoredWord>
	{
		private String word;
		private double score;
		private ScoredWord(String word)
		{
			this.word = word;
			score();
		}
		//lower is better
		private void score()
		{
			score = 0;
			for(String otherWord : weights.keySet())
			{
				score += StringUtils.getLevenshteinDistance(this.word, otherWord) * Math.exp(weights.get(otherWord));
			}
		}
		@Override
		public int compareTo(ScoredWord o) 
		{
			return Double.compare(score, o.score);
		}
		@Override
		public String toString()
		{
			return word+" "+score;
		}
	}
}
