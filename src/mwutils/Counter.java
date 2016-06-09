package mwutils;

import gnu.trove.TCollections;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.Read;
import utils.Read.LineOperation;

public class Counter<T> 
{
	private TObjectIntMap<T> map = TCollections.synchronizedMap(new TObjectIntHashMap<T>());
	private long totalCount = 0;	
	public Counter()
	{
		
	}
	public void add(T elem)
	{
		if(map.get(elem) < Integer.MAX_VALUE)
		{
			map.adjustOrPutValue(elem, 1, 1);
			totalCount ++;
		}
	}
	public void addAll(Collection<T> coll)
	{
		for(T elem : coll)
		{
			add(elem);
		}
	}
	public void addCounter(Counter<T> counter)
	{
		for(T elem : counter.keySet())
		{
			put(elem, counter.getCount(elem));
		}
	}
	public void put(T elem, int count)
	{
		totalCount += count - getCount(elem);
		map.put(elem, count);
	}
	public int getCount(T elem)
	{
		if(!map.containsKey(elem))
		{
			return 0;
		}
		return map.get(elem);
	}
	public void remove(T elem)
	{
		map.remove(elem);
	}
	public boolean contains(T elem)
	{
		return getCount(elem) != 0;
	}
	/** 
	 * 
	 * Return log probability
	 * 
	 * @param elem
	 * @return
	 */
	public double getProb(T elem)
	{
		return Math.log(map.get(elem) + 1) - Math.log(totalCount); 
	}
	public boolean similarCount(T one, T two, int log10)
	{
		return Math.abs(Math.log10(getCount(one)) - Math.log10(getCount(two))) < log10;
	}
	public double getNormSS()
	{
		double ss = 0;
		for(T key : map.keySet())
		{
			ss += map.get(key) * map.get(key);
		}
		ss = Math.sqrt(ss);
		return ss / totalCount;
	}
	public Set<T> keySet()
	{
		return map.keySet();
	}
	public long size()
	{
		return totalCount;
	}
	public void threshold(int threshold)
	{
		for(T key : new HashSet<>(map.keySet()))
		{
			if(getCount(key) < threshold)
			{
				map.remove(key);
			}
		}
	}
	public void export(String filename)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			for(T key : map.keySet())
			{
				writer.write(key+"\\"+map.get(key)+"\n");
			}
			writer.close();
		}
		catch(IOException e) {}
	}
	public static Counter<String> load(String filename)
	{
		Counter<String> counter = new Counter<>();
		FrequencyCounter freq = new FrequencyCounter(counter);
		Read.byLine(filename, freq);
		return counter;
	}
	public static Counter<String> tally(List<String> filenames)
	{
		Counter<String> counter = new Counter<>();
		LineCounter lineCounter = new LineCounter(counter);
		for(String filename : filenames)
		{
			Read.byLine(filename, lineCounter);
		}
		return counter;
	}
	static class LineCounter implements LineOperation
	{
		private Counter<String> counter;
		public LineCounter(Counter<String> counter)
		{
			this.counter = counter;
		}
		public void read(String line)
		{
			counter.add(line);
		}
	}
	static class FrequencyCounter implements LineOperation
	{
		private Counter<String> counter;
		public FrequencyCounter(Counter<String> counter)
		{
			this.counter = counter;
		}
		public void read(String line)
		{
			//String[] components = line.split("\\\\");
			int splitIndex = line.lastIndexOf("\\");
			
			counter.put(line.substring(0, splitIndex), Integer.parseInt(line.substring(splitIndex + 1)));
		}
	}
}
