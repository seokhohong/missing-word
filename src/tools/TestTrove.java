package tools;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

public class TestTrove 
{
	public static void main(String[] args)
	{
		TLongIntMap map = new TLongIntHashMap();
		System.out.println(map.get(0L));
	}
}
