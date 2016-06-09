package parseGram;

import java.util.Arrays;

import utils.ArrayUtils;
import utils.ListUtils;

public class ParseGram 
{
	private String[] tokens;
	public ParseGram(String parseGram)
	{
		tokens = parseGram.split("\\|");
	}
	public void removeWord()
	{
		for(int a = tokens.length; a --> 0; )
		{
			if(tokens[a].contains("/") && tokens[a].length() > 1)
			{
				tokens[a] = tokens[a].split("/")[1];
			}
		}
	}
	public void removeMarkovOrder(int order)
	{
		while(tokens.length > order)
		{
			tokens = ArrayUtils.cutout(tokens, 0);
		}
	}
	public String toString()
	{
		return ListUtils.join(Arrays.asList(tokens), "|");
	}
}
