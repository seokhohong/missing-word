package learnModelWeights;

import java.util.Map;

import main.Gold;
import utils.Pickle;
import correct.FixOptions;

public class FixOptionEval 
{
	public static void main(String[] args)
	{
		Map<Gold, FixOptions> fixOptions = (Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/corrScoring/1000fixOptions.ser");
		for(Gold gold : fixOptions.keySet())
		{
			if(!fixOptions.get(gold).getFixWords().contains(gold.removed()))
			{
				System.out.println(gold.removed()+"@"+gold);
			}
			for(String fixWord : fixOptions.get(gold).getFixWords())
			{
				if(fixOptions.get(gold).getScore(fixWord) > 0)
				{
					System.out.println("NonZero Score");
				}
			}
		}
	}
}
