package global;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import main.Gold;
import main.GoldStandard;
import mwutils.TaggedSentence;
import stanford.ConstituencyParser;
import stanford.SRParser;
import stanford.StanfordTagger;
import strComp.Dictionary;
import tag.NTagCorrector;
import tree.ReconstructTree;
import utils.ArrayUtils;
import utils.ListUtils;
import utils.Multitasker;
import utils.Read;
import utils.Write;
import correct.FixOptions;
import correct.WeightedModelsCorrector.TagTask;
import dep.DepParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import gnu.trove.set.TLongSet;

public class PCFGCache 
{
	private String cacheName;
	private List<Gold> golds;						public List<Gold> getGolds() { return golds; }
	public static void main(String[] args)
	{
		/*
		DependencyCache cache = new DependencyCache("cache");
		cache.make(new GoldStandard(25, 200).getGolds());
		*/
		/*
		List<Gold> golds = new GoldStandard(25, 1000).getGolds();
		PCFGCache trainCache = new PCFGCache("trainPCFG", golds);
		trainCache.make(golds);
		*/
		List<Gold> golds = new GoldStandard(25, 1000).getGolds();
		PCFGCache trainCache = new PCFGCache("trainPCFG", golds);
		for(int a = 0; a < 200; a++)
		{
			trainCache.retrieve(golds.get(a));
		}
		/*
		List<Gold> golds = new GoldStandard(15, 300200).getGolds();
		PCFGCache testCache = new PCFGCache("15testPCFG", golds);
		System.out.println(golds.get(300000));
		testCache.make(golds.subList(300000, 300200));
		*/
		
	}
	public PCFGCache(String cacheName, List<Gold> golds)
	{
		this.cacheName = cacheName;
		this.golds = golds;
	}
	
	private void make(List<Gold> golds)
	{
		Map<Gold, TLongSet> suggestions = new NTagCorrector(4, 3).suggest(golds);
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		StanfordTagger tagger = new StanfordTagger();
		ConstituencyParser parser = new SRParser();
		for(Gold gold : suggestions.keySet())
		{
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
	private static String[] spliceWord(StanfordTagger tagger, String[] oldTokens, int index, String word)
	{
		return ArrayUtils.splice(oldTokens, index, word);
	}
	private void store(int index, FixOptions fixOptions, ConstituencyParser parser)
	{
		List<String> output = new ArrayList<>();
		Map<String, Tree> parses = new HashMap<>();
		Map<String, RunParser> parseTasks = new HashMap<>();
		Multitasker multitasker = new Multitasker(7);
		
		for(String fixWord : fixOptions.getFixWords())
		{
			RunParser parseTask = new RunParser(TaggedSentence.fromTaggedLine(fixOptions.fill(fixWord)).toString(), parser);
			multitasker.load(parseTask);
			parseTasks.put(fixWord, parseTask);
		}
		multitasker.done();
		for(String fixWord : parseTasks.keySet())
		{
			parses.put(fixWord, parseTasks.get(fixWord).tree);
		}
		for(String fixWord : fixOptions.getFixWords())
		{
			Tree tree = parses.get(fixWord);
			StringBuilder builder = new StringBuilder();
			builder.append(fixWord);
			builder.append("@");
			builder.append(tree.toString());
			output.add(builder.toString());
		}
		Write.to("D:/MissingWord/cache/"+cacheName+"/"+index+".txt", output);
	}
	private static final Random rnd = new Random();
	public static class RunParser implements Runnable
	{
		Tree tree;							public Tree getTree() { return tree; }
		String toParse;
		ConstituencyParser parser;
		public RunParser(String toParse, ConstituencyParser parser)
		{
			this.toParse = toParse;
			this.parser = parser;
		}
		@Override
		public void run() 
		{
			tree = parser.parse(toParse);
			if(rnd.nextInt(1000) == 0)
			{
				System.out.println("Parsed "+toParse);
			}
		}
	}
	private Map<Integer, Map<String, Tree>> cache = new HashMap<>();
	private Multitasker multitasker = new Multitasker(6);
	public Map<String, Tree> retrieve(Gold gold)
	{
		int index = golds.indexOf(gold);
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
					multitasker.load(new FetchParse(index + a));
				}
			}
		}
		while(true)
		{
			if(cache.containsKey(index) && cache.get(index) != null)
			{
				System.out.println("Recalled Parse "+index);
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
	class FetchParse extends Thread
	{
		private int index;
		FetchParse(int index)
		{
			this.index = index;
		}
		public void run()
		{
			String filename = "D:/MissingWord/cache/"+cacheName+"/"+index+".txt";
			if(new File(filename).exists())
			{
				List<String> lines = Read.from(filename);
				Map<String, Tree> parses = new HashMap<>();
				for(String line : lines)
				{
					String[] parts = line.split("@");
					try
					{
						parses.put(parts[0], ReconstructTree.reconstruct(parts[1]));
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
				}
				synchronized(cache)
				{
					cache.put(index, parses);
				}
			}
			else
			{
				System.out.println(filename+" does not exist!");
			}
		}
	}
}