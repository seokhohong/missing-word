package correct;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import strComp.StringCompressor;
import utils.ListUtils;

public class TopCharacters 
{
	public static void main(String[] args)
	{
		TopCharacters top = new TopCharacters();
		for(int a = 0; a < 10; a++)
		{
			top.addWord("the");
		}
		for(int b = 0; b < 10; b++)
		{
			top.addWord("he");
		}
		for(int a = 0; a < 20; a++)
		{
			top.addWord("she");
		}
		System.out.println(ListUtils.print(top.getScoredSequences()));
	}
	private static final StringCompressor cmp = new StringCompressor();
	private static final int TRIGGER_PRUNE = 1000;
	private static final int PRUNE_LENGTH = 100;
	
	private int[] lenWords = new int[10];
	private TLongIntMap map = new TLongIntHashMap();
	public TopCharacters()
	{
		
	}
	public void addWord(String word)
	{
		lenWords[Math.min(word.length(), lenWords.length) - 1] ++;
		for(long comb : combinationsOf(word).toArray())
		{
			map.adjustOrPutValue(comb, 1, 1);
		}
		if(map.keys().length > TRIGGER_PRUNE)
		{
			prune();
		}
	}
	private void prune()
	{
		List<ScoredSequence> sequences = getScoredSequences();
		map.clear();
		for(int a = 0; a < PRUNE_LENGTH; a++)
		{
			map.put(sequences.get(a).encoded, sequences.get(a).count);
		}
	}
	private List<ScoredSequence> getScoredSequences()
	{
		List<ScoredSequence> sequences = new ArrayList<>();
		for(long sequence : map.keys())
		{
			sequences.add(new ScoredSequence(sequence, map.get(sequence)));
		}
		Collections.sort(sequences);
		return sequences;
	}
	public List<ScoredSequence> getTopSequences(int count)
	{
		return getScoredSequences().subList(0, count);
	}
	class ScoredSequence implements Comparable<ScoredSequence>
	{
		private int score;
		private long encoded;
		private int count;
		private ScoredSequence(long encoded, int count) //count, not score
		{
			this.encoded = encoded;
			int length = cmp.length(encoded);
			this.score = count * length;
			this.count = count;
			for(int a = 0; a < length; a++)
			{
				score -= (length - (a + 1)) * lenWords[a];
			}
		}
		@Override
		public int compareTo(ScoredSequence arg0) 
		{
			return arg0.score - score;
		}
		public String toString()
		{
			return new String(cmp.decode(encoded))+" "+score+" "+count;
		}
	}
	private static TLongList combinationsOf(String word)
	{
		TLongList list = new TLongArrayList();
		for(int a = 0; a < word.length(); a++)
		{
			generateCombinations(list, word.toCharArray(), new char[7], a, 0);
		}
		return list;
	}
	private static void generateCombinations(TLongList list, char[] available, char[] built, int index, int depth)
	{
		built[depth] = available[index];
		list.add(cmp.encode(built, depth + 1));
		if(depth == 6 || index == available.length - 1)
		{
			return;
		}
		for(int a = index + 1; a < available.length; a++)
		{
			generateCombinations(list, available, built, a, depth + 1);
		}
		built[depth] = 0;
		
	}
}
