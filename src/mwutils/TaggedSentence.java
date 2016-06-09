package mwutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.ListUtils;
import utils.Read;
import utils.Read.LineOperation;

public class TaggedSentence 
{
	private String[] tokens;							public String[] getTokens() { return tokens; }
	private String[] tags;								public String[] getTags() { return tags; }
	
	public TaggedSentence(String[] tokens, String[] tags)
	{
		this.tokens = tokens;
		this.tags = tags;
	}
	public int length()
	{
		return tokens.length;
	}
	public String getToken(int i)
	{
		return Utils.getElem(tokens, i);
	}
	public String getTag(int i)
	{
		return Utils.getElem(tags, i);
	}
	public String[] getChunks()
	{
		String[] chunks = new String[tokens.length];
		for(int a = 0; a < tokens.length; a++)
		{
			chunks[a] = tokens[a] + "_" + tags[a];
		}
		return chunks;
	}
	public static List<TaggedSentence> load(String filename)
	{
		ReadTaggedSentences reader = new ReadTaggedSentences();
		Read.byLine(filename, reader);
		return reader.sentences;
	}
	public String toString()
	{
		return ListUtils.join(Arrays.asList(tokens), " ");
	}
	public String toTaggedString()
	{
		List<String> chunks = new ArrayList<>();
		for(int a = 0; a < tokens.length; a++)
		{
			chunks.add(tokens[a]+"_"+tags[a]);
		}
		return ListUtils.join(chunks, " ");
	}
	public static TaggedSentence fromTaggedLine(String taggedLine) throws IllegalArgumentException
	{
		String[] split = taggedLine.split(" ");
		String[] tags = new String[split.length];
		String[] tokens = new String[split.length];
		for(int a = 0; a < split.length; a++)
		{
			try
			{
				if(split[a].equals("_")) //ugh
				{
					tokens[a] = "";
					tags[a] = "";
				}
				else
				{
					tokens[a] = split[a].split("_")[0];
					tags[a] = split[a].split("_")[1];
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		return new TaggedSentence(tokens, tags);
	}
	static class ReadTaggedSentences implements LineOperation
	{
		List<TaggedSentence> sentences = new ArrayList<>();
		@Override
		public void read(String line) 
		{
			sentences.add(fromTaggedLine(line));
		}
		
	}
}
