package learnModelWeights;

import java.util.ArrayList;
import java.util.List;

import utils.ListUtils;
import utils.Read;

public class ModelPower 
{
	public static void main(String[] args)
	{
		new ModelPower().measure("C:/MissingWord/corrScoring/dobjLabels.txt");
	}
	private void measure(String filename)
	{
		List<String> lines = Read.from(filename);
		List<Double> values = new ArrayList<>();
		for(String line : lines)
		{
			values.add(Math.exp(Double.parseDouble(line)));
		}
		System.out.println(ListUtils.mean(values));
	}
}
