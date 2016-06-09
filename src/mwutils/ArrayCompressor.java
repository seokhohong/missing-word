package mwutils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import gnu.trove.map.TLongIntMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import strComp.MultiCompress;
import utils.ArrayUtils;

public class ArrayCompressor implements CompressedCounter
{
	private MultiCompress mc;
	private int[] array;
	public ArrayCompressor(MultiCompress mc)
	{
		this.mc = mc;
		array = new int[1 << mc.bitsRequired()];
	}
	public ArrayCompressor(MultiCompress mc, TLongIntMap map)
	{
		this(mc);
		for(long key : map.keys())
		{
			array[(int) key] = map.get(key);
		}
	}
	@Override
	public void add(String... elems) 
	{
		int encode = (int) mc.encode(elems);
		if(array[encode] < Integer.MAX_VALUE)
		{
			array[encode] += 1;
		}
	}
	@Override
	public int getCount(String... elems) 
	{
		int encode = (int) mc.encode(elems);
		if(encode > array.length) return 0;
		return array[encode];
	}
	public TLongSet keySet()
	{
		TLongSet keySet = new TLongHashSet();
		for(int a = 0; a < array.length; a++)
		{
			if(array[a] > 0)
			{
				keySet.add((long) a);
			}
		}
		return keySet;
	}
	public int keySize()
	{
		return -1;
	}
	public void export(String filename)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			for(int a = 0; a < array.length; a++)
			{
				if(array[a] != 0)
				{
					writer.write(a+"\\"+array[a]+"\n");
				}
			}
			writer.close();
		}
		catch(IOException e) {}
	}
}
