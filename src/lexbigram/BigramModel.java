package lexbigram;

import java.util.ArrayList;
import java.util.List;

import strComp.Dictionary;
import strComp.MultiCompress;
import model.Model;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.TaggedSentence;
import mwutils.Utils;

public class BigramModel implements Model
{
	@Override
	public List<List<float[]>> process(List<TaggedSentence> sentences) 
	{
		Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		MultiCompress mc = new MultiCompress(wordsDict, wordsDict);
		CompressedCounter origCounter = CompressedCounterFactory.load("D:/MissingWord/ngrams/freqBigrams.txt", mc);
		CompressedCounter modCounter = CompressedCounterFactory.load("D:/MissingWord/ngrams/freqBigramsMod.txt", mc);
		
		List<List<float[]>> featureList = new ArrayList<>();
		for(TaggedSentence sentence : sentences)
		{
			List<float[]> perIndex = new ArrayList<>();
			for(int a = 0; a < sentence.length(); a++)
			{
				perIndex.add(getFeatures(sentence, a, origCounter, modCounter));
			}
			featureList.add(perIndex);
		}
		return featureList;
	}
	private float[] getFeatures(TaggedSentence sentence, int index, CompressedCounter origCounter, CompressedCounter modCounter)
	{
		float[] features = new float[2];
		String[] elems = new String[2];
		elems[0] = Utils.getElem(sentence.getTokens(), index - 1);
		elems[1] = Utils.getElem(sentence.getTokens(), index);
		features[0] = (float) Math.log((double) (origCounter.getCount(elems) + modCounter.getCount(elems) + 1) / (origCounter.getCount(elems) + 1));
		features[1] = (float) Math.log(origCounter.getCount(elems) + modCounter.getCount(elems) + 1);
		return features;
	}
}
