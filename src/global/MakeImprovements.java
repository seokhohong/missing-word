package global;

import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import correct.WeightedModelsCorrector;
import main.Gold;
import mwutils.Counter;
import mwutils.TaggedSentence;
import mwutils.Utils;
import stanford.StanfordTagger;
import utils.ListUtils;
import utils.Read;
import utils.Write;
import main.GoldStandard;
import utils.ArrayUtils;

public class MakeImprovements 
{
	private StanfordTagger tagger = new StanfordTagger();
	public static void main(String[] args)
	{
		new MakeImprovements().test();
	}
	private void go()
	{
		List<String> incorrectSentences = Read.from("C:/MissingWord/incorrect.txt");
		List<Gold> allGolds = new ArrayList<>();
		List<Gold> correctGolds = new ArrayList<>();
		List<Gold> incorrectGolds = new ArrayList<>();
		for(String incorrectSentence : incorrectSentences)
		{
			int correctIndex = Integer.parseInt(incorrectSentence.substring(0, incorrectSentence.indexOf(" ")));
			incorrectSentence = incorrectSentence.substring(incorrectSentence.indexOf(" ") + 1);
			TaggedSentence tagged = tagger.tagSentence(incorrectSentence);
			for(int a = 1; a < tagged.getTokens().length; a++)
			{
				Gold gold = new Gold("", a ,tagged.toTaggedString());
				if(a == correctIndex)
				{
					correctGolds.add(gold);
				}
				else
				{
					incorrectGolds.add(gold);
				}
			}
		}
	}
	private static final Random rnd = new Random();
	private void test()
	{
		int length = 15;
		List<PairwiseModel> models = Collections.singletonList(new PCFGPairwiseModel());
		List<Gold> actualGolds = new GoldStandard(length, 10000).getGolds();
		List<ImprovementSet> improvementSets = new ArrayList<>();
		for(Gold actual : actualGolds)
		{
			String original = TaggedSentence.fromTaggedLine(actual.incorrectSentence()).toString();
			List<List<String>> sentences = new ArrayList<>();
			sentences.add(Collections.singletonList(original));
			for(int a = 0; a < length; a++)
			{
				if(a == actual.removeIndex())
				{
					sentences.add(Collections.singletonList(ArrayUtils.join(ArrayUtils.splice(Arrays.copyOf(actual.getTokens(), actual.getTokens().length), a, Utils.getToken(actual.removed())), " ")));
				}
				else
				{
					List<String> trySentences = new ArrayList<>();
					for(int b = 0; b < 10; b++)
					{
						trySentences.add(ArrayUtils.join(ArrayUtils.splice(Arrays.copyOf(actual.getTokens(), actual.getTokens().length), a, randomWord()), " "));
					}
					sentences.add(trySentences);
				}
			}
			improvementSets.add(new ImprovementSet(sentences, actual.removeIndex()));
			/*
			for(String sentence : sentences)
			{
				System.out.println(sentence);
			}
			*/
		}
		List<String> features = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		for(PairwiseModel model : models)
		{
			model.load();
			for(List<Double> featureSet : model.getFeatures(improvementSets))
			{
				features.add(ListUtils.print(featureSet));
			}
			model.unload();
		}
		for(ImprovementSet set : improvementSets)
		{
			labels.add(Integer.toString(set.getImprovementIndex()));
		}
		Write.to("C:/MissingWord/improvementFeatures.txt", features);
		Write.to("C:/MissingWord/improvementLabels.txt", labels);
	}
	private List<String> midFrequent = new ArrayList<>(Counter.load("C:/MissingWord/midFrequentWords.txt").keySet());
	private String randomWord()
	{
		return midFrequent.get(rnd.nextInt(midFrequent.size()));
	}
}
