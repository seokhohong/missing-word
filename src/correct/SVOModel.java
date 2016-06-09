package correct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dep.DepParser;
import dep.Dependency;
import dep.DependencyCache;
import main.Gold;
import main.GoldStandard;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class SVOModel implements AdvancedReplacer 
{
	private static Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
	public static void main(String[] args)
	{
		SVOModel model = new SVOModel(new DependencyCache("cache", new GoldStandard(25, 1000).getGolds()));
		model.load();
		System.out.println();
	}
	public SVOModel(DependencyCache cache)
	{
		this.cache = cache;
	}
	private void make()
	{
		MultiCompress mc = new MultiCompress(wordsDict, 3);
		CompressedCounter svoCounter = CompressedCounterFactory.getInstance(mc);
		Multitasker multitasker = new Multitasker(5);
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new ExtractSVO("D:/MissingWord/train/depPart"+a+".txt", svoCounter));
		}
		multitasker.done();
		svoCounter.export("C:/MissingWord/dep/svo.txt");
	}
	class ExtractSVO implements Runnable, LineOperation
	{
		private String filename;
		private CompressedCounter counter;
		private int numLine = 0;
		public ExtractSVO(String filename, CompressedCounter counter)
		{
			this.filename = filename;
			this.counter = counter;
		}
		@Override
		public void read(String line) 
		{
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+counter.keySet().size());
			}
			List<Dependency> deps = new ArrayList<>();
			for(String depChunk : line.split("@"))
			{
				try
				{
					deps.add(Dependency.parse(depChunk));
					for(String[] triple : extractSvo(deps))
					{
						counter.add(triple);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					return;
				}
			}

			numLine ++;
		}

		@Override
		public void run() 
		{
			Read.byLine(filename, this);
		}
		
	}
	private List<String[]> extractSvo(List<Dependency> deps)
	{
		List<String[]> triples = new ArrayList<>();
		for(Dependency dep : deps)
		{
			if(dep.getRelation().contains("nsubj"))
			{
				for(Dependency dep2 : deps)
				{
					if(dep2.getHeadChunk().equals(dep.getHeadChunk()) && (dep2.getRelation().contains("comp") || dep2.getRelation().contains("dobj")))
					{
						triples.add(new String[] {dep.getDepToken(), dep.getHeadToken(), dep2.getDepToken()});
					}
				}
			}
		}
		return triples;
	}
	MultiCompress mc = new MultiCompress(wordsDict, 3);
	private CompressedCounter svoCounter;
	List<Gold> goldsList;
	@Override
	public void load() 
	{
		svoCounter = CompressedCounterFactory.load("C:/MissingWord/dep/svo.txt", mc);
	}

	@Override
	public void unload() 
	{
		svoCounter = null;
		cache.done();
	}

	private DependencyCache cache; 
	
	@Override
	public void reweight(FixOptions fixOptions, double weight) 
	{
		
		Map<String, List<Dependency>> deps = cache.retrieve(fixOptions.getGold());
		for(String fixWord : fixOptions.getFixWords())
		{
			double sum = 0;
			List<String[]> triples = extractSvo(deps.get(fixWord));
			for(String[] triple : triples)
			{
				sum += Math.log(svoCounter.getCount(triple) + 1);
			}
			if(triples.size() != 0)
			{
				fixOptions.multLogScore(fixWord, sum / triples.size());
			}
		}
	}

}
