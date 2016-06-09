package partialTag;

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
public class NPartialLexModel implements Model
{
	private int windowSize;
	private int leftOffset;
	public static void main(String[] args)
	{
		//new NTagModel(6).go();
		for(boolean bool : new boolean[] {true} )
		{
			//NPartialLexModel.make(2, 1, bool);
			/*
			NPartialLexModel.make(3, 1, bool);
			NPartialLexModel.make(3, 2, bool);
			NPartialLexModel.make(4, 1, bool);
			NPartialLexModel.make(4, 2, bool);
			NPartialLexModel.make(4, 3, bool);
			*/
			NPartialLexModel.make(5, 1, bool);
			NPartialLexModel.make(5, 2, bool);
			NPartialLexModel.make(5, 3, bool);
			NPartialLexModel.make(5, 4, bool);
		}
	}
	private String threshold = "";
	public NPartialLexModel(int windowSize, int leftOffset)
	{
		this.windowSize = windowSize;
		this.leftOffset = leftOffset;
	}
	public NPartialLexModel(int windowSize, int leftOffset, int threshold)
	{
		this(windowSize, leftOffset);
		this.threshold = Integer.toString(threshold);
	}
	@Override
	public List<List<float[]>> process(List<TaggedSentence> sentences) 
	{	
		Dictionary partialLexDict = Dictionary.fromCounterFile("C:/MissingWord/partialLex/partialLex.txt");
		Dictionary[] dicts = new Dictionary[windowSize];
		for(int a = 0; a < windowSize; a++)
		{
			dicts[a] = partialLexDict;
		}
		MultiCompress mc = new MultiCompress(dicts);
		
		CompressedCounter orig = CompressedCounterFactory.load("D:/MissingWord/partialLex/"+windowSize+"partialLex"+leftOffset+"offset"+threshold+".txt", mc);
		CompressedCounter mod = CompressedCounterFactory.load("D:/MissingWord/partialLex/"+windowSize+"partialLex"+leftOffset+"offsetMod"+threshold+".txt", mc);
		
		List<List<float[]>> featureList = new ArrayList<>();
		for(TaggedSentence sentence : sentences)
		{
			List<float[]> perIndex = new ArrayList<>();
			for(int a = 0; a < sentence.length(); a++)
			{
				perIndex.add(getFeatures(sentence, a, orig, mod, partialLexDict));
			}
			featureList.add(perIndex);
		}
		return featureList;
	}
	private float[] getFeatures(TaggedSentence sentence, int index, CompressedCounter origCounter, CompressedCounter modCounter, Dictionary lexDict)
	{
		float[] features = new float[2];
		String[] elems = new String[windowSize];
		String[] lex = getLex(sentence, lexDict);
		for(int b = 0; b < windowSize; b++)
		{
			elems[b] = Utils.getElem(lex, index + b - leftOffset);
		}
		features[0] = (float) Math.log((double) (origCounter.getCount(elems) + modCounter.getCount(elems) + 1) / (origCounter.getCount(elems) + 1));
		features[1] = (float) Math.log(origCounter.getCount(elems) + modCounter.getCount(elems) + 1);
		return features;
	}
	public String[] getLex(TaggedSentence sentence, Dictionary lexDict)
	{
		String[] tags = sentence.getTags();
		String[] tokens = sentence.getTokens();
		String[] lex = new String[tokens.length];
		for(int b = 0; b < tokens.length; b++)
		{
			String token = tokens[b];
			String combined = token+"_"+tags[b];
			lex[b] = combined;
			if(!lexDict.contains(combined) && token.length() > 0)
			{
				lex[b] = tags[b];
			}
		}
		return lex;
	}
	static Dictionary tagDict = Dictionary.fromCounterFile("C:/MissingWord/partialLex/partialLex.txt");
	public static void make(int winSize, int offset, boolean mod)
	{
		Dictionary[] dicts = new Dictionary[winSize];
		for(int a = 0; a < winSize; a++)
		{
			dicts[a] = tagDict;
		}
		CompressedCounter counter = CompressedCounterFactory.getInstance(new MultiCompress(dicts));
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new NPartialTagMaker("C:/MissingWord/train/tokensPart"+a+".txt", "C:/MissingWord/train/tagsPart"+a+".txt", winSize, offset, counter, mod));
			//new NPartialTagMaker("C:/MissingWord/train/tokensPart"+a+".txt", "C:/MissingWord/train/tagsPart"+a+".txt", winSize, counter, mod).run();
			
		}
		multitasker.done();
		String modString = mod ? "Mod" : "";
		counter.export("D:/MissingWord/partialLex/"+winSize+"partialLex"+offset+"offset"+modString+".txt");
	}
	static class NPartialTagMaker implements SimulLineOperation, Runnable
	{
		private int windowSize;
		private String file1;
		private String file2;
		private boolean mod;
		private CompressedCounter counter;
		private int offset = 0;
		public NPartialTagMaker(String file1, String file2, int windowSize, int offset, CompressedCounter counter, boolean mod)
		{
			this.file1 = file1;
			this.file2 = file2;
			this.windowSize = windowSize;
			this.mod = mod;
			this.counter = counter;
			this.offset = offset;
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
				System.out.println(numLine+" "+counter.keySize());
			}
			String[] tokens = line1.split(" ");
			String[] tags = line2.split(" ");
			for(int a = - offset; a < tokens.length - offset; a++)
			{
				String[] lex = new String[windowSize];
				for(int b = 0; b < windowSize; b++)
				{
					int adj = mod & b >= offset ? 1 : 0;
					String token = Utils.getElem(tokens, a + b + adj);
					String tag = Utils.getElem(tags, a + b + adj);
					lex[b] = token + "_" + tag;
					if(!tagDict.contains(lex[b]) && lex[b].length() > 0)
					{
						lex[b] = tag;
					}
				}
				counter.add(lex);
			}
		}
	}
}
