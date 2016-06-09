package mwutils;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import strComp.MultiCompress;
import utils.Read;
import utils.Read.LineOperation;

public class CompressedCounterFactory 
{
	private static final int BITS_THRESHOLD = 26;
	public static CompressedCounter getInstance(MultiCompress mc)
	{
		if(mc.bitsRequired() > BITS_THRESHOLD)
		{
			return new MapCompressor(mc);
		}
		return new ArrayCompressor(mc);
	}
	public static CompressedCounter load(String filename, MultiCompress mc)
	{
		FrequencyCounter freq = new FrequencyCounter();
		Read.byLine(filename, freq);
		if(mc.bitsRequired() > BITS_THRESHOLD)
		{
			return new MapCompressor(mc, freq.map);
		}
		else
		{
			return new ArrayCompressor(mc, freq.map);
		}
	}
	static class FrequencyCounter implements LineOperation
	{
		private TLongIntMap map = new TLongIntHashMap();
		public void read(String line)
		{
			String[] split = line.split("\\\\");
			if(split.length == 2)
			{
				map.put(Long.parseLong(split[0]), Integer.parseInt(split[1]));
			}
		}
	}
}
