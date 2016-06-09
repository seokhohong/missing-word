package global;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dep.DepParser;
import dep.Dependency;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.TaggedSentence;
import stanford.StanfordTagger;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.ArrayUtils;
import utils.ListUtils;
import utils.Read;
import utils.Write;

public class DependencyFeatures 
{
	Dictionary relationDict = Dictionary.fromCounterFile("C:/MissingWord/dep/depRelationCounter.txt");
	Dictionary unigramsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
	MultiCompress entireMc = new MultiCompress(relationDict, unigramsDict, unigramsDict);
	
	private MultiCompress partMc = new MultiCompress(relationDict, unigramsDict);
	private CompressedCounter relnDep = CompressedCounterFactory.load("C:/MissingWord/dep/relnDep.txt", partMc);
	private CompressedCounter relnHead = CompressedCounterFactory.load("C:/MissingWord/dep/relnHead.txt", partMc);
	
	private CompressedCounter entireCounter = CompressedCounterFactory.load("C:/MissingWord/dep/depUncollapsed.txt", entireMc);
	private Counter<String> unigramCounter = Counter.load("C:/MissingWord/frequentWords.txt");
	private static final int NUM_FEATURES = 50;
	private DepParser parser = new DepParser();
	public static void main(String[] args)
	{
		new DependencyFeatures().test();
	}
	private void test()
	{
		List<String> sentences = new ArrayList<>();
		sentences.add("They make their way and down the river.");
		sentences.add("They make their way up and down the river.");
		sentences.add("This is supposed to be experienced candidate who can answer a call at 3 AM?");
		sentences.add("This is supposed to be an experienced candidate who can answer a call at 3 AM?");
		List<TaggedSentence> taggedSentences = new ArrayList<>();
		StanfordTagger tagger = new StanfordTagger();
		for(String sentence : sentences)
		{
			taggedSentences.add(tagger.tagSentence(sentence));
		}
		List<List<Dependency>> deps = fromTagged(taggedSentences);
		List<String> features = new ArrayList<>();
		for(List<Dependency> depList : deps)
		{
			features.add(ListUtils.join(makeFeatures(depList), ","));
		}
		Write.to("C:/MissingWord/testFeatures.txt", features);
	}
	private void go()
	{
		
		List<List<Dependency>> incorrectDependencies = getDependencies("C:/MissingWord/incorrect.txt", 10000);
		
		List<String> incorrectFeatures = new ArrayList<>();
		for(List<Dependency> incorrectDepList : incorrectDependencies)
		{
			incorrectFeatures.add(ListUtils.join(makeFeatures(incorrectDepList), ","));
		}
		Write.to("C:/MissingWord/incorrectFeatures.txt", incorrectFeatures);
		
		
		List<List<Dependency>> correctDependencies = getDependencies("C:/MissingWord/train/taggedPart2.txt", 10000);
		List<String> correctFeatures = new ArrayList<>();
		for(List<Dependency> correctDepList : correctDependencies)
		{
			correctFeatures.add(ListUtils.join(makeFeatures(correctDepList), ","));
		}
		Write.to("C:/MissingWord/correctFeatures.txt", correctFeatures);
		
	}
	private List<List<Dependency>> fromTagged(List<TaggedSentence> incorrectSentences)
	{
		List<List<Dependency>> incorrectDependencies = new ArrayList<>();
		for(TaggedSentence incorrectSentence : incorrectSentences)
		{
			List<Dependency> deps = new ArrayList<>();
			GrammaticalStructure gs = parser.parse(ArrayUtils.join(incorrectSentence.getTokens(), " "));
			for(TypedDependency td : gs.typedDependencies())
			{
				deps.add(Dependency.getInstance(td));
			}
			incorrectDependencies.add(deps);
		}
		return incorrectDependencies;
	}
	private List<List<Dependency>> getDependencies(String filename, int num)
	{
		List<String> incorrect = Read.from(filename, num);
		List<TaggedSentence> incorrectSentences = new ArrayList<>();
		for(String incorr : incorrect)
		{
			incorrectSentences.add(TaggedSentence.fromTaggedLine(incorr));
		}
		

		return fromTagged(incorrectSentences);
	}
	private List<Double> makeFeatures(List<Dependency> deps)
	{
		List<DependencyLikelihood> likelihood = new ArrayList<>();
		for(Dependency dep : deps)
		{
			double logFreq = entireCounter.getCount(dep.getRelation(), dep.getHeadToken(), dep.getDepToken());
			likelihood.add(new DependencyLikelihood(
					logFreq, 
					Math.log(relnHead.getCount(dep.getRelation(), dep.getHeadToken()) + 1),
					Math.log(relnDep.getCount(dep.getRelation(), dep.getDepToken()) + 1),
					Math.log(unigramCounter.getCount(dep.getHeadToken()) + 1), 
					Math.log(unigramCounter.getCount(dep.getDepToken()) + 1)));
			System.out.println(dep.getRelation()+" "+dep.getHeadToken()+" "+dep.getDepToken()+" "+logFreq);
		}
		Collections.sort(likelihood);
		List<Double> features = new ArrayList<>();
		for(int a = 0; a < Math.min(likelihood.size(), 10); a++)
		{
			features.add(likelihood.get(a).logFreq);
			features.add(likelihood.get(a).relnHead);
			features.add(likelihood.get(a).relnDep);
			features.add(likelihood.get(a).logWord1);
			features.add(likelihood.get(a).logWord2);
		}
		//features.add(Math.log(deps.size()));
		
		while(features.size() < NUM_FEATURES)
		{
			features.add(0d);
		}
		
		return features;
	}
	class DependencyLikelihood implements Comparable<DependencyLikelihood>
	{
		private double logFreq;
		private double relnHead;
		private double relnDep;
		private double logWord1;
		private double logWord2;
		DependencyLikelihood(double logFreq, double relnHead, double relnDep, double logWord1, double logWord2)
		{
			this.logFreq = logFreq;
			this.relnHead = relnHead;
			this.relnDep = relnDep;
			this.logWord1 = logWord1;
			this.logWord2 = logWord2;
		}
		@Override
		public int compareTo(DependencyLikelihood o) 
		{
			return Double.compare(logFreq - logWord1 - logWord2, o.logFreq - o.logWord1 - o.logWord2);
		}
		
	}
}
