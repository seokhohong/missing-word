package parseGram;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import utils.ListUtils;
import utils.Read;
import utils.Read.LineOperation;

public class TransformParserGram 
{
	public static void main(String[] args)
	{
		new TransformParserGram().go();
	}
	private void go()
	{
		NoMarkov op = new NoMarkov("C:/MissingWord/parserModTagsPart3.txt");
		Read.byLine("C:/MissingWord/parseModGramsPart3.txt", op);
		op.close();
	}
	class NoMarkov implements LineOperation
	{
		BufferedWriter writer;
		public NoMarkov(String output)
		{
			try
			{
				writer = new BufferedWriter(new FileWriter(output));
			}
			catch(IOException e) 
			{
				e.printStackTrace();
			}
		}
		@Override
		public void read(String line) 
		{
			String[] grams = line.split(",");
			for(int a = 0; a < grams.length; a++)
			{
				String[] tokens = grams[a].split("\\|");
				if(tokens.length > 1)
				{
					grams[a] = tokens[1];
				}
			}
			try
			{
				writer.write(ListUtils.join(Arrays.asList(grams), ","));
				writer.newLine();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		void close()
		{
			try
			{
				writer.close();
			}
			catch(IOException e) {}
		}
	}
}
