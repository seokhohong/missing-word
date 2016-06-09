package tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mwutils.Counter;
import utils.Read;

public class ExtractTestSentences 
{
	public static void main(String[] args)
	{
		new ExtractTestSentences().go();
	}
	private void go()
	{
		List<String> lines = Read.from("C:/MissingWord/test_v2.txt");
		Counter<Integer> counter = new Counter<>();
		for(String line : lines)
		{
			line = line.substring(line.indexOf(",") + 2, line.length() - 1);
			line = line.replaceAll("\\\"\\\"", "\"");
			counter.add(line.split(" ").length);
		}
		List<Integer> sortedKeys = new ArrayList<>(counter.keySet());
		Collections.sort(sortedKeys);
		int cumul = 0;
		for(Integer key : sortedKeys)
		{
			cumul += counter.getCount(key);
			System.out.println(key +"\t" + counter.getCount(key) + "\t"+cumul);
		}
	}
}
