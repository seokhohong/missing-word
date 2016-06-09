package lexbigram;

import java.util.ArrayList;
import java.util.List;

import strComp.Dictionary;
import strComp.MultiCompress;
import model.Model;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.TaggedSentence;
import mwutils.Utils;

public class TrigramRarityModel implements Model
{
	private int offset;
	public TrigramRarityModel(int offset)
	{
		this.offset = offset;
	}
	@Override
	public List<List<float[]>> process(List<TaggedSentence> sentences) 
	{
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/2000frequentWords.txt");
		Counter<String> words = Counter.load("C:/MissingWord/frequentWords.txt");
		MultiCompress mc = new MultiCompress(wordsDict, 3);
		CompressedCounter origCounter = CompressedCounterFactory.load("D:/MissingWord/ngrams/"+offset+"-2000freqTrigrams5.txt", mc);
		
		List<List<float[]>> featureList = new ArrayList<>();
		for(TaggedSentence sentence : sentences)
		{
			List<float[]> perIndex = new ArrayList<>();
			for(int a = 0; a < sentence.length(); a++)
			{
				perIndex.add(getFeatures(sentence, a, origCounter, words));
			}
			featureList.add(perIndex);
		}
		return featureList;
	}
	private float[] getFeatures(TaggedSentence sentence, int index, CompressedCounter origCounter, Counter<String> unigramCounter)
	{
		float[] features = new float[2];
		String[] elems = new String[3];
		String[] tokens = sentence.getTokens();
		double unigramRarity = 0;
		for(int b = 0; b < 3; b++)
		{
			elems[b] = Utils.getElem(tokens, index + b - offset);
			unigramRarity += Math.log(unigramCounter.getCount(elems[b]) + 1);
		}
		features[0] = (float) Math.log((double) (origCounter.getCount(elems) + 1)) - (float) unigramRarity;
		return features;
	}
}
