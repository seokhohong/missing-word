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
import tools.ReadSimul;
import tools.ReadSimul.SimulLineOperation;
import utils.Multitasker;


//3-2 division for mod
public class NTagModel implements Model
{
	private int windowSize;
	private int leftOffset;
	private String threshold = "";
	public static void main(String[] args)
	{
		//new NTagModel(6).go();
		int windowSize = 6;
		for(int offset = 1; offset < 6; offset++)
		{
			NTagModel.make(windowSize, offset, true);
			NTagModel.make(windowSize, offset, false);
		}
	}
	public NTagModel(int windowSize, int leftOffset)
	{
		this.leftOffset = leftOffset;
		this.windowSize = windowSize;
		if(leftOffset >= windowSize)
		{
			System.out.println("Parameter Error");
			System.exit(1);
		}
	}
	public NTagModel(int windowSize, int leftOffset, int threshold)
	{
		this(windowSize, leftOffset);
		this.threshold = Integer.toString(threshold);
	}
	@Override
	public List<List<float[]>> process(List<TaggedSentence> sentences) 
	{
		Dictionary tagDict = Dictionary.fromCounterFile("C:/MissingWord/tag/tags.txt");
		Dictionary[] dicts = new Dictionary[windowSize];
		for(int a = 0; a < windowSize; a++)
		{
			dicts[a] = tagDict;
		}
		MultiCompress mc = new MultiCompress(dicts);
		
		CompressedCounter orig = CompressedCounterFactory.load("D:/MissingWord/tag/"+windowSize+"tag"+leftOffset+"offset"+threshold+".txt", mc);
		CompressedCounter mod = CompressedCounterFactory.load("D:/MissingWord/tag/"+windowSize+"tag"+leftOffset+"offset"+"Mod"+threshold+".txt", mc);
		
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
		String[] elems = new String[windowSize];
		for(int b = 0; b < windowSize; b++)
		{
			elems[b] = Utils.getElem(sentence.getTags(), index + b - leftOffset);
		}
		features[0] = (float) Math.log((double) (origCounter.getCount(elems) + modCounter.getCount(elems) + 1) / (origCounter.getCount(elems) + 1));
		features[1] = (float) Math.log(origCounter.getCount(elems) + modCounter.getCount(elems) + 1);
		return features;
	}
	public String[] tags(String taggingOutput)
	{
		String[] bigTokens = taggingOutput.split(" ");
		String[] tokens = new String[bigTokens.length];
		for(int a = 0; a < bigTokens.length; a++)
		{
			tokens[a] = bigTokens[a].split("/")[1];
		}
		return tokens;
	}
	public static void make(int windowSize, int leftOffset, boolean mod)
	{
		Dictionary tagDict = Dictionary.fromCounterFile("C:/MissingWord/tag/tags.txt");
		Dictionary[] dicts = new Dictionary[windowSize];
		for(int a = 0; a < windowSize; a++)
		{
			dicts[a] = tagDict;
		}
		CompressedCounter counter = CompressedCounterFactory.getInstance(new MultiCompress(dicts));
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new NTagMaker("D:/MissingWord/train/tokensPart"+a+".txt", "D:/MissingWord/train/tagsPart"+a+".txt", counter, windowSize, leftOffset, mod));
		}
		multitasker.done();
		String modStr = mod ? "Mod" : ""; 
		counter.export("D:/MissingWord/tag/"+windowSize+"tag"+leftOffset+"offset"+modStr+".txt");
	}
	public static class NTagMaker implements SimulLineOperation, Runnable
	{
		private int windowSize;
		private boolean mod;
		private int leftOffset;
		private String file1;
		private String file2;
		private CompressedCounter counter;
		public NTagMaker(String file1, String file2, CompressedCounter counter, int windowSize, int leftOffset, boolean mod)
		{
			this.windowSize = windowSize;
			this.counter = counter;
			this.mod = mod;
			this.leftOffset = leftOffset;
			this.file1 = file1;
			this.file2 = file2;
		}
		public void run()
		{
			ReadSimul.byLine(file1, file2, this);
		}
		private int numLine = 0;
		@Override
		public void read(String line1, String line2) 
		{
			numLine ++;
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+counter.keySize()+" "+Thread.currentThread().getName());
			}
			String[] lineTags = line2.split(" ");
			for(int a = - leftOffset; a < lineTags.length + leftOffset; a++)
			{
				String[] tags = new String[windowSize];
				for(int b = 0; b < windowSize; b++)
				{
					int adj = mod & b >= leftOffset ? 1 : 0;
					tags[b] = Utils.getElem(lineTags, a + b + adj); 
				}
				counter.add(tags);
			}
		}
	}
}
