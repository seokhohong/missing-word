package parseGram;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import edu.stanford.nlp.trees.Tree;
import stanford.SRParser;
import utils.ArrayUtils;
import utils.ListUtils;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class ParseEverything 
{
	private Random rnd = new Random();
	SRParser parser = null;
	public static void main(String[] args)
	{
		ParseEverything pe = new ParseEverything();
		pe.parse();
	}
	private boolean mod = false;
	private void parse()
	{
		parser = new SRParser();
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new ParseFile("D:/MissingWord/train/tokensPart"+a+".txt", "D:/MissingWord/train/parsePart"+a+".txt"));
		}
		multitasker.done();
		parser = null;
		System.gc();
	}
	/*
	private void parseMod(int interval)
	{
		parser = new SRParser();
		Multitasker multitasker = new Multitasker();
		for(int a = 4; a < 5; a++)
		{
			multitasker.load(new ParseFile("C:/MissingWord/train/cleanTokensPart"+a+".txt", "C:/MissingWord/train/parseModPart"+a+".txt", interval * 30000, (interval + 1) * 30000));
		}
		multitasker.done();
		parser = null;
		System.gc();
	}
	*/
	class ParseFile implements Runnable
	{
		private String input;
		private String output;
		public ParseFile(String input, String output)
		{
			this.input = input;
			this.output = output;
		}
		@Override
		public void run() 
		{
			Read.byLine(input, new Parse(output));
		}
		
	}
	private int fastForward = 0; //2.5m on reg, 1.01-1.8 on mod
	class Parse implements LineOperation
	{
		int numLine = 0;
		BufferedWriter outputWriter;
		public Parse(String output)
		{
			try
			{
				outputWriter = new BufferedWriter(new FileWriter(output));
			}
			catch(IOException e) { e.printStackTrace(); }
		}
		public void read(String line)
		{
			numLine ++;
			if(fastForward > numLine)
			{
				return;
			}
			if(numLine % 1000 == 0)
			{
				System.out.println(numLine);
			}
			if(mod)
			{
				String[] tokens = line.trim().split(" ");
				int removeIndex = rnd.nextInt(tokens.length);
				tokens = ArrayUtils.cutout(tokens, removeIndex);
				line = ListUtils.join(Arrays.asList(tokens), " ");
			}
			Tree tree = parser.parse(line);
			if(tree != null)
			{
				try
				{
					outputWriter.write(tree.toString());
					outputWriter.newLine();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
