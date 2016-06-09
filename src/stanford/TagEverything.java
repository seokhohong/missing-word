package stanford;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class TagEverything 
{
	private StanfordTagger tagger = new StanfordTagger();
	public static void main(String[] args)
	{
		new TagEverything().go();
	}
	private void go()
	{
		Multitasker multitasker = new Multitasker();
		List<Tag> tags = new ArrayList<>();
		for(int a = 0; a < 5; a++)
		{
			Tag tag = new Tag("C:/MissingWord/train/corpusPart"+a+".txt", "C:/MissingWord/train/taggedPart"+a+".txt");
			tags.add(tag);
			multitasker.load(tag);
		}
		multitasker.done();
		for(Tag tag : tags)
		{
			try
			{
				tag.writer.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	class Tag implements LineOperation, Runnable
	{
		BufferedWriter writer;
		String input;
		public Tag(String input, String output)
		{
			this.input = input;
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
			try
			{
				writer.write(tagger.tag(line));
				writer.newLine();
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
		}
	}
}
