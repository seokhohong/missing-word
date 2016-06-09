package npreduce;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import edu.stanford.nlp.trees.Tree;
import tree.ReconstructTree;
import utils.ArrayUtils;
import utils.ListUtils;
import utils.Read;
import utils.Read.LineOperation;

public class ParserNPReduction 
{
	private boolean remove = false;
	private Random rnd = new Random();
	private int skipTo = 0;
	public static void main(String[] args)
	{
		new ParserNPReduction().go();
	}
	private void go()
	{
		int length = 6;
		/*
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("C:/MissingWord/train/parsePart"+a+".txt", new Count("C:/MissingWord/npReduce"+length+"Part"+a+".txt", length));
		}
		*/
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("C:/MissingWord/train/parseModPart"+a+".txt", new Count("C:/MissingWord/npReduceMod"+length+"Part"+a+".txt", length));
		}
	}
	class Count implements LineOperation
	{
		int numLine = 0;
		BufferedWriter outputWriter;
		private int length;
		public Count(String output, int length)
		{
			this.length = length;
			try
			{
				outputWriter = new BufferedWriter(new FileWriter(output));
			}
			catch(IOException e) { e.printStackTrace(); }
		}
		@Override
		public void read(String line) 
		{
			numLine ++;
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine);
			}
			if(numLine < skipTo)
			{
				return;
			}
			if(line.length() < 2) return;
			int removeIndex = 0;
			if(remove == true)
			{
				String[] tokens = line.split(" ");
				removeIndex = rnd.nextInt(tokens.length - 2) + 1;
				if(tokens.length < 3) return;
				tokens = ArrayUtils.cutout(tokens, removeIndex);
				line = ListUtils.join(Arrays.asList(tokens), " ");
			}
			Tree tree = null;
			try
			{
				tree = ReconstructTree.reconstruct(line);
			}
			catch(Exception ignored)
			{
				
			}
			if(tree == null) return;
			List<String> windows = NPReduce.foldNP(tree, length);
			for(int a = 0; a < windows.size(); a++)
			{
				try
				{
					if(remove == false || (a > removeIndex && a < removeIndex + windows.size()))
					{
						outputWriter.write(windows.get(a));
						outputWriter.newLine();
					}
				}
				catch(IOException e) { e.printStackTrace(); }
			}

		}
		
	}
}
