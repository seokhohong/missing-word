package dep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import strComp.Dictionary;
import strComp.MultiCompress;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;
import word2vec.SimilarityMatrix;
import main.Gold;
import main.GoldStandard;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.TaggedSentence;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import correct.AdvancedReplacer;
import correct.FixOptions;

public class DependencyModel implements AdvancedReplacer
{
	static Dictionary relationDict = Dictionary.fromCounterFile("C:/MissingWord/dep/depRelationCounter.txt");
	static Dictionary unigramsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
	static MultiCompress entireMc = new MultiCompress(relationDict, unigramsDict, unigramsDict);
	
	private static CompressedCounter entireCounter = CompressedCounterFactory.load("C:/MissingWord/dep/wholeDep5.txt", entireMc);
	private static Counter<String> unigramCounter = Counter.load("C:/MissingWord/frequentWords.txt");
	
	private double LARGE_PENALTY = -10000000; //-Inf. causes problems
	private List<Gold> goldsList;
	//private static Map<Gold, Map<String, List<Dependency>>> allDeps = new ProcessDeps().loadDeps("D:/MissingWord/300Deps.txt", goldsList);
	public void load()
	{
		
	}
	public void unload()
	{
		cache.done();
	}
	private List<String> labels;
	private boolean decollapse;
	public DependencyModel(List<String> labels, boolean decollapse, DependencyCache cache)
	{
		this(labels, cache);
		this.decollapse = decollapse;
	}
	public DependencyModel(List<String> labels, DependencyCache cache)
	{
		this.labels = labels;
		this.cache = cache;
	}
	private DependencyCache cache;
	public void reweight(FixOptions fixOptions, double weight) 
	{
		/*
		Map<String, List<TypedDependency>> deps = new HashMap<>();
		Map<String, RunDep> depTasks = new HashMap<>();
		Multitasker multitasker = new Multitasker(7);
		for(String fixWord : fixOptions.getFixWords())
		{
			RunDep depTask = new RunDep(TaggedSentence.fromTaggedLine(fixOptions.fill(fixWord)).toString(), parser);
			multitasker.load(depTask);
			depTasks.put(fixWord, depTask);
		}
		multitasker.done();
		for(String fixWord : fixOptions.getFixWords())
		{
			deps.put(fixWord, new ArrayList<>(depTasks.get(fixWord).gs.typedDependenciesCollapsed()));
		}
		 */
		Map<String, List<Dependency>> deps = cache.retrieve(fixOptions.getGold());
		//Map<String, List<Dependency>> deps = allDeps.get(fixOptions.getGold());
		for(String fixWord : fixOptions.getFixWords())
		{
			double summedLogScore = 0;
			int depCount = 0;
			List<Dependency> thisDeps = deps.get(fixWord);
			if(decollapse)
			{
				thisDeps = splitPreps(thisDeps);
			}
			if(thisDeps == null)
			{
				System.out.println();
			}
			for(Dependency dep : thisDeps)
			{
				//summedLogScore += smoothedScore(dep, entireCounter, unigramCounter, matrix);
				boolean containsOr = false;
				for(String label : labels)
				{
					containsOr |= dep.getRelation().contains(label);
				}
				if(labels.size() == 0)
				{
					containsOr = true;
				}
				if(containsOr)
				{
					summedLogScore += roughScore(dep, entireCounter, unigramCounter);
					depCount ++;
				}
			}
			if(depCount > 0)
			{
				fixOptions.multLogScore(fixWord, summedLogScore / depCount * weight);
			}
		}
	}
	private List<Dependency> splitPreps(List<Dependency> deps)
	{
		List<Dependency> newDeps = new ArrayList<>();
		for(Dependency dep : deps)
		{
			if(dep.getRelation().contains("prep_"))
			{
				String[] prepParts = dep.getRelation().split("_");
				Dependency headDep = Dependency.getInstance(prepParts[0], dep.getHeadToken(), dep.getHeadTag(), prepParts[1], "IN");
				Dependency secondaryDep = Dependency.getInstance("pobj", prepParts[1], "IN", dep.getDepToken(), dep.getDepTag());
				newDeps.add(headDep);
				newDeps.add(secondaryDep);
			}
			else
			{
				newDeps.add(dep);
			}
		}
		return newDeps;
	}
	static Random rnd = new Random();
	public static class RunDep implements Runnable
	{
		GrammaticalStructure gs;							public GrammaticalStructure getGs() { return gs; }
		String toParse;
		DepParser parser;
		public RunDep(String toParse, DepParser parser)
		{
			this.toParse = toParse;
			this.parser = parser;
		}
		@Override
		public void run() 
		{
			gs = parser.parse(toParse);
			if(rnd.nextInt(1000) == 0)
			{
				System.out.println("DepParsed "+toParse);
			}
		}
	}
	
	private double smoothedScore(Dependency dep, CompressedCounter entireCounter, Counter<String> unigramCounter, SimilarityMatrix matrix)
	{
		Map<String, Double> simHead = matrix.similarityDataAndSelf(dep.getHeadToken());
		Map<String, Double> simDep = matrix.similarityDataAndSelf(dep.getDepToken());
		double totalLogProb = 0;
		double sumCosine = 0;
		for(String similarHead : simHead.keySet())
		{
			for(String similarDep : simDep.keySet())
			{
				double cosine = simHead.get(similarHead) * simDep.get(similarDep);
				//System.out.println(similarHead+" "+similarDep+" "+entireCounter.getCount(dep.getRelation(), similarHead, similarDep));
				totalLogProb += Math.log((double) (entireCounter.getCount(dep.getRelation(), similarHead, similarDep) + 1) / (unigramCounter.getCount(similarHead) + 1) / (unigramCounter.getCount(similarDep) + 1)) * cosine;
				sumCosine += cosine;
			}
		}
		return totalLogProb / sumCosine;
	}
	private double roughScore(Dependency dep, CompressedCounter entireCounter, Counter<String> unigramCounter)
	{
		//return Math.log((double) (entireCounter.getCount(dep.getRelation(), dep.getHead(), dep.getDep()) + 1) / (unigramCounter.getCount(dep.getHead()) + 1) / (unigramCounter.getCount(dep.getDep()) + 1)) ;
		return Math.log((double) (entireCounter.getCount(dep.getRelation(), dep.getHeadToken(), dep.getDepToken()) + 1));
	}
	
	private static void make()
	{

	}
	static class CountEntireDependency implements LineOperation, Runnable
	{
		private CompressedCounter depCounter;
		private int numLine = 0;
		private String filename;
		public CountEntireDependency(String filename, CompressedCounter depCounter)
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
					depCounter.add(dep.getRelation(), dep.getHeadToken(), dep.getDepToken());
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
