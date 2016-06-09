package strComp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.Read;

public class Dictionary 
{
	private Map<String, Integer> map = new HashMap<>();
	private String[] decoding;
	private Dictionary(Map<String, Integer> map)
	{
		this.map = map;
		decoding = new String[map.size() + 1];
		for(String key : map.keySet())
		{
			decoding[map.get(key)] = key;
		}
	}
	public static Dictionary fromCounterFile(String file)
	{
		Map<String, Integer> compressionMapping = new HashMap<>();
		List<String> lines = Read.from(file);
		//Super iffy and bad code because HashSet has no ordering guarantee!! :(
		Set<String> elimDup = new HashSet<>();
		for(String line : lines)
		{
			elimDup.add(line.split("\\\\")[0]);
		}
		List<String> finalList = new ArrayList<>(elimDup);
		for(int a = 0; a < finalList.size(); a++)
		{
			compressionMapping.put(finalList.get(a), a);
		}
		return new Dictionary(compressionMapping);
	}
	public void threshold(int count)
	{
		for(String word : new HashSet<>(map.keySet()))
		{
			if(map.get(word) < count)
			{
				map.remove(word);
			}
		}
	}
	public static Dictionary from(String file)
	{
		Map<String, Integer> compressionMapping = new HashMap<>();
		List<String> lines = Read.from(file);
		for(int a = 0; a < lines.size(); a++)
		{
			compressionMapping.put(lines.get(a), a);
		}
		return new Dictionary(compressionMapping);
	}
	public int encode(String str)
	{
		if(map.containsKey(str))
		{
			return map.get(str);
		}
		else return map.size() + 1;
	}
	public boolean contains(String str)
	{
		return map.containsKey(str);
	}
	public String decode(int index)
	{
		if(index < decoding.length)
		{
			return decoding[index];
		}
		return null;
	}
	public Set<String> keySet()
	{
		return map.keySet();
	}
	public int size() 
	{
		return map.keySet().size();
	}
	public int bitsRequired()
	{
		return (int) (Math.log(map.size()) / Math.log(2)) + 1;
	}
}
