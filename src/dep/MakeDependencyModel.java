package dep;

import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.Multitasker;
import dep.DependencyModel.CountEntireDependency;

public class MakeDependencyModel 
{
	public static void main(String[] args)
	{
		makeUncollapsed();
	}
	private static void makeUncollapsed()
	{
		Dictionary relationDict = Dictionary.fromCounterFile("C:/MissingWord/dep/depRelationCounter.txt");
		Dictionary unigramsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		MultiCompress mc = new MultiCompress(relationDict, unigramsDict, unigramsDict);
		CompressedCounter depCounter = CompressedCounterFactory.getInstance(mc);
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new CountEntireDependency("D:/MissingWord/train/depUncollapsedPart"+a+".txt", depCounter));
		}
		multitasker.done();
		depCounter.export("C:/MissingWord/dep/depUncollapsed.txt");
	}
}
