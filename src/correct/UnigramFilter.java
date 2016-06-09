package correct;

import mwutils.Counter;
import mwutils.Utils;

public class UnigramFilter implements AdvancedReplacer 
{
	private int freq;
	private Counter<String> unigrams;
	public UnigramFilter(int freq)
	{
		this.freq = freq;
	}
	@Override
	public void reweight(FixOptions fixOptions, double weight) 
	{
		for(String fixWord : fixOptions.getFixWords())
		{
			if(unigrams.getCount(Utils.getToken(fixWord)) < freq)
			{
				fixOptions.multLogScore(fixWord, -10000000);
			}
		}
	}
	@Override
	public void load() 
	{
		unigrams = Counter.load("C:/MissingWord/frequentWords.txt");
	}
	@Override
	public void unload() 
	{
		unigrams = null;
		
	}

}
