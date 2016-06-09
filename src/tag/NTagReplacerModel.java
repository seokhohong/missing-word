package tag;

import java.util.ArrayList;
import java.util.List;

import model.Model;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.TaggedSentence;
import mwutils.Utils;
import strComp.Dictionary;
import strComp.MultiCompress;
import utils.ArrayUtils;

public class NTagReplacerModel implements Model
{
	/** windowSize is the base size, referring to the probabilities of the n+1 model */
	private int windowSize;
	private int offset;
	private Dictionary tagDict = Dictionary.fromCounterFile("C:/MissingWord/tags.txt");
	public NTagReplacerModel(int windowSize, int offset)
	{
		this.windowSize = windowSize;
		this.offset = offset;
	}
	@Override
	public List<List<float[]>> process(List<TaggedSentence> sentences) 
	{
		
		CompressedCounter small = CompressedCounterFactory.load("C:/MissingWord/tag/"+windowSize+"tag"+offset+"offset.txt", new MultiCompress(tagDict, windowSize));
		CompressedCounter large = CompressedCounterFactory.load("C:/MissingWord/tag/"+(windowSize + 1)+"tag"+(offset + 1)+"offset"+"Mod.txt", new MultiCompress(tagDict, windowSize + 1));
		
		List<List<float[]>> featureList = new ArrayList<>();
		for(TaggedSentence sentence : sentences)
		{
			List<float[]> perIndex = new ArrayList<>();
			for(int a = 0; a < sentence.length(); a++)
			{
				perIndex.add(getFeatures(sentence, a, small, large));
			}
			featureList.add(perIndex);
		}
		return featureList;
	}
	private float[] getFeatures(TaggedSentence sentence, int index, CompressedCounter small, CompressedCounter large)
	{
		float[] features = new float[2];
		String[] elems = new String[windowSize];
		for(int b = 0; b < windowSize; b++)
		{
			elems[b] = Utils.getElem(sentence.getTags(), index + b - offset);
		}
		List<Integer> counts = new ArrayList<>();
		List<String> tag = new ArrayList<>(tagDict.keySet());
		double sum = 0;
		for(String key : tag)
		{
			String[] largerElems = new String[windowSize + 1];
			for(int b = 0; b < windowSize; b++)
			{
				int adj = b >= offset ? 1 : 0;
				if(b == offset)
				{
					largerElems[b] = key;
				}
				largerElems[b + adj] = Utils.getElem(sentence.getTags(), index + b - offset);
			}
			sum += large.getCount(largerElems);
			counts.add(large.getCount(largerElems));
		}
		double coherence = Utils.normSS(counts);
		if(sum < 1)
		{
			coherence = 0;
		}
		
		features[0] = (float) coherence;
		features[1] = (float) Math.log(sum);
		return features;
	}
}
