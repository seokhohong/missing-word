package learnModelWeights;

import global.PCFGCache;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.*;

import main.Gold;
import main.GoldStandard;
import mwutils.Counter;
import mwutils.Utils;
import tag.NTagCorrector;
import utils.ArrayUtils;
import utils.ListUtils;
import utils.Multitasker;
import utils.Pickle;
import correct.AdvancedReplacer;
import correct.BidirTagsReplacer;
import correct.CombinedTrigramModel;
import correct.FixEntry;
import correct.FixOptions;
import correct.GlobalBigramModel;
import correct.NTagReplacer;
import correct.PCFGModel;
import correct.SVOModel;
import correct.SpecialFeatures;
import correct.TrigramModel;
import correct.UnigramFilter;
import stanford.StanfordTagger;
import strComp.Dictionary;
import utils.Write;
import correct.BigramModel;
import correct.WeightedModelsCorrector.TagTask;
import dep.DependencyCache;
import dep.DependencyModel;
import utils.Read;

public class MakeFeatures 
{
	private static Counter<String> tags = Counter.load("C:/MissingWord/tag/tags.txt");
	private static Counter<String> words = Counter.load("C:/MissingWord/frequentWords.txt");
	private static List<String> tagsList = new ArrayList<>();
	static
	{
		tagsList.addAll(tags.keySet());
		Collections.sort(tagsList);
	}
	public static void main(String[] args)
	{
		List<Gold> golds = new GoldStandard(15, 300200).getGolds().subList(300000, 300200);
		new MakeFeatures().saveFixOptions(10000);
		new MakeFeatures().makeFeatures("C:/MissingWord/corrScoring/10000fixOptions.ser", 10000);
		new MakeFeatures().makeModelSpecialFeatures(10000, new GoldStandard(25, 10000).getGolds(), new DependencyCache("cache", new GoldStandard(25, 10000).getGolds()), (Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/corrScoring/10000fixOptions.ser"));
		new MakeFeatures().makeLabels(10000, new GoldStandard(25, 10000).getGolds(), new DependencyCache("cache", new GoldStandard(25, 10000).getGolds()), (Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/corrScoring/10000fixOptions.ser"));
		
		
		Pickle.dump(new MakeFeatures().makeFixOptions(golds), "C:/MissingWord/testFixOptions.ser");
	}

	public Map<Gold, FixOptions> makeFixOptions(List<Gold> golds)
	{
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		StanfordTagger tagger = new StanfordTagger();
		
		Map<Gold, TLongSet> suggestions = new NTagCorrector(4, 3).suggest(golds);
		
		List<AdvancedReplacer> initFilters = new ArrayList<>();
		initFilters.add(new UnigramFilter(1000));
		
		Map<Gold, FixOptions> fixOptions = fixOptions(golds, suggestions, wordsDict, tagger);
		
		runFilters(fixOptions, golds, initFilters);
		
		return fixOptions;
	}
	public void saveFixOptions(int numFixOptions)
	{
		List<Gold> golds = new GoldStandard(25, numFixOptions).getGolds();
		Map<Gold, FixOptions> fixOptions = makeFixOptions(golds);
		Pickle.dump(fixOptions, "C:/MissingWord/corrScoring/"+numFixOptions+"fixOptions.ser");
	}
	private Map<Gold, FixOptions> fixOptions(List<Gold> golds, Map<Gold, TLongSet> suggestions, Dictionary wordsDict, StanfordTagger tagger)
	{
		Map<Gold, FixOptions> fixTokens = new HashMap<>();
		for(Gold gold : golds)
		{
			TLongSet wordSet = suggestions.get(gold);
			if(wordSet == null)
			{
				wordSet = new TLongHashSet();
				wordSet.add(0);
			}
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
			fixTokens.put(gold, new FixOptions(gold, fixes, tagger));
		}
		return fixTokens;
	}
	private static String[] spliceWord(StanfordTagger tagger, String[] oldTokens, int index, String word)
	{
		return ArrayUtils.splice(oldTokens, index, word);
	}
	private void runFilters(Map<Gold, FixOptions> fixOptions, List<Gold> golds, List<AdvancedReplacer> initFilters)
	{
		for(AdvancedReplacer replacer : initFilters)
		{
			System.out.println("Filtering");
			replacer.load();
			for(int a = 0; a < golds.size(); a++)
			{
				System.out.println(a);
				replacer.reweight(fixOptions.get(golds.get(a)), 1);
				fixOptions.get(golds.get(a)).recalibrate();
			}
			replacer.unload();
		}		
	}
	private void makeFeatures(String filename, int numFeatures)
	{	
		List<AdvancedReplacer> initFilters = new ArrayList<>();
		initFilters.add(new UnigramFilter(1000));
		//initFilters.add(new NTagReplacer(5, 1));
		//initFilters.add(new NTagReplacer(5, 3));
		//initFilters.add(new NTagReplacer(5, 4));
		//initFilters.add(new BidirTagsReplacer(1, 1, 2));
		
		Map<Gold, FixOptions> fixOptions = (Map<Gold, FixOptions>) Pickle.load(filename);
		List<Gold> goldList = new GoldStandard(25, numFeatures).getGolds();

		List<String> allFeatures = new ArrayList<>();
		for(AdvancedReplacer replacer : initFilters)
		{
			System.out.println("Model");
			replacer.load();
			for(int a = 0; a < goldList.size(); a++)
			{
				System.out.println(a);
				replacer.reweight(fixOptions.get(goldList.get(a)), 1);
				fixOptions.get(goldList.get(a)).recalibrate();
			}
			replacer.unload();
		}
		for(int a = 0; a < goldList.size(); a++)
		{
			allFeatures.add(ListUtils.print(fixOptions.get(goldList.get(a)).makeFeatures(tagsList, words)));
		}
		Write.to("C:/MissingWord/corrScoring/"+numFeatures+"features.txt", allFeatures);
	}
	private void makeModelSpecialFeatures(int numFeatures, List<Gold> golds, DependencyCache cache, Map<Gold, FixOptions> fixOptions)
	{
		Map<String, AdvancedReplacer> models = new HashMap<>();
		
		models.put("cop", new DependencyModel(Arrays.asList(new String[] { "cop" }), cache));
		models.put("det", new DependencyModel(Arrays.asList(new String[] { "det" }), cache));
		models.put("conj", new DependencyModel(Arrays.asList(new String[] { "conj" }), cache));
		models.put("nn", new DependencyModel(Arrays.asList(new String[] { "nn" }), cache));
		models.put("cc", new DependencyModel(Arrays.asList(new String[] { "cc" }), cache));
		models.put("prep_", new DependencyModel(Arrays.asList(new String[] { "prep_" }), cache));
		models.put("prep", new DependencyModel(Arrays.asList(new String[] { "prep" }), true, cache));
		models.put("pobj", new DependencyModel(Arrays.asList(new String[] { "pobj" }), true, cache));
		models.put("dobj", new DependencyModel(Arrays.asList(new String[] {"dobj"}), cache));
		models.put("nsubj", new DependencyModel(Arrays.asList(new String[] {"nsubj"}), cache));
		
		models.put("svo", new SVOModel(cache));
		
		models.put("bigram-1", new BigramModel(-1, false));
		models.put("bigram0", new BigramModel(0, false));
		models.put("trigram-2", new TrigramModel(-2));
		models.put("trigram-1", new TrigramModel(-1));
		models.put("trigram0", new TrigramModel(0));
		models.put("trigram", new CombinedTrigramModel());
		
		models.put("bigram0Lower", new BigramModel(0, true));
		models.put("bigram-1Lower", new BigramModel(-1, true));
		models.put("ntag51", new NTagReplacer(5, 1));
		models.put("ntag54", new NTagReplacer(5, 4));
		models.put("ntag53", new NTagReplacer(5, 3));
		models.put("bidir112", new BidirTagsReplacer(1, 1, 2));
		
		for(String modelName : models.keySet())
		{
			System.out.println(modelName);
			AdvancedReplacer model = models.get(modelName);
			model.load();
			SpecialFeatures.exportSpecialFeatures("C:/MissingWord/corrScoring/"+modelName+"trainSpecialFeatures.txt", golds, models.get(modelName), fixOptions);
			model.unload();
		}
	}
	private void makeLabels(int numLabels, List<Gold> golds, DependencyCache cache, Map<Gold, FixOptions> fixOptions)
	{
		Map<String, AdvancedReplacer> models = new HashMap<>();
		models.put("cop", new DependencyModel(Arrays.asList(new String[] { "cop" }), cache));
		models.put("det", new DependencyModel(Arrays.asList(new String[] { "det" }), cache));
		models.put("conj", new DependencyModel(Arrays.asList(new String[] { "conj" }), cache));
		models.put("nn", new DependencyModel(Arrays.asList(new String[] { "nn" }), cache));
		models.put("cc", new DependencyModel(Arrays.asList(new String[] { "cc" }), cache));
		models.put("prep_", new DependencyModel(Arrays.asList(new String[] { "prep_" }), cache));
		models.put("prep", new DependencyModel(Arrays.asList(new String[] { "prep" }), true, cache));
		models.put("pobj", new DependencyModel(Arrays.asList(new String[] { "pobj" }), true, cache));
		models.put("dobj", new DependencyModel(Arrays.asList(new String[] {"dobj"}), cache));
		models.put("nsubj", new DependencyModel(Arrays.asList(new String[] {"nsubj"}), cache));
		
		models.put("svo", new SVOModel(cache));
		
		models.put("bigram-1", new BigramModel(-1, false));
		models.put("bigram0", new BigramModel(0, false));
		models.put("trigram-2", new TrigramModel(-2));
		models.put("trigram-1", new TrigramModel(-1));
		models.put("trigram0", new TrigramModel(0));
		models.put("trigram", new CombinedTrigramModel());
		
		models.put("bigram0Lower", new BigramModel(0, true));
		models.put("bigram-1Lower", new BigramModel(-1, true));
		models.put("ntag51", new NTagReplacer(5, 1));
		models.put("ntag54", new NTagReplacer(5, 4));
		models.put("ntag53", new NTagReplacer(5, 3));
		models.put("bidir112", new BidirTagsReplacer(1, 1, 2));
		//models.put("globalbigram", new GlobalBigramModel());
		//models.put("pcfg", new PCFGModel());
		
		for(String key : models.keySet())
		{
			System.out.println(key);
			
			models.get(key).load();
			List<String> labels = new ArrayList<>();
			for(int a = 0; a < golds.size(); a++)
			{
				models.get(key).reweight(fixOptions.get(golds.get(a)), 1);
				fixOptions.get(golds.get(a)).recalibrate();
				labels.add(Double.toString(getSmoothLabel(fixOptions.get(golds.get(a)))));
			}
			models.get(key).unload();
			Write.to("C:/MissingWord/corrScoring/"+key+"Labels.txt", labels);
		}
	}

	private double getSingleLabel(FixOptions fixOptions)
	{
		return fixOptions.scoreOf(fixOptions.getGold().removedToken());
	}
	public static double getSmoothLabel(FixOptions fixOptions)
	{
		Map<String, Double> map = new HashMap<>();
		for(String word : fixOptions.getFixWords())
		{
			map.put(Utils.getToken(word), fixOptions.getScore(word));
		}
		return AccuracyMetric.of(map, fixOptions.getGold().removedToken());
	}
}

