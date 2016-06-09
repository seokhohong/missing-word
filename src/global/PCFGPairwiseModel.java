package global;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;
import mwutils.Counter;
import stanford.PCFGParser;
import stanford.SRParser;
import tree.TreeElem;
import utils.Multitasker;

public class PCFGPairwiseModel implements PairwiseModel
{
	Counter<String> parseElemCounter;
	SRParser parser;
	public static void main(String[] args)
	{
		new PCFGPairwiseModel().go();
	}
	private void go()
	{
		
		String sentence = "Others were asking about postoperative recovery and, while four patients were seeking reassurance.";
		String sentence2 = "Others were asking about postoperative recovery and person, while four patients were seeking reassurance.";
		PCFGParser parser = new PCFGParser();
		parseElemCounter = Counter.load("C:/MissingWord/parseElems.txt");
		analyze(parser.parse(sentence));
		analyze(parser.parse(sentence2));
	}
	private void analyze(Tree parse)
	{
		System.out.println(parse);
		double score = 0;
		for(TreeElem elem : TreeElem.makeListFrom(parse))
		{
			if(!elem.getNode().isLeaf() && !elem.getNode().isPreTerminal())
			{
				int count = parseElemCounter.getCount(elem.toString());
				score += Math.log(count + 1) - Math.log(parseElemCounter.size());
				System.out.println(elem + " " + count);
			}
		}
		System.out.println(score);
	}
	private double getScore(String sentence)
	{
		double score = 0;
		for(TreeElem elem : TreeElem.makeListFrom(parser.parse(sentence)))
		{
			if(!elem.getNode().isLeaf() && !elem.getNode().isPreTerminal())
			{
				int count = parseElemCounter.getCount(elem.toString());
				score += Math.log(count + 1) - Math.log(parseElemCounter.size());
			}
		}
		return score;
	}
	public void load()
	{
		parseElemCounter = Counter.load("C:/MissingWord/parseElems.txt");
		parser = new SRParser();
	}
	public void unload()
	{
		parseElemCounter = null;
		parser = null;
	}
	@Override
	public List<List<Double>> getFeatures(List<ImprovementSet> pairs) 
	{
		List<List<Double>> allFeatures = new ArrayList<>();
		List<ParseTask> tasks = new ArrayList<>();
		Multitasker multitasker = new Multitasker(7);
		for(ImprovementSet pair : pairs)
		{
			ParseTask task = new ParseTask(pair);
			tasks.add(task);
			multitasker.load(task);	
		}
		multitasker.done();
		for(ParseTask task : tasks)
		{
			List<Double> features = new ArrayList<>();
			double origScore = task.scores.get(0);
			for(int a = 1; a < task.scores.size(); a++)
			{
				features.add(task.scores.get(a) - origScore);
			}
			allFeatures.add(features);
		}
		return allFeatures;
	}
	private class ParseTask implements Runnable
	{
		ImprovementSet set;
		List<Double> scores = new ArrayList<>();
		ParseTask(ImprovementSet set)
		{
			this.set = set;
		}

		@Override
		public void run() 
		{
			for(List<String> trySet: set.getSentences())
			{
				double highestScore = Double.NEGATIVE_INFINITY;
				String bestSentence = "";
				for(String sentence : trySet)
				{
					double score = getScore(sentence);
					if(score > highestScore)
					{
						highestScore = score;
						bestSentence = sentence;
					}
				}
				scores.add(highestScore);
				//System.out.println(bestSentence);
			}
		}
	}
}
