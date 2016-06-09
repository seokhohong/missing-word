package tools;

import java.util.List;

import utils.Read;

public class CommaDisparity 
{
	public static void main(String[] args)
	{
		new CommaDisparity().go();
	}
	private void go()
	{
		System.out.println(commaFrequencyTest("C:/MissingWord/test_v2.txt"));
		System.out.println(commaFrequency("C:/MissingWord/train/tokensPart2.txt"));
		System.out.println(commaFrequency("C:/MissingWord/gold/15.txt"));
		System.out.println(commaFrequency("C:/MissingWord/train/corpusPart2.txt"));
	}
	public static double commaFrequencyTest(String file)
	{
		List<String> lines = Read.from(file);
		int commas = 0;
		int other = 0;
		for(String line : lines)
		{
			line = line.substring(line.indexOf(",") + 1);
			for(char a : line.toCharArray())
			{
				if(a == ',')
				{
					commas ++;
				}
				else
				{
					other ++;
				}
			}
		}
		return (double) commas / other;
	}
	public static double commaFrequency(String file)
	{
		List<String> lines = Read.from(file);
		int commas = 0;
		int other = 0;
		for(String line : lines)
		{
			for(char a : line.toCharArray())
			{
				if(a == ',')
				{
					commas ++;
				}
				else
				{
					other ++;
				}
			}
		}
		return (double) commas / other;
	}
}
