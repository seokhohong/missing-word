package tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Gold;
import mwutils.MapCompressor;
import mwutils.Utils;
import strComp.Dictionary;
import strComp.MultiCompress;
import tools.ReadSimul;
import tools.ReadSimul.SimulLineOperation;
import utils.ArrayUtils;
import utils.Multitasker;
import utils.Write;
import gnu.trove.map.TLongDoubleMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import correct.CorrectorDict;
import correct.CorrectorDict.CorrectorOperation;
import correct.Corrector;

public class NTagCorrector implements Corrector
{
	
	public static void main(String[] args)
	{
		new NTagCorrector(4, 3).make();
	}
	
	
	public Map<Gold, TLongSet> suggest(List<Gold> golds)
	{
		Suggest suggestionOp = new Suggest(golds);
		CorrectorDict.makePass(suggestionOp);
		return suggestionOp.suggestions;
	}

	class Suggest implements CorrectorOperation
	{
		private TLongObjectMap<List<Gold>> correctionPairs;
		private Map<Gold, TLongSet> suggestions = new HashMap<>();
		private Suggest(List<Gold> golds)
		{
			correctionPairs = getCorrectionPairs(golds);
		}
		@Override
		public void found(long l, MapCompressor map) 
		{
			List<Gold> relevantGolds = correctionPairs.get(l);
			for(Gold gold : relevantGolds)
			{
				suggestions.put(gold, map.keySet());
			}
		}

		@Override
		public String filename(int part) 
		{
			return NTagCorrector.this.filename(part);
		}

		@Override
		public TLongSet keys() 
		{
			return correctionPairs.keySet();
		}
		
	}
	public void reweight(Map<Gold, TLongDoubleMap> weights)
	{
		Reweight reweight = new Reweight(weights);
		CorrectorDict.makePass(reweight);
	}
	class Reweight implements CorrectorOperation
	{
		//feature set mapped to golds that have this particular feature set
		private Map<Gold, TLongDoubleMap> weights;
		//tag feature mapped to golds that have it
		private TLongObjectMap<List<Gold>> correctionPairs;
		private Reweight(Map<Gold, TLongDoubleMap> weights)
		{
			this.weights = weights;
			correctionPairs = getCorrectionPairs(weights.keySet());
		}
		@Override
		public void found(long feature, MapCompressor map)
		{
			for(Gold gold : correctionPairs.get(feature))
			{
				TLongIntMap decompressed = CorrectorDict.decompress(map);
				TLongDoubleMap weights = generateFeatures(decompressed);
				mergeWith(gold, weights);
			}
		}
		
		private void mergeWith(Gold gold, TLongDoubleMap additionalWeights)
		{
			add(weights.get(gold), additionalWeights);
		}
		
		private void add(TLongDoubleMap orig, TLongDoubleMap toAdd)
		{
			//only adds if key is in original
			for(long l : orig.keys())
			{
				if(toAdd.containsKey(l))
				{
					orig.adjustValue(l, toAdd.get(l));
				}
			}
		}

		@Override
		public String filename(int part) 
		{
			return NTagCorrector.this.filename(part);
		}


		@Override
		public TLongSet keys() 
		{
			return correctionPairs.keySet();
		}
	}
	private TLongDoubleMap generateFeatures(TLongIntMap counts)
	{
		TLongDoubleMap scores = new TLongDoubleHashMap();
		double totalCount = ArrayUtils.sum(counts.values());
		double confidence = Math.log(totalCount);
		double coherence = Math.sqrt(ArrayUtils.sumOfSquares(counts.values())) / totalCount;
		for(long word : counts.keys())
		{
			double probability = Math.log((double) counts.get(word) / totalCount);
			scores.put(word, probability * confidence * coherence);
		}
		return scores;
	}
	private TLongObjectMap<List<Gold>> getCorrectionPairs(Collection<Gold> golds)
	{
		TLongObjectMap<List<Gold>> correctionPairs = new TLongObjectHashMap<>();
		for(Gold gold : golds)
		{
			int goldOffset = gold.removeIndex() - offset;
			String[] elems = new String[windowSize];
			for(int a = 0; a < windowSize; a++)
			{
				elems[a] = Utils.getElem(gold.getTags(), a + goldOffset);
			}
			long key = tagMc.encode(elems);
			if(!correctionPairs.containsKey(key))
			{
				correctionPairs.put(key, new ArrayList<>());
			}
			correctionPairs.get(tagMc.encode(elems)).add(gold);
		}
		return correctionPairs;
	}
	
	class FeatureCorrectionPair
	{
		long feature;
		String word;
		private FeatureCorrectionPair(long feature, String word)
		{
			this.feature = feature;
			this.word = word;
		}
	}
	
	public String filename(int part)
	{
		return "C:/MissingWord/corr/ntag/"+windowSize+"-"+offset+"Part"+part+".txt";
	}
	
	private void make()
	{
		TLongSet exploredKeys = new TLongHashSet();
		int part = 0;
		while(true)
		{
			CorrectorDict corrector = new CorrectorDict(exploredKeys);
			Multitasker multitasker = new Multitasker();
			for(int a = 0; a < 5; a++)
			{
				multitasker.load(new FillCorrector("D:/MissingWord/train/tokensPart"+a+".txt", "D:/MissingWord/train/tagsPart"+a+".txt", corrector));
			}
			multitasker.done();
			if(corrector.keys().isEmpty())
			{
				break;
			}
			Write.to(filename(part), corrector.export());
			exploredKeys.addAll(corrector.keys());
			part ++;
		}
	}

	class FillCorrector implements SimulLineOperation, Runnable
	{
		private CorrectorDict corrector;
		private String tokenInput;
		private String tagInput;
		private FillCorrector(String tokenInput, String tagInput, CorrectorDict corrector)
		{
			this.tokenInput = tokenInput;
			this.tagInput = tagInput;
			this.corrector = corrector;
		}
		int numLine = 0;
		@Override
		public void read(String line1, String line2) 
		{
			numLine ++;
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+corrector.keys().size());
			}
			String[] tokens = line1.split(" ");
			String[] lineTags = line2.split(" ");
			for(int a = - offset; a < lineTags.length + offset; a++)
			{
				String[] tags = new String[windowSize];
				for(int b = 0; b < windowSize; b++)
				{
					int adj = b >= offset ? 1 : 0;
					tags[b] = Utils.getElem(lineTags, a + b + adj); 
				}
				corrector.add(tagMc.encode(tags), Utils.getElem(tokens, a + offset));
			}
		}
		@Override
		public void run() 
		{
			ReadSimul.byLine(tokenInput, tagInput, this);
		}
		
	}
	
	private int windowSize;
	private int offset;
	private MultiCompress tagMc;
	private Dictionary wordDict = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
	public NTagCorrector(int windowSize, int offset) 
	{
		this.windowSize = windowSize;
		this.offset = offset;
		Dictionary tagDict = Dictionary.fromCounterFile("C:/MissingWord/tag/tags.txt");
		tagMc = new MultiCompress(tagDict, windowSize);
	}

}
