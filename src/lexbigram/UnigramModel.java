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

/** Not exactly a model, but returns the useful log frequency of a given unigram */
public class UnigramModel implements Model
{
	@Override
	public List<List<float[]>> process(List<TaggedSentence> sentences) 
	{
		Dictionary wordDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		CompressedCounter counter = CompressedCounterFactory.load("C:/MissingWord/unigramCounter.txt", new MultiCompress(wordDict));
		
		List<List<float[]>> featureList = new ArrayList<>();
		for(TaggedSentence sentence : sentences)
		{
			List<float[]> perIndex = new ArrayList<>();
			for(int a = 0; a < sentence.length(); a++)
			{
				perIndex.add(getFeatures(sentence, a, counter));
			}
			featureList.add(perIndex);
		}
		return featureList;
	}
	private float[] getFeatures(TaggedSentence sentence, int index, CompressedCounter counter)
	{
		return new float[] { (float) Math.log(counter.getCount(sentence.getToken(index)) + 1) };
	}
}
