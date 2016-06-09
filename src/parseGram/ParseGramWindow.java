package parseGram;

import java.util.ArrayList;
import java.util.List;

import utils.ListUtils;

public class ParseGramWindow 
{
	private List<ParseGram> parseGrams = new ArrayList<>();
	public ParseGramWindow(String line)
	{
		for(String gram : line.split("~"))
		{
			parseGrams.add(new ParseGram(gram));
		}
	}
	public void removeWord()
	{
		for(ParseGram parseGram : parseGrams)
		{
			parseGram.removeWord();
		}
	}
	public void removeMarkovOrder(int order)
	{
		for(ParseGram parseGram : parseGrams)
		{
			parseGram.removeMarkovOrder(order);
		}
	}
	public String toString()
	{
		return ListUtils.join(parseGrams, ",");
	}
}
