package correct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import correct.SmallModelTest.WordScore;
import dep.DependencyModel;
import stanford.StanfordTagger;
import strComp.Dictionary;
import utils.ArrayUtils;
import utils.ListUtils;
import utils.Multitasker;
import utils.Pickle;
import utils.Read;
import utils.Write;
import main.Gold;
import main.GoldStandard;
import mwutils.Counter;
import mwutils.TaggedSentence;
import learnModelWeights.MakeFeatures;
import dep.DependencyCache;

public class WeightedModelsCorrector 
{
	Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
	StanfordTagger tagger = new StanfordTagger();
	private static Counter<String> wordsCounter = Counter.load("C:/MissingWord/frequentWords.txt");
	private static Counter<String> tags = Counter.load("C:/MissingWord/tag/tags.txt");
	private static List<String> tagsList = new ArrayList<>();
	Map<String, AdvancedReplacer> models = new HashMap<>();
	List<String> modelsList = Read.from("C:/MissingWord/corrScoring/modelsList.txt");
	DependencyCache cache;
	private static final double WEIGHT_CONST = 20;
	static
	{
		tagsList.addAll(tags.keySet());
		Collections.sort(tagsList);
	}
	public static void main(String[] args)
	{
		List<Gold> cacheGold = new GoldStandard(15, 300200).getGolds().subList(300000, 300200);
		List<Gold> golds = cacheGold.subList(13, 14);
		new WeightedModelsCorrector(golds, new DependencyCache("15test", cacheGold)).go();
	}
	private List<Gold> golds;
	public WeightedModelsCorrector(List<Gold> golds, DependencyCache cache)
	{
		this.golds = golds;
		this.cache = cache;
		loadModels();
	}
	private void loadModels()
	{
		models.put("trigram0", new TrigramModel(0));
		models.put("trigram-1", new TrigramModel(-1));
		models.put("trigram-2", new TrigramModel(-2));
		models.put("trigram", new CombinedTrigramModel());
		models.put("bigram0Lower", new BigramModel(0, false));
		models.put("bigram-1Lower", new BigramModel(-1, false));
		models.put("bigram0Lower", new BigramModel(0, true));
		models.put("bigram-1Lower", new BigramModel(-1, true));
		models.put("cc", new DependencyModel(Arrays.asList(new String[] {"cc"}), cache));
		models.put("prep_", new DependencyModel(Arrays.asList(new String[] {"prep_"}), cache));
		models.put("conj", new DependencyModel(Arrays.asList(new String[] {"conj"}), cache));
		models.put("dobj", new DependencyModel(Arrays.asList(new String[] {"dobj"}), cache));
		models.put("nsubj", new DependencyModel(Arrays.asList(new String[] {"nsubj"}), cache));
		models.put("nn", new DependencyModel(Arrays.asList(new String[] {"nn"}), cache));
		models.put("det", new DependencyModel(Arrays.asList(new String[] {"det"}), cache));
		models.put("cop", new DependencyModel(Arrays.asList(new String[] {"cop"}), cache));
		models.put("prep", new DependencyModel(Arrays.asList(new String[] {"prep"}), true, cache));
		models.put("pobj", new DependencyModel(Arrays.asList(new String[] {"pobj"}), true, cache));
		models.put("svo", new SVOModel(cache));
		models.put("ntag51", new NTagReplacer(5, 1));
		models.put("ntag53", new NTagReplacer(5, 3));
		models.put("ntag54", new NTagReplacer(5, 4));
		models.put("bidir112", new BidirTagsReplacer(1, 1, 2));
	}
	private void go()
	{
		
		//Map<Gold, FixOptions> fixOptions = (Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/corrScoring/1000fixOptions.ser");
		Map<Gold, FixOptions> fixOptions = (Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/testFixOptions.ser");
		/*
		for(Gold gold : fixOptions.keySet())
		{
			System.out.println(gold);
		}
		for(Gold gold : golds)
		{
			System.out.println(gold);
		}
		*/
		exportFeatures(fixOptions);
		
		for(String modelName : models.keySet())
		{
			System.out.println(modelName);
			AdvancedReplacer model = models.get(modelName);
			model.load();
			SpecialFeatures.exportSpecialFeatures("C:/MissingWord/corrScoring/"+modelName+"QuerySpecialFeatures.txt", golds, models.get(modelName), fixOptions);
			model.unload();
		}
		
		for(FixOptions fixOption : fixOptions.values())
		{
			fixOption.resetWeights();
		}
		
		List<List<Double>> weights = ModelWeighting.getWeights();
		Map<Gold, Integer> goldIndices = getGoldIndices();
		for(String modelName : modelsList)
		{
			System.out.println("Weighted Model "+modelName);
			AdvancedReplacer model = models.get(modelName);
			model.load();
			for(Gold gold : golds)
			{
				model.reweight(fixOptions.get(gold), Math.exp(weights.get(goldIndices.get(gold)).get(modelsList.indexOf(modelName))) * WEIGHT_CONST);
				fixOptions.get(gold).recalibrate();
			}
			List<WordScore> scores = new ArrayList<>();
			for(String fixWord : fixOptions.get(golds.get(0)).getFixWords())
			{
				scores.add(new WordScore(fixWord, fixOptions.get(golds.get(0)).getScore(fixWord)));
			}
			Collections.sort(scores);
			for(WordScore score : scores)
			{
				System.out.println(score.getWord()+" "+score.getScore());
			}
			model.unload();
		}
		PCFGModel model = new PCFGModel();
		for(Gold gold : golds)
		{
			model.reweight(fixOptions.get(gold), 1);
		}
		Multitasker multitasker = new Multitasker(7);
		List<EvaluateGuess> evaluations = new ArrayList<>();
		for(int a = 0; a < golds.size(); a++)
		{
			EvaluateGuess eg = new EvaluateGuess(fixOptions.get(golds.get(a)));
			evaluations.add(eg);
			multitasker.load(eg);
		}
		multitasker.done();
		
		double sum = 0;
		for(EvaluateGuess eg : evaluations)
		{
			sum += eg.result;
		}
		System.out.println(sum / golds.size());
		
		evaluateGuessedOnly(evaluations);
	}
	private void evaluateGuessedOnly(List<EvaluateGuess> egs)
	{
		List<String> guesses = Read.from("C:/MissingWord/export.txt").subList(0, egs.size());
		double sum = 0;
		int num = 0;
		for(int a = 0; a < guesses.size(); a++)
		{
			if(!guesses.get(a).startsWith("0"))
			{
				sum += egs.get(a).result;
				num ++;
			}
		}
		System.out.println(sum / num);
	}
	public Map<Gold, String> suggestFix(List<Gold> golds)
	{
		Map<Gold, FixOptions> fixOptions = new MakeFeatures().makeFixOptions(golds);
		exportFeatures(fixOptions);
		for(FixOptions fixOption : fixOptions.values())
		{
			fixOption.resetWeights();
		}
		
		List<List<Double>> weights = ModelWeighting.getWeights();
		Map<Gold, Integer> goldIndices = getGoldIndices();
		for(String modelName : modelsList)
		{
			System.out.println("Weighted Model "+modelName);
			AdvancedReplacer model = models.get(modelName);
			model.load();
			for(Gold gold : golds)
			{
				model.reweight(fixOptions.get(gold), Math.exp(weights.get(goldIndices.get(gold)).get(modelsList.indexOf(modelName))) * WEIGHT_CONST);
				fixOptions.get(gold).recalibrate();
				if(modelName.equals(modelsList.get(modelsList.size() - 1)))
				{
					for(String fixWord : fixOptions.get(gold).getFixWords())
					{
						System.out.println(fixWord+" "+fixOptions.get(gold).getScore(fixWord));
					}
				}
			}
			model.unload();
		}
		Map<Gold, String> bestFix = new HashMap<>();
		for(Gold gold : fixOptions.keySet())
		{
			bestFix.put(gold, fixOptions.get(gold).bestWord());
		}
		
		return bestFix;
	}
	private class EvaluateGuess implements Runnable
	{
		private FixOptions fixOptions;
		private double result;
		EvaluateGuess(FixOptions fixOptions)
		{
			this.fixOptions = fixOptions;
		}
		public void run()
		{
			result = fixOptions.evaluateGuess(golds);
		}
	}

	private double softmax(List<Double> weights, int index)
	{
		double sum = 0;
		for(double weight : weights)
		{
			sum += Math.exp(weight);
		}
		return Math.exp(weights.get(index)) / sum;
	}
	private Map<Gold, Integer> getGoldIndices()
	{
		Map<Gold, Integer> goldIndices = new HashMap<>();
		for(int a = 0; a < golds.size(); a++)
		{
			goldIndices.put(golds.get(a), a);
		}
		return goldIndices;
	}

	private static Random rnd = new Random();
	public static class TagTask implements Runnable
	{
		private String[] tokens;
		private StanfordTagger tagger;
		private Gold gold;
		private List<String> taggedWord;
		public TagTask(String[] tokens, StanfordTagger tagger, Gold gold, List<String> taggedWord)
		{
			this.tokens = tokens;
			this.tagger = tagger;
			this.gold = gold;
			this.taggedWord = taggedWord;
		}
		@Override
		public void run() 
		{
			TaggedSentence taggedSentence = tagger.tagSentence(ListUtils.join(Arrays.asList(tokens), " "));
			if(rnd.nextInt(1000) == 0)
			{
				System.out.println("Tagged "+ArrayUtils.join(tokens, " "));
			}
			synchronized(taggedWord)
			{
				taggedWord.add(taggedSentence.getToken(gold.removeIndex())+"_"+taggedSentence.getTag(gold.removeIndex()));
			}
		}
	}

	private void exportFeatures(Map<Gold, FixOptions> fixOptions)
	{
		List<String> allFeatures = new ArrayList<>();
		for(int a = 0; a < golds.size(); a++)
		{
			allFeatures.add(ListUtils.print(fixOptions.get(golds.get(a)).makeFeatures(tagsList, wordsCounter)));
		}
		Write.to("C:/MissingWord/corrScoring/queryFeatures.txt", allFeatures);
	}
}
