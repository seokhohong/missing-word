package global;

import java.util.ArrayList;
import java.util.List;

import stanford.ConstituencyParser;
import stanford.PCFGParser;
import stanford.SRParser;
import tree.TreeElem;
import utils.ArrayUtils;
import utils.Multitasker;
import model.Model;
import mwutils.CompressedCounter;
import mwutils.Counter;
import mwutils.TaggedSentence;

public class PCFGSelectImprovementModel implements Model 
{

	@Override
	public List<List<float[]>> process(List<TaggedSentence> sentences) 
	{
		ConstituencyParser parser = new SRParser();
		Counter<String> parseElemCounter = Counter.load("C:/MissingWord/parseElems.txt");
		List<List<float[]>> featureList = new ArrayList<>();
		List<ParseTask> parseTasks = new ArrayList<>();
		Multitasker multitasker = new Multitasker(7);
		for(int a = 0; a < sentences.size(); a++)
		{
			TaggedSentence sentence = sentences.get(a);
			List<float[]> perIndex = new ArrayList<>();
			for(int b = 0; b < sentence.length(); b ++)
			{
				ParseTask task = new ParseTask(sentence, a, b, parser, parseElemCounter);
				multitasker.load(task);
				parseTasks.add(task);
				//perIndex.add(getFeatures(sentence, a, parser, parseElemCounter));
			}
			featureList.add(perIndex);
		}
		multitasker.done();
		for(ParseTask task : parseTasks)
		{
			featureList.get(task.sentenceIndex).add(task.features);
		}
		return featureList;
	}
	int numComplete = 0;
	class ParseTask implements Runnable
	{
		float[] features;
		TaggedSentence sentence;
		int index;
		ConstituencyParser parser;
		Counter<String> parseElemCounter;
		int sentenceIndex;
		
		public ParseTask(TaggedSentence sentence, int sentenceIndex, int index, ConstituencyParser parser, Counter<String> parseElemCounter)
		{
			this.sentence = sentence;
			this.sentenceIndex = sentenceIndex;
			this.index = index;
			this.parser = parser;
			this.parseElemCounter = parseElemCounter;
		}
		@Override
		public void run() 
		{
			features = getFeatures(sentence, index, parser, parseElemCounter);
			numComplete ++;
			if(numComplete % 1000 == 0)
			{
				System.out.println(numComplete);
			}
		}
		
	}
	public float[] getFeatures(TaggedSentence sentence, int index, ConstituencyParser parser, Counter<String> parseElemCounter)
	{
		float[] features = new float[2];
		double origScore = getScore(parser, parseElemCounter, sentence.toString());
		String[] splicedNnsChunk = ArrayUtils.splice(sentence.getChunks(), index, "people_NNS");
		double nnsSplicedScore = getScore(parser, parseElemCounter, TaggedSentence.fromTaggedLine(ArrayUtils.join(splicedNnsChunk, " ")).toString());
		features[0] = (float) (nnsSplicedScore - origScore);
		String[] splicedNnChunk = ArrayUtils.splice(sentence.getChunks(), index, "person_NN");
		double nnSplicedScore = getScore(parser, parseElemCounter, TaggedSentence.fromTaggedLine(ArrayUtils.join(splicedNnChunk, " ")).toString());
		features[1] = (float) (nnSplicedScore - origScore);
		return features;
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
}
