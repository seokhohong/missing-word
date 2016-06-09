package dep;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import main.Gold;
import main.GoldStandard;
import mwutils.TaggedSentence;
import stanford.StanfordTagger;
import strComp.Dictionary;
import tag.NTagCorrector;
import utils.ArrayUtils;
import utils.ListUtils;
import utils.Multitasker;
import utils.Read;
import utils.Write;
import correct.FixOptions;
import correct.WeightedModelsCorrector.TagTask;
import dep.DependencyModel.RunDep;
import edu.stanford.nlp.trees.TypedDependency;
import gnu.trove.set.TLongSet;

public class DependencyCache 
{
	private String cacheName;
	private List<Gold> golds;						public List<Gold> getGolds() { return golds; }
	public static void main(String[] args)
	{
		
		DependencyCache cache = new DependencyCache("cache", new GoldStandard(25, 10000).getGolds());
		cache.make(254);
		
		/*
		List<Gold> golds = new GoldStandard(15, 300200).getGolds();
		DependencyCache testCache = new DependencyCache("15test", golds);
		System.out.println(golds.get(300000));
		testCache.make(golds.subList(300000, 300200));
		*/
		/*
		List<Gold> golds = new GoldStandard(15, 300200).getGolds();
		DependencyCache testCache = new DependencyCache("15test", golds);
		for(int a = 0; a < 100; a++)
		{
			testCache.retrieve(golds.get(a));
		}
		*/
	}
	public DependencyCache(String cacheName, List<Gold> golds)
	{
		this.cacheName = cacheName;
		this.golds = golds;
	}
	
	private void make()
	{
		Map<Gold, TLongSet> suggestions = new NTagCorrector(4, 3).suggest(golds);
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		StanfordTagger tagger = new StanfordTagger();
		DepParser parser = new DepParser();
		for(Gold gold : suggestions.keySet())
		{
			if(new File(filename(golds.indexOf(gold))).exists())
			{
				continue;
			}
			TLongSet wordSet = suggestions.get(gold);
			List<String> fixes = new ArrayList<>();
			Multitasker multitasker = new Multitasker(7);
			for(long key : wordSet.toArray())
			{
				String word = wordsDict.decode((int) key);
				if(word != null)
				{
					String[] newTokens = spliceWord(tagger, gold.getTokens(), gold.removeIndex(), word);
					multitasker.load(new TagTask(newTokens, tagger, gold, fixes));
				}
			}
			multitasker.done();
			store(golds.indexOf(gold), new FixOptions(gold, fixes, tagger), parser);
		}
	}
	private void make(int index)
	{
		Map<Gold, TLongSet> suggestions = new NTagCorrector(4, 3).suggest(golds);
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		StanfordTagger tagger = new StanfordTagger();
		DepParser parser = new DepParser();
		Gold gold = golds.get(index);
		if(new File(filename(golds.indexOf(gold))).exists())
		{
			return;
		}
		TLongSet wordSet = suggestions.get(gold);
		List<String> fixes = new ArrayList<>();
		Multitasker multitasker = new Multitasker(7);
		for(long key : wordSet.toArray())
		{
			String word = wordsDict.decode((int) key);
			if(word != null)
			{
				String[] newTokens = spliceWord(tagger, gold.getTokens(), gold.removeIndex(), word);
				multitasker.load(new TagTask(newTokens, tagger, gold, fixes));
			}
		}
		multitasker.done();
		store(golds.indexOf(gold), new FixOptions(gold, fixes, tagger), parser);
	}
	private static String[] spliceWord(StanfordTagger tagger, String[] oldTokens, int index, String word)
	{
		return ArrayUtils.splice(oldTokens, index, word);
	}
	private void store(int index, FixOptions fixOptions, DepParser parser)
	{
		List<String> output = new ArrayList<>();
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
		for(String fixWord : fixOptions.getFixWords())
		{
			List<Dependency> regDep = new ArrayList<>();
			for(TypedDependency td : deps.get(fixWord))
			{
				Dependency dep = Dependency.getInstance(td);
				regDep.add(dep);
			}
			StringBuilder builder = new StringBuilder();
			builder.append(fixWord);
			builder.append("@");
			builder.append(ListUtils.join(regDep, "@"));
			output.add(builder.toString());
		}
		Write.to("D:/MissingWord/cache/"+cacheName+"/"+index+".txt", output);
	}
	private Map<Integer, Map<String, List<Dependency>>> cache = new HashMap<>();
	private Multitasker multitasker = new Multitasker(6);
	public Map<String, List<Dependency>> retrieve(Gold gold)
	{
		int index = golds.indexOf(gold);
		if(index == -1)
		{
			System.out.println("Incompatible Golds");
			System.exit(1);
		}
		if(!new File(filename(index)).exists())
		{
			return new HashMap<>();
		}
		synchronized(cache)
		{
			for(Integer i : new HashSet<>(cache.keySet()))
			{
				if(index > i + 10)
				{
					cache.remove(i);
				}
			}
			for(int a = 0; a < 20; a++) //lookahead
			{
				if(!cache.keySet().contains(index + a))
				{
					cache.put(index + a, null);
					multitasker.load(new FetchDep(index + a));
				}
			}
		}
		while(true)
		{
			if(cache.containsKey(index) && cache.get(index) != null)
			{
				System.out.println("Recalled Dep "+index);
				return cache.get(index);
			}
		}
	}
	public void done()
	{
		multitasker.done();
		cache.clear();
		multitasker = new Multitasker(6);
	}
	public String filename(int index)
	{
		return "D:/MissingWord/cache/"+cacheName+"/"+index+".txt";
	}
	class FetchDep extends Thread
	{
		private int index;
		FetchDep(int index)
		{
			this.index = index;
		}
		public void run()
		{
			Map<String, List<Dependency>> deps = new HashMap<>();
			String filename = filename(index);
			if(new File(filename).exists())
			{
				List<String> lines = Read.from(filename);
				for(String line : lines)
				{
					String[] parts = line.split("@");
					List<Dependency> depList = new ArrayList<>();
					for(int a = 1 ; a < parts.length; a++)
					{
						Dependency dep = Dependency.parse(parts[a]);
						if(dep != null)
						{
							depList.add(dep);
						}
					}
					deps.put(parts[0], depList);
				}
				synchronized(cache)
				{
					cache.put(index, deps);
				}
			}
			else
			{
				System.out.println(filename+" does not exist!");
			}
		}
	}
}
