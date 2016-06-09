package correct;

import strComp.Dictionary;
import strComp.MultiCompress;
import utils.Read;
import utils.Read.LineOperation;
import mwutils.MapCompressor;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;

import java.io.File;
import java.util.*;

public class CorrectorDict 
{
	private static final int MAX_KEYS = 1000000;
	private static final MultiCompress mc = new MultiCompress(Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt"));
	private TLongObjectMap<MapCompressor> map = new TLongObjectHashMap<MapCompressor>();
	private TLongSet exploredKeys;
	public CorrectorDict(TLongSet exploredKeys)
	{
		this.exploredKeys = exploredKeys;
	}
	public static CorrectorDict load(String filename)
	{
		LoadCorrector loader = new LoadCorrector();
		Read.byLine(filename, loader);
		return loader.corrector;
	}
	public static void makePass(CorrectorOperation op)
	{
		int part = 0;
		while(true)
		{
			System.out.println("Part "+part);
			if(!new File(op.filename(part)).exists())
			{
				System.out.println("Done!");
				break;
			}
			CorrectorDict corrector = CorrectorDict.load(op.filename(part));
			int numToDo = op.keys().toArray().length;
			int index = 0;
			System.out.println("Task Length "+numToDo);
			for(long key : op.keys().toArray())
			{
				if(index % Math.ceil((double) numToDo / 100) == 0)
				{
					System.out.println((index / Math.ceil((double) numToDo / 100))+ "%");
				}
				index ++ ;
				if(corrector.keys().contains(key))
				{
					op.found(key, corrector.get(key));
				}
			}
			part ++;
		}
	}
	public static TLongIntMap decompress(MapCompressor compressor)
	{
		TLongIntMap decompressed = new TLongIntHashMap();
		for(long key : compressor.keySet().toArray())
		{
			decompressed.put(key, compressor.getDirectCount(key));
		}
		return decompressed;
	}
	public interface CorrectorOperation
	{
		public void found(long l, MapCompressor map);
		public String filename(int part);
		public TLongSet keys();
	}
	static class LoadCorrector implements LineOperation
	{
		CorrectorDict corrector = new CorrectorDict(null);
		int numLine = 0;
		@Override
		public void read(String line) 
		{
			numLine ++;
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine);
			}
			String[] parts = line.split("@");
			corrector.map.put(Long.parseLong(parts[0]), makeMap(parts[1]));
		}
		private MapCompressor makeMap(String mapString)
		{
			String[] components = mapString.split("\\\\");
			TLongIntMap wordsMap = new TLongIntHashMap();
			for(int a = 0; a < components.length / 2; a++)
			{
				wordsMap.put(Long.parseLong(components[a * 2]), Integer.parseInt(components[a * 2 + 1]));
			}
			return new MapCompressor(mc, wordsMap);
		}
	}
	public void add(long l, String word)
	{
		MapCompressor lCompressor = null;
		synchronized(map)
		{
			if(!map.containsKey(l))
			{
				if(map.keySet().size() < MAX_KEYS && !exploredKeys.contains(l))
				{
					map.put(l, new MapCompressor(mc));
					lCompressor = map.get(l);
				}
			}
			else
			{
				lCompressor = map.get(l);
			}
		}
		if(lCompressor != null)
		{
			synchronized(lCompressor)
			{
				lCompressor.add(word);
			}
		}
	}
	public MapCompressor get(long l)
	{
		if(map.containsKey(l))
		{
			return map.get(l);
		}
		return null;
	}
	public TLongSet keys()
	{
		return map.keySet();
	}
	public List<String> export()
	{
		List<String> exportData = new ArrayList<>();
		for(long key : map.keys())
		{
			StringBuilder builder = new StringBuilder();
			builder.append(key);
			builder.append("@");
			builder.append(map.get(key).exportString());
			exportData.add(builder.toString());
		}
		return exportData;
	}
}
