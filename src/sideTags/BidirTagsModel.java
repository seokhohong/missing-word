package sideTags;

import java.util.ArrayList;
import java.util.List;

import model.Model;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.TaggedSentence;
import mwutils.Utils;
import strComp.Dictionary;
import strComp.MultiCompress;
import tools.ReadSimul;
import tools.ReadSimul.SimulLineOperation;
import utils.Multitasker;

public class BidirTagsModel implements Model
{
	private static final int FREQ_THRESHOLD = 5;
	private Counter<String> freqWords = Counter.load("C:/MissingWord/frequentWords.txt");
	Dictionary tagsDict = Dictionary.fromCounterFile("C:/MissingWord/tag/tags.txt");
	Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
	CompressedCounter counter;
	public static void main(String[] args)
	{
		for(boolean bool : new boolean[] {true, false} )
		{
			new BidirTagsModel(1, 2, 1).go(bool);
			new BidirTagsModel(1, 2, 2).go(bool);
			new BidirTagsModel(1, 2, 3).go(bool);
		}
	}
	private int lTags;
	private int rTags;
	private int offset;
	private String threshold = "";
	public BidirTagsModel(int lTags, int rTags, int offset, int threshold)
	{
		this(lTags, rTags, offset);
		this.threshold = Integer.toString(threshold);
	}
	public BidirTagsModel(int lTags, int rTags, int offset)
	{
		this.lTags = lTags;
		this.rTags = rTags;
		this.offset = offset;
		Dictionary[] dicts = new Dictionary[lTags + rTags + 1];
		for(int a = 0; a < dicts.length; a++)
		{
			dicts[a] = tagsDict;
		}
		dicts[lTags] = wordsDict;
		counter = CompressedCounterFactory.getInstance(new MultiCompress(dicts));
	}
	@Override
	public List<List<float[]>> process(List<TaggedSentence> sentences) 
	{	
		Dictionary[] dicts = new Dictionary[lTags + rTags + 1];
		for(int a = 0; a < lTags + rTags + 1; a++)
		{
			dicts[a] = tagsDict;
		}
		dicts[lTags] = wordsDict;
		MultiCompress mc = new MultiCompress(dicts);
		
		CompressedCounter orig = CompressedCounterFactory.load("D:/MissingWord/sideTags/"+lTags+"l"+rTags+"r"+offset+"offset"+threshold+".txt", mc);
		CompressedCounter mod = CompressedCounterFactory.load("D:/MissingWord/sideTags/"+lTags+"l"+rTags+"r"+offset+"offsetMod"+threshold+".txt", mc);
		
		List<List<float[]>> featureList = new ArrayList<>();
		for(TaggedSentence sentence : sentences)
		{
			List<float[]> perIndex = new ArrayList<>();
			for(int a = 0; a < sentence.length(); a++)
			{
				perIndex.add(getFeatures(sentence, a, orig, mod));
			}
			featureList.add(perIndex);
		}
		return featureList;
	}
	private float[] getFeatures(TaggedSentence sentence, int index, CompressedCounter origCounter, CompressedCounter modCounter)
	{
		float[] features = new float[2];
		String[] elems = new String[lTags + rTags + 1];
		for(int a = 0; a < lTags + rTags + 1; a ++)
		{
			elems[a] = Utils.getElem(sentence.getTags(), index - (lTags - a) - 1);
		}
		elems[lTags] = Utils.getElem(sentence.getTokens(), index - 1);
		features[0] = (float) Math.log((double) (origCounter.getCount(elems) + modCounter.getCount(elems) + 1) / (origCounter.getCount(elems) + 1));
		features[1] = (float) Math.log(origCounter.getCount(elems) + modCounter.getCount(elems) + 1);
		return features;
	}
	public void go(boolean mod)
	{
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new ReadSentences("D:/MissingWord/train/tokensPart"+a+".txt", "D:/MissingWord/train/tagsPart"+a+".txt", mod));
		}
		multitasker.done();
		String modTag = mod ? "Mod" : "";
		counter.export("C:/MissingWord/sideTags/"+lTags+"l"+rTags+"r"+offset+"offset"+modTag+".txt");
	}
	
	class ReadSentences implements SimulLineOperation, Runnable
	{
		private String file1;
		private String file2;
		private boolean mod;
		ReadSentences(String file1, String file2, boolean mod)
		{
			this.file1 = file1;
			this.file2 = file2;
			this.mod = mod;
		}
		int numLine = 0;
		@Override
		public void read(String line1, String line2) 
		{
			numLine ++;
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+counter.keySize());
			}
			tagWord(line1.split(" "), line2.split(" "), lTags, rTags, mod);
		}
		@Override
		public void run() 
		{
			ReadSimul.byLine(file1, file2, this);
		}
	}
	private void tagWord(String[] tokens, String[] tags, int lTags, int rTags, boolean mod)
	{
		int tTags = lTags + rTags + 1;
		
		for(int a = 0; a < tokens.length; a++)
		{
			if(freqWords.getCount(tokens[a]) > FREQ_THRESHOLD)
			{
				String[] elems = new String[tTags];
				for(int b = 0; b < tTags; b ++)
				{
					int adj = mod && b >= offset ? 1 : 0;
					int elemIndex = a - offset + b + adj;
					if(b != lTags)
					{
						elems[b] = Utils.getElem(tags, elemIndex);
					}
					else
					{
						elems[b] = Utils.getElem(tokens, elemIndex);
					}
				}
				counter.add(elems);
			}
		}
	}
}
