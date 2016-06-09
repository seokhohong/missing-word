package dep;

import strComp.Dictionary;
import strComp.MultiCompress;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class DependencyRelnHeadModel
{
	private Dictionary relationDict = Dictionary.fromCounterFile("C:/MissingWord/dep/depRelationCounter.txt");
	private Dictionary unigramsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
	private MultiCompress mc = new MultiCompress(relationDict, unigramsDict);
	private CompressedCounter depCounter;
	public static void main(String[] args)
	{
		//DependencyModel depModel = new DependencyModel();
		//depModel.test();
		DependencyRelnHeadModel.make();
	}
	public DependencyRelnHeadModel()
	{
		depCounter = CompressedCounterFactory.load("C:/MissingWord/dep/relnHeadDep.txt", mc);
	}
	public int getCount(String... elems)
	{
		return depCounter.getCount(elems);
	}
	private static void make()
	{
		Dictionary relationDict = Dictionary.fromCounterFile("C:/MissingWord/dep/depRelationCounter.txt");
		Dictionary unigramsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		MultiCompress mc = new MultiCompress(relationDict, unigramsDict);
		CompressedCounter depCounter = CompressedCounterFactory.getInstance(mc);
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new CountDependency("C:/MissingWord/train/depPart"+a+".txt", depCounter));
		}
		multitasker.done();
		depCounter.export("C:/MissingWord/dep/relnHead.txt");
	}
	static class CountDependency implements LineOperation, Runnable
	{
		private CompressedCounter depCounter;
		private int numLine = 0;;
		private String filename;
		public CountDependency(String filename, CompressedCounter depCounter)
		{
			this.depCounter = depCounter;
			this.filename = filename;
		}
		@Override
		public void read(String line) 
		{
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+depCounter.keySize());
			}
			numLine ++;
			for(String depChunk : line.split("@"))
			{
				try
				{
					Dependency dep = Dependency.parse(depChunk);
					depCounter.add(dep.getRelation(), dep.getHeadToken());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		@Override
		public void run() 
		{
			Read.byLine(filename, this);
		}
	}
}
