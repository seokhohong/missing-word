package correct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import correct.FixOptions.ScoredWord;
import mwutils.Counter;
import mwutils.TaggedSentence;
import mwutils.Utils;
import edu.stanford.nlp.parser.Parser;
import edu.stanford.nlp.trees.Tree;
import global.PCFGCache;
import stanford.ConstituencyParser;
import stanford.PCFGParser;
import stanford.SRParser;
import tree.TreeElem;
import utils.Multitasker;

public class PCFGModel implements AdvancedReplacer
{
	private Counter<String> parseElemCounter = Counter.load("C:/MissingWord/parseElems.txt");
	private int numParses = 0;
	private int totalParses = 0;
	
	private boolean reweightAll = false;
	
	public PCFGModel(boolean reweightAll)
	{
		this.reweightAll = reweightAll;
	}
	public PCFGModel() {}
	
	@Override
	public void reweight(FixOptions fixOptions, double weight) 
	{
		numParses = 0;
		totalParses = 0;
		
		Counter<String> unigrams = Counter.load("C:/MissingWord/frequentWords.txt");

		Multitasker multitasker = new Multitasker(7);
		Map<String, RunParser> parseTasks = new HashMap<>();
		ConstituencyParser parser = new PCFGParser();
		List<ScoredWord> scores = fixOptions.sortedScores();
		List<String> topWords = new ArrayList<>();
		List<ScoredWord> limit = reweightAll ? scores : scores.subList(0, Math.min(scores.size(), 20));
		for(ScoredWord scored : limit)
		{
			topWords.add(scored.getWord());
		}
		for(String fixWord : topWords)
		{ 
			RunParser runParser = new RunParser(parser, fixOptions.fill(fixWord));
			parseTasks.put(fixWord, runParser);
			multitasker.load(runParser);
		}
		multitasker.done();
		for(String fixWord : fixOptions.getFixWords())
		{
			if(topWords.contains(fixWord) || reweightAll)
			{
				fixOptions.multLogScore(fixWord, parseTasks.get(fixWord).score / Math.sqrt((unigrams.getCount(Utils.getToken(fixWord)) + 1)));
			}
			else
			{
				fixOptions.multLogScore(fixWord, -100000);
			}
			//System.out.println("PARSER "+fixEntries.get(a)+" "+parserTasks.get(a).score);
		}
		return;
	}
	private static final Random rnd = new Random();
	class RunParser implements Runnable
	{
		ConstituencyParser parser;
		private String text;
		private double score;
		RunParser(ConstituencyParser parser, String text)
		{
			this.parser = parser;
			this.text = text;
		}

		@Override
		public void run()
		{
			//score = getScore(parser, parseElemCounter, text);
			score = getRawScore(parser, text);
			numParses ++;
			if(numParses % 1000 == 0)
			{
				System.out.println(numParses + " " + totalParses);
			}
		}
		private double getScore(ConstituencyParser parser, Counter<String> parseElemCounter, String sentence)
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
		private double getRawScore(ConstituencyParser parser, String sentence)
		{
			System.out.println(parser.parse(sentence).score()+" "+sentence);
			return parser.parse(sentence).score();
		}
	}
	@Override
	public void load() 
	{
		
	}
	@Override
	public void unload() 
	{
		
	}
}
