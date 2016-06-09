package dep;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import strComp.Dictionary;
import strComp.MultiCompress;
import utils.ListUtils;
import utils.Multitasker;
import utils.Pickle;
import utils.Read;
import utils.Read.LineOperation;
import utils.Write;
import correct.FixOptions;
import dep.DependencyModel.RunDep;
import edu.stanford.nlp.trees.TypedDependency;
import main.Gold;
import main.GoldStandard;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.TaggedSentence;
import mwutils.Utils;

public class ProcessDeps 
{
	private Dictionary relationDict = Dictionary.fromCounterFile("C:/MissingWord/dep/depRelationCounter.txt");
	private Dictionary unigramsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
	private Dictionary tagsDict = Dictionary.fromCounterFile("C:/MissingWord/tag/tags.txt");
	private MultiCompress entireMc = new MultiCompress(relationDict, unigramsDict, unigramsDict);
	private MultiCompress smallMc = new MultiCompress(relationDict, unigramsDict);
	private CompressedCounter entireCounter = CompressedCounterFactory.load("C:/MissingWord/dep/wholeDep5.txt", entireMc);
	private CompressedCounter headCounter = CompressedCounterFactory.load("C:/MissingWord/dep/relnHead.txt", smallMc);
	private CompressedCounter depCounter = CompressedCounterFactory.load("C:/MissingWord/dep/relnDep.txt", smallMc);
	private Counter<String> unigramCounter = Counter.load("C:/MissingWord/frequentWords.txt");
	private List<String> relationList = new ArrayList<>(relationDict.keySet());
	private List<String> tagList = new ArrayList<>(tagsDict.keySet());
	private Map<String, Integer> relationIndex = makeIndex(relationList);
	private Map<String, Integer> tagsIndex = makeIndex(tagList);
	public static void main(String[] args)
	{
		new ProcessDeps().exportFeatures();
	}
	private void go()
	{
		Map<Gold, FixOptions> fixOptions = (Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/corrScoring/fixOptions.ser");
		DepParser parser = new DepParser();
		Map<Gold, Map<String, List<Dependency>>> deps = new HashMap<>();
		System.out.println(fixOptions.keySet().size());
		for(Gold gold : fixOptions.keySet())
		{
			deps.put(gold, new HashMap<>());
			Map<String, RunDep> depTasks = new HashMap<>();
			Multitasker multitasker = new Multitasker(7);
			for(String fixWord : fixOptions.get(gold).getFixWords())
			{
				RunDep depTask = new RunDep(TaggedSentence.fromTaggedLine(fixOptions.get(gold).fill(fixWord)).toString(), parser);
				multitasker.load(depTask);
				depTasks.put(fixWord, depTask);
			}
			multitasker.done();
			for(String fixWord : fixOptions.get(gold).getFixWords())
			{
				List<Dependency> dependencies = new ArrayList<>();
				for(TypedDependency td : new ArrayList<>(depTasks.get(fixWord).gs.typedDependenciesCollapsed()))
				{
					dependencies.add(Dependency.getInstance(td));
				}
				deps.get(gold).put(fixWord, dependencies);
			}
		}
		
		Pickle.dump(deps, "C:/MissingWord/corrScoring/mapGoldStringTypedDependency.ser");
	}
	private Map<Gold, Map<String, List<Dependency>>> makeDeps(Map<Gold, FixOptions> fixOptions, List<Gold> goldsList)
	{
		DepParser parser = new DepParser();
		Map<Gold, Map<String, List<Dependency>>> deps = new HashMap<>();
		for(Gold gold : goldsList)
		{
			deps.put(gold, new HashMap<>());
			Map<String, RunDep> depTasks = new HashMap<>();
			Multitasker multitasker = new Multitasker(7);
			for(String fixWord : fixOptions.get(gold).getFixWords())
			{
				RunDep depTask = new RunDep(TaggedSentence.fromTaggedLine(fixOptions.get(gold).fill(fixWord)).toString(), parser);
				multitasker.load(depTask);
				depTasks.put(fixWord, depTask);
			}
			multitasker.done();
			for(String fixWord : fixOptions.get(gold).getFixWords())
			{
				List<Dependency> dependencies = new ArrayList<>();
				for(TypedDependency td : new ArrayList<>(depTasks.get(fixWord).gs.typedDependenciesCollapsed()))
				{
					dependencies.add(Dependency.getInstance(td));
				}
				deps.get(gold).put(fixWord, dependencies);
			}
		}
		return deps;
	}
	private void exportDeps()
	{
		List<Gold> goldList = new GoldStandard(25, 300).getGolds();
		Map<Gold, FixOptions> fixOptions = (Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/corrScoring/300fixOptions.ser");
		exportDeps(makeDeps(fixOptions, goldList), goldList, "C:/MissingWord/corrScoring/300Deps.txt");
	}
	private void testLoadDeps()
	{
		List<Gold> goldsList = new GoldStandard(25, 2).getGolds();
		Map<Gold, Map<String, List<Dependency>>> deps = loadDeps("C:/MissingWord/corrScoring/savedDeps.txt", goldsList);
		System.out.println();
	}
	private void exportDeps(Map<Gold, Map<String, List<Dependency>>> deps, List<Gold> goldList, String filename)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			for(Gold gold : goldList)
			{
				Map<String, List<Dependency>> thisDeps = deps.get(gold);
				for(String fixWord : thisDeps.keySet())
				{
					StringBuilder builder = new StringBuilder();
					builder.append(fixWord);
					for(Dependency dep : thisDeps.get(fixWord))
					{
						builder.append("@");
						builder.append(dep);
					}
					writer.write(builder.toString());
					writer.newLine();
				}
				writer.newLine();
			}	
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Map<Gold, Map<String, List<Dependency>>> loadDeps(String filename, List<Gold> goldList)
	{
		DependencyLoader loader = new DependencyLoader(goldList);
		Read.byLine(filename, loader);
		return loader.deps;
	}
	class DependencyLoader implements LineOperation
	{
		Map<Gold, Map<String, List<Dependency>>> deps = new HashMap<>();
		Map<String, List<Dependency>> byFixWord = new HashMap<>();
		int numEmpty = 0;
		private List<Gold> goldList;
		DependencyLoader(List<Gold> goldList)
		{
			this.goldList = goldList;
		}
		@Override
		public void read(String line) 
		{
			if(line.isEmpty())
			{
				deps.put(goldList.get(numEmpty), byFixWord);
				byFixWord = new HashMap<>();
				numEmpty ++;
				System.out.println(numEmpty);
			}
			else
			{
				String[] split = line.split("@");
				List<Dependency> depList = new ArrayList<>();
				for(int a = 1; a < split.length; a++)
				{
					Dependency dep = Dependency.parse(split[a]);
					depList.add(dep);
				}
				byFixWord.put(split[0], depList);
			}
		}
	}
	private void exportFeatures()
	{
		Map<Gold, FixOptions> fixOptions = (Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/corrScoring/1000fixOptions.ser");
		List<Gold> goldsList = new GoldStandard(25, 1000).getGolds();
		exportFeatures(fixOptions, goldsList, "C:/MissingWord/corrScoring/trainDepFeatures.txt");
	}
	public void exportFeatures(Map<Gold, FixOptions> fixOptions, List<Gold> goldsList, String filename)
	{
		//Map<Gold, Map<String, List<Dependency>>> deps = (Map<Gold, Map<String, List<Dependency>>>) Pickle.load("C:/MissingWord/corrScoring/mapGoldStringTypedDependency.ser");
		
		Map<Gold, Map<String, Double>> allScores = new HashMap<>();
		
		for(int a = 0; a < goldsList.size(); a++)
		{
			Map<String, List<Dependency>> deps = new DependencyCache().retrieve(a);
			Gold gold = goldsList.get(a);
			Map<String, Double> scores = new HashMap<>();
			for(String fixWord : deps.keySet())
			{
				for(Dependency dep : deps.get(fixWord))
				{
					if(!scores.containsKey(fixWord))
					{
						scores.put(fixWord, 0d);
					}
					scores.put(fixWord, scores.get(fixWord) + Math.log((double) (entireCounter.getCount(dep.getRelation(), dep.getHeadToken(), dep.getDepToken()) + 1)));
				}
			}
			allScores.put(gold, scores);
		}
		System.out.println("Scored");
		List<List<Double>> allFeatures = new ArrayList<>();
		Map<Gold, Set<Dependency>> commonDeps = getCommonDeps(goldsList);
		for(int a = 0; a < goldsList.size(); a++)
		{
			Map<String, List<Dependency>> deps = new DependencyCache().retrieve(a);
			Gold gold = goldsList.get(a);
			Map<String, Double> scores = allScores.get(gold);
			String highestWord = highestWord(scores);
			String highestToken = Utils.getToken(highestWord);

			List<Double> features = new ArrayList<>();
			//unigram feature
			features.add(Math.log(unigramCounter.getCount(highestToken)));
			//counts of half dependencies of top word
			List<Double> headCounts = new ArrayList<>();
			List<Double> depCounts = new ArrayList<>();
			List<Double> headTokenCounts = new ArrayList<>();
			List<Double> depTokenCounts = new ArrayList<>();
			for(Dependency dep : deps.get(highestWord))
			{
				if(!commonDeps.get(gold).contains(dep))
				{
					headCounts.add((double) headCounter.getCount(dep.getRelation(), dep.getHeadToken()));
					depCounts.add((double) depCounter.getCount(dep.getRelation(), dep.getDepToken()));
					headTokenCounts.add((double) unigramCounter.getCount(dep.getHeadToken()));
					depTokenCounts.add((double) unigramCounter.getCount(dep.getDepToken()));
				}
			}
			addFixedFeaturesFrom(features, headCounts, 5, true);
			addFixedFeaturesFrom(features, depCounts, 5, true);
			addFixedFeaturesFrom(features, depTokenCounts, 5, true);
			addFixedFeaturesFrom(features, headTokenCounts, 5, true);

			//num deps feature
			List<Double> numDeps = new ArrayList<>();
			for(String fixWord : deps.keySet())
			{
				numDeps.add((double) deps.get(fixWord).size());
			}
			features.add(ListUtils.mean(numDeps));
			features.add((double) deps.get(highestWord).size());
			//relation weights
			List<Double> relationWeights = new ArrayList<>();
			//tag weights of head/dep
			List<Double> headTagWeights = new ArrayList<>();
			List<Double> depTagWeights = new ArrayList<>();
			for(int b = 0; b < relationDict.keySet().size(); b++)
			{
				relationWeights.add(0d);
			}
			for(int b = 0; b < tagsDict.keySet().size(); b++)
			{
				headTagWeights.add(0d);
				depTagWeights.add(0d);
			}
			for(String fixWord : deps.keySet())
			{
				Set<Dependency> uniqueDeps = new HashSet<>(deps.get(fixWord));
				uniqueDeps.removeAll(commonDeps.get(gold));
				for(Dependency dep : uniqueDeps)
				{
					if(relationDict.contains(dep.getRelation()))
					{
						int relIndex = relationIndex.get(dep.getRelation());
						relationWeights.set(relIndex, relationWeights.get(relIndex) + Math.log(entireCounter.getCount(dep.getRelation(), dep.getHeadToken(), dep.getDepToken()) + 1));
						if(dep.getHeadTag() != null)
						{
							int headTagIndex = tagsIndex.get(dep.getHeadTag());
							headTagWeights.set(headTagIndex, headTagWeights.get(headTagIndex) + 1);
						}
						if(dep.getDepTag() != null)
						{
							int depTagIndex = tagsIndex.get(dep.getDepTag());
							depTagWeights.set(depTagIndex, depTagWeights.get(depTagIndex) + 1);
						}
					}
				}
			}
			ListUtils.normalize(relationWeights);
			ListUtils.normalize(headTagWeights);
			ListUtils.normalize(depTagWeights);
			features.addAll(relationWeights);
			features.addAll(headTagWeights);
			features.addAll(depTagWeights);
			allFeatures.add(features);
		}
		List<String> stringed = new ArrayList<>();
		for(List<Double> featureSet : allFeatures)
		{
			stringed.add(ListUtils.join(featureSet, " "));
		}
		Write.to(filename, stringed);
	}
	private Map<Gold, Set<Dependency>> getCommonDeps(List<Gold> goldsList)
	{
		Map<Gold, Set<Dependency>> commonDeps = new HashMap<>();
		for(int a = 0; a < goldsList.size(); a++)
		{
			Map<String, List<Dependency>> deps = new DependencyCache().retrieve(a);
			Set<Dependency> commons = null;
			for(List<Dependency> depList : deps.values())
			{
				if(commons == null)
				{
					commons = new HashSet<>(depList);
				}
				else
				{
					if(depList.size() >= commons.size()) //arbitrary heuristic
					{
						commons.retainAll(depList);
					}
				}
			}
			commonDeps.put(goldsList.get(a), commons);
		}
		return commonDeps;
	}
	private String highestWord(Map<String, Double> scores)
	{
		double max = Double.NEGATIVE_INFINITY;
		String maxKey = null;
		for(String key : scores.keySet())
		{
			if(scores.get(key) > max)
			{
				maxKey = key;
				max = scores.get(key);
			}
		}
		return maxKey;
	}
	private Map<String, Integer> makeIndex(List<String> list)
	{
		Map<String, Integer> relationIndex = new HashMap<>();
		for(int a = 0; a < list.size(); a++)
		{
			relationIndex.put(list.get(a), a);
		}
		return relationIndex;
	}
	public static void addFixedFeaturesFrom(List<Double> features, List<Double> toAdd, int cap, boolean sort)
	{
		if(sort)
		{
			Collections.sort(toAdd);
		}
		toAdd = toAdd.subList(0, Math.min(cap, toAdd.size()));
		fill(toAdd, cap, 0);
		features.addAll(toAdd);
	}
	public static void fill(List<Double> list, int upTo, double with)
	{
		while(list.size() < upTo)
		{
			list.add(with);
		}
	}
}
