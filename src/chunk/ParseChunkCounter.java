package chunk;

import java.util.HashMap;
import java.util.Map;

import utils.Read;
import utils.Read.LineOperation;

public class ParseChunkCounter 
{
	private Map<String, Integer> map = new HashMap<>();
	private int totalCount = 0;
	private ParseChunkCounter(Map<String, Integer> map)
	{
		this.map = map;
		for(String key : map.keySet())
		{
			totalCount += map.get(key);
		}
	}
	
	public static ParseChunkCounter fromGramList(String filename)
	{
		ReadGrams readGrams = new ReadGrams();
		Read.byLine(filename, readGrams);

		return new ParseChunkCounter(readGrams.gramCounter);
	}
	
	public static ParseChunkCounter fromCounter(String filename)
	{
		ReadCounter readCounter = new ReadCounter();
		Read.byLine(filename, readCounter);
		return new ParseChunkCounter(readCounter.map);
	}
	
	static class ReadGrams implements LineOperation
	{
		Map<String, Integer> gramCounter = new HashMap<>();
		@Override
		public void read(String line) 
		{
			if(!gramCounter.containsKey(line))
			{
				gramCounter.put(line, 0);
			}
			gramCounter.put(line, gramCounter.get(line) + 1);
		}
	}
	
	static class ReadCounter implements Read.LineOperation
	{
		private Map<String, Integer> map = new HashMap<>();
		@Override
		public void read(String line) 
		{
			String[] split = line.split("\\\\");
			int count = Integer.parseInt(split[1]);
			map.put(split[0], count);
		}
	}
	public int count(String key)
	{
		if(map.containsKey(key))
		{
			return map.get(key);
		}
		return 0;
	}
	public double prob(String key)
	{
		if(!map.containsKey(key))
		{
			return 1 / (totalCount + 1);
		}
		return Math.log(map.get(key) + 1) - Math.log(totalCount + 1);
	}
}
