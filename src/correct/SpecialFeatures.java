package correct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import dep.ProcessDeps;
import utils.ListUtils;
import utils.Pickle;
import utils.Write;
import main.Gold;
import main.GoldStandard;

public class SpecialFeatures 
{
	public static void main(String[] args)
	{
		new SpecialFeatures().go();
	}
	private void go()
	{
		TrigramModel model = new TrigramModel(0);
		model.load();
		Map<Gold, FixOptions> fixOptions = (Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/corrScoring/300fixOptions.ser");
		List<Gold> goldList = new GoldStandard(25, 300).getGolds();
		List<String> data = new ArrayList<>();
		for(Gold gold : goldList)
		{
			data.add(ListUtils.join(model.getSpecialFeatures(fixOptions.get(gold)), " "));
		}
		Write.to("C:/MissingWord/corrScoring/trigramResults.txt", data);
	}
	public static void exportSpecialFeatures(String filename, List<Gold> goldList, AdvancedReplacer model, Map<Gold, FixOptions> fixOptions)
	{
		List<String> data = new ArrayList<>();
		for	(Gold gold : goldList)
		{
			data.add(ListUtils.join(SpecialFeatures.getSpecialFeatures(model, fixOptions.get(gold)), " "));
		}
		Write.to(filename, data);
	}
	public static List<Double> getSpecialFeatures(AdvancedReplacer replacer, FixOptions fixOptions)
	{
		List<Double> specialFeatures = new ArrayList<>();
		fixOptions.resetWeights();
		replacer.reweight(fixOptions, 1);
		List<Double> scores = new ArrayList<>();
		for(String fixWord : fixOptions.getFixWords())
		{
			scores.add(fixOptions.getScore(fixWord));
		}
		Collections.sort(scores);
		Collections.reverse(scores);
		ProcessDeps.addFixedFeaturesFrom(specialFeatures, scores, 10, false);
		return specialFeatures;
	}
}
