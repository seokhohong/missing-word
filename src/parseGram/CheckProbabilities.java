package parseGram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mwutils.Counter;
import npreduce.NPReduce;
import stanford.PCFGParser;

public class CheckProbabilities 
{
	public static void main(String[] args)
	{
		new CheckProbabilities().go();
	}
	private void go()
	{
		int length = 6;
		String sentence = "Japan has suspended of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese .";
		//String sentence = "If no candidate wins an absolute majority , there will be a runoff between the top two contenders , most likely in mid-October .";
		PCFGParser parser = new PCFGParser();
		List<String> npReduce = NPReduce.foldNP(parser.parse(sentence), length);
		List<String> parseGrams = parser.lex(sentence);
		
		parser = null;
		System.gc();
		//ParseChunkCounter parseModCounter = ParseChunkCounter.fromGramList("C:/MissingWord/parseModGramsPart3.txt");
		//ParseChunkCounter parseCounter = ParseChunkCounter.fromGramList("C:/MissingWord/parseGramsPart3.txt");
		
		//ParseChunkCounter parseModCounter = ParseChunkCounter.fromGramList("C:/MissingWord/parserModTagsPart3.txt");
		//ParseChunkCounter parseCounter = ParseChunkCounter.fromGramList("C:/MissingWord/parserTagsPart3.txt");
		/*
		List<String> filenames = new ArrayList<>();
		List<String> modFilenames = new ArrayList<>();
		for(int a = 0; a < 4; a++)
		{
			filenames.add("C:/MissingWord/npReduce"+length+"Part"+a+".txt");
		}
		for(int a = 0; a < 4; a++)
		{
			modFilenames.add("C:/MissingWord/npReduceMod"+length+"Part"+a+".txt");
		}
		
		Counter<String> parseModCounter = Counter.tally(modFilenames);
		parseModCounter.export("C:/MissingWord/parseMod6.txt");
		Counter<String> parseCounter = Counter.tally(filenames);
		parseCounter.export("C:/MissingWord/parse6.txt");
		*/
		
		Counter<String> parseModCounter = Counter.load("C:/MissingWord/parseMod6.txt");
		Counter<String> parseCounter = Counter.load("C:/MissingWord/parse6.txt");
		
		for(String npGram : npReduce)
		{
			System.out.println(npGram);
			System.out.println(parseCounter.getCount(npGram)+" "+parseModCounter.getCount(npGram));
			System.out.println(parseCounter.getProb(npGram)+" "+parseModCounter.getProb(npGram));
			System.out.println((double) (parseModCounter.getProb(npGram) - parseCounter.getProb(npGram)));
		}
		
		/*
		for(String gram : parseGrams)
		{
			ParseGramWindow window = new ParseGramWindow(gram);
			window.removeWord();
			window.removeMarkovOrder(1);
			System.out.println(window);
			System.out.println(parseCounter.prob(window.toString())+" "+parseModCounter.prob(window.toString()));
			//System.out.println(parseCounter.count(window.toString()));
		
		}
		*/
	}

}
