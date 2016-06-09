package mwutils;

import gnu.trove.TCollections;
import gnu.trove.map.TLongDoubleMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TLongSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import strComp.MultiCompress;
import utils.ArrayUtils;
import utils.Read;
import utils.Read.LineOperation;

public class MapCompressor implements CompressedCounter
{
	public MultiCompress mc;
	private TLongIntMap map = TCollections.synchronizedMap(new TLongIntHashMap());
	private int totalCount;
	public MapCompressor(MultiCompress mc)
	{
		this.mc = mc;
	}
	public MapCompressor(MultiCompress mc, TLongIntMap map)
	{
		this(mc);
		this.map = map;
	}
	public void add(String... elems)
	{
		long key = mc.encode(elems);
		if(map.get(key) < Integer.MAX_VALUE)
		{
			map.adjustOrPutValue(key, 1, 1);
			totalCount ++;
		}
	}
	public int getCount(String... elems)
	{
		long key = mc.encode(elems);
		return map.get(key);
	}
	public int getDirectCount(long encoded)
	{
		return map.get(encoded);
	}
	public void mergeWith(MapCompressor other)
	{
		map.putAll(other.map);
	}
	public int[] values()
	{
		return map.values();
	}
	public int keySize()
	{
		return map.keySet().size();
	}
	public TLongSet keySet()
	{
		return map.keySet();
	}
	public int size()
	{
		return totalCount;
	}
	public void export(String filename)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			for(long key : map.keys())
			{
				writer.write(key+"\\"+map.get(key)+"\n");
			}
			writer.close();
		}
		catch(IOException e) {}
	}
	public String exportString()
	{
		StringBuilder builder = new StringBuilder();
		for(long key : map.keys())
		{
			builder.append(key);
			builder.append("\\");
			builder.append(map.get(key));
			builder.append("\\");
		}
		return builder.toString();
	}

	public static void threshold(String input, String output, int count)
	{
		Threshold thresh = new Threshold(output, count);
		Read.byLine(input, thresh);
		try
		{
			thresh.writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	static class Threshold implements LineOperation
	{
		private String output;
		private int threshold;
		BufferedWriter writer;
		public Threshold(String output, int threshold)
		{
			this.output = output;
			this.threshold = threshold;
			try
			{
				writer = new BufferedWriter(new FileWriter(output));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void read(String line) 
		{
			int count = Integer.parseInt(line.split("\\\\")[1]);
			if(count >= threshold)
			{
				try
				{
					writer.write(line);
					writer.newLine();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
