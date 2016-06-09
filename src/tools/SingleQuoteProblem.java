package tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.Gold;
import main.GoldStandard;

public class SingleQuoteProblem 
{
	public static void main(String[] args)
	{
		new SingleQuoteProblem().go();
	}
	private void go()
	{
		List<Gold> golds = new GoldStandard(15).getGolds();
		int numQuoteErrors = 0;
		for(Gold gold : golds)
		{
			List<String> tokens = Arrays.asList(gold.getTokens());
			
			List<Integer> indices = indicesOfQuotes(tokens);
			if(indices.size() % 2 != 0)
			{
				if(!indices.contains(0))
				{
					numQuoteErrors ++;
				}
			}
		}
		System.out.println(numQuoteErrors);
	}
	private List<Integer> indicesOfQuotes(List<String> tokens)
	{
		List<Integer> indices = new ArrayList<>();
		for(int a = 0; a < tokens.size(); a++)
		{
			if(tokens.get(a).equals("\'\'"))
			{
				indices.add(a);
			}
		}
		return indices;
	}
	private int nonOverlappingMatches(String str, String findStr)
	{
		return str.split(findStr, -1).length - 1;
	}
}
