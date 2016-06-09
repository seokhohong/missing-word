package tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import mwutils.TaggedSentence;
import utils.ListUtils;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class SeparateTagged 
{
	public static void main(String[] args)
	{
		new SeparateTagged().go();
	}
	private void go()
	{
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new Split("C:/MissingWord/train/taggedPart"+a+".txt", "C:/MissingWord/train/tagsPart"+a+".txt", "C:/MissingWord/train/tokensPart"+a+".txt"));
		}
		multitasker.done();
	}
	class Split implements Runnable, LineOperation
	{
		private String input;
		private BufferedWriter tags;
		private BufferedWriter tokens;
		public Split(String input, String tags, String tokens)
		{
			this.input = input;
			try
			{
				this.tags = new BufferedWriter(new FileWriter(tags));
				this.tokens = new BufferedWriter(new FileWriter(tokens));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		@Override
		public void run() 
		{
			Read.byLine(input, this);
			try
			{
				tags.close();
				tokens.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void read(String line) 
		{
			TaggedSentence tagged = TaggedSentence.fromTaggedLine(line);
			try
			{
				tags.write(ListUtils.join(Arrays.asList(tagged.getTags()), " "));
				tags.newLine();
				tokens.write(ListUtils.join(Arrays.asList(tagged.getTokens()), " "));
				tokens.newLine();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
}
