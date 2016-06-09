package parseGram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import stanford.PCFGParser;
import utils.ArrayUtils;
import utils.ListUtils;

public class MakeParserGrams 
{
	Random rnd = new Random();
	boolean remove = false;
	public static void main(String[] args)
	{
		new MakeParserGrams().go();
	}
	private void go()
	{
		PCFGParser parser = new PCFGParser();
		try
		{
			BufferedReader corpusReader = new BufferedReader(new FileReader("C:/MissingWord/train/cleanTokensPart3.txt"));
			BufferedWriter gramsWriter = new BufferedWriter(new FileWriter("C:/MissingWord/parseGramsPart3.txt"));
			String line;
			int index = 0;
			while((line = corpusReader.readLine()) != null)
			{
				index ++;
				if(index % 10000 == 0)
				{
					System.out.println(index);
				}
				if(line.trim().length() == 0) continue;
				String[] tokens = line.trim().split(" ");
				if(remove)
				{
					int removeIndex = rnd.nextInt(tokens.length);
					tokens = ArrayUtils.cutout(tokens, removeIndex);
				}
				List<String> grams = parser.lex(ListUtils.join(Arrays.asList(tokens), " "));
				for(String stringGram : grams)
				{
					ParseGramWindow window = new ParseGramWindow(stringGram);
					window.removeWord();
					window.removeMarkovOrder(1);
					gramsWriter.write(window+"\n");
				}
			}
			gramsWriter.close();
			corpusReader.close();
		}
		catch(IOException e)
		{
			
		}
	}
}
