package npreduce;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.Read;
import utils.Read.LineOperation;

public class CounterVariation 
{
	public static void main(String[] args)
	{
		new CounterVariation().go();
	}
	private void go()
	{
		SimpleCounter parseGrams = new SimpleCounter("C:/MissingWord/parserNPReduction.txt");
		SimpleCounter parseModGrams = new SimpleCounter("C:/MissingWord/parserModNPReduction.txt");
		System.out.println(score(parseGrams.counter, parseModGrams.counter));
	}
	public static <K> double score(Map<K, Integer> one, Map<K, Integer> two)
	{
		int totalKeys = one.keySet().size() + two.keySet().size();
		double score = 0;
		for(K oneKey : one.keySet())
		{
			if(two.containsKey(oneKey))
			{
				score += Math.log(Math.abs(one.get(oneKey) - two.get(oneKey)) + 1) - Math.log(one.get(oneKey) + two.get(oneKey));
			}
		}
		return score / totalKeys;
	}
	class SimpleCounter
	{
		Map<String, Integer> counter = new HashMap<>();
		public SimpleCounter(String filename)
		{
			Read.byLine(filename, new Reader());
		}
		class Reader implements LineOperation
		{

			@Override
			public void read(String line) 
			{
				if(!counter.containsKey(line))
				{
					counter.put(line, 0);
				}
				counter.put(line, counter.get(line) + 1);
			}
			
		}
	}
}
