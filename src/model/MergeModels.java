package model;

import global.PCFGSelectImprovementModel;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import partialTag.NPartialLexModel;
import lexbigram.*;
import main.Gold;
import main.GoldStandard;
import mwutils.TaggedSentence;
import sideTags.BidirTagsModel;
import tag.NTagModel;
import utils.ListUtils;
import utils.Read;
import utils.Read.LineOperation;

public class MergeModels 
{
	private int length;
	private List<Gold> golds;
	private List<Model> models = new ArrayList<>();
	private String lengthString;
	private static final int BATCH_SIZE_TIMES_WORD = 5000000; //control memory a bit better
	private int batchSize;
	public static void main(String[] args)
	{
		for(int a = 15; a < 16; a++)
		{
			new MergeModels(a).make(400000);
		}
		/*
		for(int a = 30; a < 50; a++)
		{
			new MergeModels(a).make(300000);
		}
		*/
	}
	
	public MergeModels(int length)
	{
		this.length = length;
		this.batchSize = BATCH_SIZE_TIMES_WORD / length;
		GoldStandard goldStd = new GoldStandard(length);
		golds = goldStd.getGolds(length);
		lengthString = length == -1 ? "" : Integer.toString(length);
	}
	
	private void make(int numToMake)
	{
		numToMake = Math.min(numToMake, golds.size());
		loadModels();
		exportBatches(numToMake);
		combineBatches(numToMake);
	}
	
	private void exportBatches(int numToMake)
	{
		for(int part = 0; part < Math.ceil((double) numToMake / batchSize); part ++)
		{
			System.out.println((part * batchSize)+" - "+(Math.min((part + 1) * batchSize, numToMake)));
			List<Gold> partGolds = golds.subList(part * batchSize, Math.min((part + 1) * batchSize, numToMake));
			
			List<TaggedSentence> taggedSentences = new ArrayList<>();
			for(Gold gold : partGolds)
			{
				taggedSentences.add(TaggedSentence.fromTaggedLine(gold.incorrectSentence()));
			}
			
			List<List<List<float[]>>> allModelFeatures = new ArrayList<>();
			
			for(int a = 0; a < models.size(); a++)
			{
				Model model = models.get(a);
				allModelFeatures.add(model.process(taggedSentences));
				System.out.println(a);
			} 
			List<List<TFloatList>> merged = merge(allModelFeatures, taggedSentences);
			System.out.println(merged.size());
			
			export(partName(part), merged);
		}
	}
	private String partName(int part)
	{
		return "C:/MissingWord/merged/"+lengthString+"Part"+part+".txt";
	}
	private void combineBatches(int numToMake)
	{
		try
		{
			BufferedWriter combined = new BufferedWriter(new FileWriter("C:/MissingWord/merged/"+lengthString+".txt"));
			for(int a = 0; a < Math.ceil((double) numToMake / batchSize); a++)
			{
				Read.byLine(partName(a), new Transcribe(combined));
				new File(partName(a)).delete();
			}
			combined.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	class Transcribe implements LineOperation
	{
		private BufferedWriter writer;
		private Transcribe(BufferedWriter writer)
		{
			this.writer = writer;
		}
		@Override
		public void read(String line)
		{
			try
			{
				writer.write(line.trim());
				writer.newLine();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void loadModels()
	{
		//models.add(new NTagModel(3, 1));
		//models.add(new NTagModel(3, 2));
		//models.add(new NTagModel(4, 1));
		//models.add(new NTagModel(4, 2));
		//models.add(new NTagModel(4, 3));
		//models.add(new PCFGSelectImprovementModel());
		models.add(new NTagModel(5, 1));
		models.add(new NTagModel(5, 2));
		models.add(new NTagModel(5, 3));
		models.add(new NTagModel(5, 4));
		models.add(new NTagModel(6, 1, 5));
		models.add(new NTagModel(6, 2, 5));
		models.add(new NTagModel(6, 3, 5));
		models.add(new NTagModel(6, 4, 5));
		models.add(new NTagModel(6, 5, 5));
		models.add(new NTagModel(7, 4, 5));
		models.add(new BigramModel());
		models.add(new TrigramModel(1));
		models.add(new TrigramModel(2));
		models.add(new TrigramRarityModel(1));
		models.add(new BidirTagsModel(0, 2, 1));
		models.add(new BidirTagsModel(2, 0, 1));
		models.add(new BidirTagsModel(1, 1, 1));
		models.add(new BidirTagsModel(1, 1, 2));
		models.add(new BidirTagsModel(1, 2, 1, 5));
		models.add(new BidirTagsModel(1, 2, 2, 5));
		models.add(new BidirTagsModel(1, 2, 3, 5));
		models.add(new BidirTagsModel(2, 1, 1, 5));
		models.add(new BidirTagsModel(2, 1, 2, 5));
		models.add(new BidirTagsModel(2, 1, 3, 5));
		models.add(new NPartialLexModel(3, 1));
		models.add(new NPartialLexModel(3, 2));
		models.add(new NPartialLexModel(4, 1));
		models.add(new NPartialLexModel(4, 2));
		models.add(new NPartialLexModel(4, 3));
		models.add(new NPartialLexModel(5, 1, 5));
		models.add(new NPartialLexModel(5, 2, 5));
		models.add(new NPartialLexModel(5, 3, 5));
		models.add(new NPartialLexModel(5, 4, 5));
		models.add(new UnigramModel());
	}

	private List<List<TFloatList>> merge(List<List<List<float[]>>> allModelFeatures, List<TaggedSentence> tagged)
	{
		List<List<TFloatList>> merged = new ArrayList<>();
		int numModels = allModelFeatures.size();
		int numSentences = allModelFeatures.get(0).size();

		for(int a = 0; a < numSentences; a++)
		{
			merged.add(new ArrayList<>());
			int numIndices = allModelFeatures.get(0).get(a).size();
			if(numIndices != tagged.get(a).length())
			{
				System.out.println("Error");
			}
			for(int b = 0; b < numIndices; b++)
			{
				merged.get(a).add(new TFloatArrayList());
				for(int c = 0; c < numModels; c++)
				{
					for(double value : allModelFeatures.get(c).get(a).get(b))
					{
						//fml
						if(Double.isFinite(value))
						{
							merged.get(a).get(b).add((float) value);
						}
						else
						{
							System.out.println("NaN");
							merged.get(a).get(b).add(0f);
						}
					}
				}
				merged.get(a).get(b).add((float) b); //index of frame
			}
		}
		return merged;
	}
	private void export(String filename, List<List<TFloatList>> features)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			for(List<TFloatList> sentenceData : features)
			{
				for(TFloatList indexData : sentenceData)
				{
					writer.write(print(indexData));
					writer.newLine();
				}
				writer.write("\n");
			}
			writer.close();
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}
	public static <T> String join(TFloatList list, String delim)
	{
		if(list.isEmpty()) return "";
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < list.size() - 1; a ++)
		{
			builder.append(list.get(a));
			builder.append(delim);
		}
		builder.append(list.get(list.size() - 1));
		return builder.toString();
	}
	public static <T> String print(TFloatList list)
	{
		return join(list, ", ");
	}
}
