package vertical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;
import model.Model;
import model.ParseModel;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.TaggedSentence;
import mwutils.Utils;
import strComp.Dictionary;
import strComp.MultiCompress;
import tools.ReadSimul;
import tools.ReadSimul.SimulLineOperation;
import tree.ReconstructTree;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class VerticalMarkovModel implements ParseModel
{
	private int windowSize;
	private int leftOffset;
	private String threshold = "";
	public static void main(String[] args)
	{
		//new NTagModel(6).go();
		int windowSize = 5;
		for(int offset = 1; offset < 5; offset++)
		{
			VerticalMarkovModel.make(windowSize, offset, true);
			VerticalMarkovModel.make(windowSize, offset, false);
		}
	}
	public VerticalMarkovModel(int windowSize, int leftOffset)
	{
		this.leftOffset = leftOffset;
		this.windowSize = windowSize;
		if(leftOffset >= windowSize)
		{
			System.out.println("Parameter Error");
			System.exit(1);
		}
	}
	public VerticalMarkovModel(int windowSize, int leftOffset, int threshold)
	{
		this(windowSize, leftOffset);
		this.threshold = Integer.toString(threshold);
	}
	@Override
	public List<List<float[]>> process(List<String> sentences) 
	{
		Dictionary tagDict = Dictionary.fromCounterFile("C:/MissingWord/vertical/verticalDict.txt");
		Dictionary[] dicts = new Dictionary[windowSize];
		for(int a = 0; a < windowSize; a++)
		{
			dicts[a] = tagDict;
		}
		MultiCompress mc = new MultiCompress(dicts);
		
		CompressedCounter orig = CompressedCounterFactory.load("D:/MissingWord/vertical/"+windowSize+"tag"+leftOffset+"offset"+threshold+".txt", mc);
		CompressedCounter mod = CompressedCounterFactory.load("D:/MissingWord/vertical/"+windowSize+"tag"+leftOffset+"offset"+"Mod"+threshold+".txt", mc);
		
		List<List<float[]>> featureList = new ArrayList<>();
		for(String sentence : sentences)
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
	private float[] getFeatures(String sentence, int index, CompressedCounter origCounter, CompressedCounter modCounter)
	{

		float[] features = new float[2];
		try
		{
			String[] verticalTags = featuresArray(sentence);
			String[] elems = new String[windowSize];
			for(int b = 0; b < windowSize; b++)
			{
				elems[b] = Utils.getElem(verticalTags, index + b - leftOffset);
			}
			features[0] = (float) Math.log((double) (origCounter.getCount(elems) + modCounter.getCount(elems) + 1) / (origCounter.getCount(elems) + 1));
			features[1] = (float) Math.log(origCounter.getCount(elems) + modCounter.getCount(elems) + 1);
		}
		catch(Exception e)
		{
			return features;
		}


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
		Dictionary tagDict = Dictionary.fromCounterFile("C:/MissingWord/vertical/verticalDict.txt");
		Dictionary[] dicts = new Dictionary[windowSize];
		for(int a = 0; a < windowSize; a++)
		{
			dicts[a] = tagDict;
		}
		CompressedCounter counter = CompressedCounterFactory.getInstance(new MultiCompress(dicts));
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new NVerticalMaker("D:/MissingWord/train/parsePart"+a+".txt", counter, windowSize, leftOffset, mod));
		}
		multitasker.done();
		String modStr = mod ? "Mod" : ""; 
		counter.export("D:/MissingWord/vertical/"+windowSize+"tag"+leftOffset+"offset"+modStr+".txt");
	}
	private static String[] featuresArray(String treeLine) throws Exception
	{
		Tree root = ReconstructTree.reconstruct(treeLine);
		List<VerticalElem> preTerminals = new ArrayList<>();
		for(Tree node : root.subTrees())
		{
			if(node.isPreTerminal())
			{
				preTerminals.add(new VerticalElem(root, node));
			}
		}
		String[] array = new String[preTerminals.size()];
		for(int a = 0; a < array.length; a++)
		{
			array[a] = preTerminals.get(a).toString();
		}
		return array;
	}
	public static class NVerticalMaker implements LineOperation, Runnable
	{
		Dictionary tagDict = Dictionary.fromCounterFile("C:/MissingWord/vertical/verticalDict.txt");
		private int windowSize;
		private boolean mod;
		private int leftOffset;
		private String file;
		private CompressedCounter counter;
		public NVerticalMaker(String file, CompressedCounter counter, int windowSize, int leftOffset, boolean mod)
		{
			this.windowSize = windowSize;
			this.counter = counter;
			this.mod = mod;
			this.leftOffset = leftOffset;
			this.file = file;
		}
		public void run()
		{
			Read.byLine(file, this);
		}
		private int numLine = 0;
		@Override
		public void read(String line) 
		{
			numLine ++;
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+counter.keySize()+" "+Thread.currentThread().getName());
			}
			try
			{
				String[] array = featuresArray(line);
				for(int a = - leftOffset; a < array.length + leftOffset; a++)
				{
					String[] tags = new String[windowSize];
					for(int b = 0; b < windowSize; b++)
					{
						int adj = mod & b >= leftOffset ? 1 : 0;
						tags[b] = Utils.getElem(array, a + b + adj); 
					}
					counter.add(tags);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
