package learnModelWeights;

import java.util.*;

public class AccuracyMetric 
{
	private static final int MIN = -7;
	public static void main(String[] args)
	{
		Map<String, Double> map = new HashMap<>();
		map.put("B", 0d);
		map.put("C", -1d);
		map.put("A", 0d);
		map.put("E", 0d);
		System.out.println(of(map, "B"));
	}
	//non giant negative
	private static double min(Collection<Double> values)
	{
		double min = 0;
		for(double value : values)
		{
			if(value < min && value > -1E5)
			{
				min = value;
			}
		}
		return min;
	}
	public static double of(Map<String, Double> map, String word)
	{
		if(!map.containsKey(word))
		{
			return MIN;
		}
		double sum = 0;
		for(String candidate : map.keySet())
		{
			sum += Math.exp(map.get(candidate));
		}
		double score = map.get(word) - Math.log(sum + 1);
		return score;
	}
	
}
