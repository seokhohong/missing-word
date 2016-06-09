package mwutils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import utils.ArrayUtils;
import utils.ListUtils;

public class Utils 
{
	private static Random rnd = new Random();
	public static String removeWord(String sentence)
	{
		String[] tokens = sentence.split(" ");
		return ListUtils.join(Arrays.asList(ArrayUtils.cutout(tokens, rnd.nextInt(tokens.length - 3) + 1)), " ");
	}
	public static String getToken(String chunk)
	{
		if(chunk.isEmpty()) return "";
		if(chunk.equals("_")) return "";
		return chunk.split("_")[0];
	}
	public static String getTag(String chunk)
	{
		if(chunk.isEmpty()) return "";
		if(chunk.equals("_")) return "";
		return chunk.split("_")[1];
	}
	public static String getElem(String[] elems, int index)
	{
		if(index < 0 || index >= elems.length)
		{
			return "";
		}
		return elems[index];
	}
	public static double normSS(List<Integer> counts)
	{
		double ss = 0;
		int sum = 0;
		for(int val : counts)
		{
			ss += val * val;
			sum += val;
		}
		return Math.sqrt(ss) / sum;
	}
	public static double normSS(int[] counts)
	{
		double ss = 0;
		int sum = 0;
		for(int val : counts)
		{
			ss += val * val;
			sum += val;
		}
		return Math.sqrt(ss) / sum;
	}
}
