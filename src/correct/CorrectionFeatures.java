package correct;

import java.util.List;

import tag.NTagCorrector;
import main.Gold;
import main.GoldStandard;

public class CorrectionFeatures 
{
	public static void main(String[] args)
	{
		new CorrectionFeatures().make(15);
	}
	public CorrectionFeatures()
	{
		
	}
	private void make(int length)
	{
		GoldStandard goldStd = new GoldStandard(length);
		List<Gold> golds = goldStd.getGolds(length);
		System.out.println(golds.size());
		NTagCorrector model = new NTagCorrector(5, 3);
		//model.exportFeatures("C:/MissingWord/corr/5-3features.txt", golds);
	}
}
