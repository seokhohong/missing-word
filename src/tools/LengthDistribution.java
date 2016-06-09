package tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.Read;
import utils.Read.LineOperation;
import mwutils.Counter;

public class LengthDistribution implements LineOperation
{
	private Counter<Integer> counter = new Counter<>();
	public static void main(String[] args)
	{
		new LengthDistribution().go();
	}
	private void go()
	{
		Read.byLine("D:/MissingWord/train/tokensPart2.txt", this);
		int cumul = 0;
		List<Integer> sortedKeys = new ArrayList<>(counter.keySet());
		Collections.sort(sortedKeys);
		for(Integer key : sortedKeys)
		{
			cumul += counter.getCount(key);
			System.out.println(key +"\t" + counter.getCount(key) + "\t"+cumul);
		}
	}
	@Override
	public void read(String line) 
	{
		counter.add(line.split(" ").length);
	}

}
