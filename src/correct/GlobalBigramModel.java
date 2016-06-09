package correct;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.Gold;
import mwutils.CompressedCounter;
import mwutils.CompressedCounterFactory;
import mwutils.Counter;
import mwutils.Utils;
import strComp.Dictionary;
import strComp.MultiCompress;
import tag.NTagModel.NTagMaker;
import utils.ListUtils;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class GlobalBigramModel implements AdvancedReplacer
{
	private static Dictionary wordsDict = Dictionary.fromCounterFile("C:/MissingWord/midFrequentWords.txt");
	Counter<String> unigramCounter = Counter.load("C:/MissingWord/midFrequentWords.txt");
	/*
	public static void main(String[] args)
	{
		GlobalBigramModel.make();
	}
	 */
	private int[][] matrix;

	public GlobalBigramModel()
	{

	}
	public void load()
	{
		matrix = getMatrix();
	}
	public void unload()
	{
		matrix = null;
	}
	private static void make()
	{
		int[][] matrix = new int[wordsDict.size()][wordsDict.size()];

		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new GlobalBigramMaker("C:/MissingWord/train/tokensPart"+a+".txt", matrix));
		}
		multitasker.done();
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("C:/MissingWord/ngrams/bigramMatrix.ser"));
			out.writeObject(matrix);
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	static class GlobalBigramMaker implements Runnable, LineOperation
	{
		private String file;
		private int[][] matrix;
		GlobalBigramMaker(String file, int[][] matrix)
		{
			this.file = file;
			this.matrix = matrix;
		}
		private int numLines = 0;
		public void read(String line)
		{
			numLines ++ ;
			if(numLines % 10000 == 0)
			{
				System.out.println(numLines);
			}
			String[] tokens = line.split(" ");
			for(int a = 0; a < tokens.length; a++)
			{
				if(wordsDict.contains(tokens[a]))
				{
					for(int b = 0; b < a; b ++)
					{
						if(tokens[a] != tokens[b] && wordsDict.contains(tokens[b]))
						{
							int order = tokens[a].compareTo(tokens[b]);
							String one = order == 1 ? tokens[a] : tokens[b];
							String two = order == 1 ? tokens[b] : tokens[a];
							//counter.add(one, two);
							synchronized(matrix)
							{
								if(matrix[wordsDict.encode(one)][wordsDict.encode(two)] < Integer.MAX_VALUE)
								{
									matrix[wordsDict.encode(one)][wordsDict.encode(two)] += 1;
								}
							}
						}
					}
				}
			}
		}
		@Override
		public void run() 
		{
			Read.byLine(file, this);
		}
	}
	private int[][] getMatrix()
	{
		int[][] matrix = null;
		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("C:/MissingWord/ngrams/bigramMatrix.ser"));
			matrix = (int[][]) in.readObject();
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return matrix;
	}
	@Override
	public void reweight(FixOptions fixOptions, double weight) 
	{
		for(String fix : fixOptions.getFixWords())
		{
			String fixToken = Utils.getToken(fix);
			if(!wordsDict.contains(fixToken)) continue;
			String[] otherChunks = fixOptions.fillArray(fix);
			double sumLogScore = 0;
			int numWords = 0;
			for(String otherChunk : otherChunks)
			{
				String otherToken = Utils.getToken(otherChunk);
				if(!fix.equals(otherChunk) && wordsDict.contains(otherToken))
				{
					numWords ++;
					int bothCount = matrix[wordsDict.encode(otherToken)][wordsDict.encode(fixToken)] + matrix[wordsDict.encode(fixToken)][wordsDict.encode(otherToken)];
					double bothProb = (double) (bothCount + 1);
					sumLogScore += Math.log(bothProb) - Math.log(unigramCounter.getCount(otherToken) - Math.log(unigramCounter.getCount(fixToken)));
				}
			}
			double modification = sumLogScore / numWords;
			fixOptions.multLogScore(fix, modification * weight);
		}
	}

}
