package main;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import utils.ArrayUtils;
import utils.ListUtils;

public class Gold implements Serializable
{
	private static final Random rnd = new Random();
	public static final String DELIM = "@";
	private String removed;									public String removed() { return removed; }
	private int removeIndex;								public int removeIndex() { return removeIndex; }
	private String incorrectSentence;						public String incorrectSentence() { return incorrectSentence; }
	
	public Gold(String removed, int removeIndex, String incorrectSentence)
	{
		this.removed = removed;
		this.removeIndex = removeIndex;
		this.incorrectSentence = incorrectSentence;
	}
	
	public static Gold makeFrom(String line)
	{
		String[] tokens = line.split(" ");
		int removeIndex = rnd.nextInt(tokens.length - 2) + 1;
		String removed = tokens[removeIndex];
		tokens = ArrayUtils.cutout(tokens, removeIndex);
		String incorrectSentence = ListUtils.join(Arrays.asList(tokens), " ");
		return new Gold(removed, removeIndex, incorrectSentence);
	}
	
	public static Gold parse(String line)
	{
		String[] split = line.split(DELIM);
		return new Gold(split[2], Integer.parseInt(split[1]), split[0]);
	}
	
	public String[] getTokens()
	{
		String[] bigTokens = incorrectSentence.split(" ");
		String[] tokens = new String[bigTokens.length];
		for(int a = 0; a < bigTokens.length; a++)
		{
			tokens[a] = bigTokens[a].substring(0, bigTokens[a].indexOf("_"));
		}
		return tokens;
	}
	public String[] getTags()
	{
		String[] bigTokens = incorrectSentence.split(" ");
		String[] tokens = new String[bigTokens.length];
		for(int a = 0; a < bigTokens.length; a++)
		{
			tokens[a] = bigTokens[a].substring(bigTokens[a].indexOf("_") + 1);
		}
		return tokens;
	}
	
	public String removedToken()
	{
		return removed.split("_")[0];
	}
	
	public double[] getLabelVector()
	{
		double[] labels = new double[incorrectSentence.split(" ").length];
		labels[removeIndex - 1] = 1;
		return labels;
	}
	public int length()
	{
		return incorrectSentence.split(" ").length;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof Gold)
		{
			Gold other = (Gold) o;
			return other.incorrectSentence.equals(incorrectSentence) && other.removed.equals(removed);
		}
		return false;
	}
	public int hashCode()
	{
		return incorrectSentence.hashCode();
	}
	public String toString()
	{
		return incorrectSentence+DELIM+removeIndex+DELIM+removed;
	}
}