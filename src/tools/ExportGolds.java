package tools;

import java.util.ArrayList;
import java.util.List;

import utils.Read;
import utils.Write;
import mwutils.TaggedSentence;

public class ExportGolds 
{
	public static void main(String[] args)
	{
		List<String> taggedSentences = loadGold("C:/MissingWord/gold.txt", 15);
		System.out.println(taggedSentences.size());
		Write.to("sentences.txt", taggedSentences);
	}
	private static List<String> loadGold(String filename, int length)
	{
		List<String> lines = Read.from(filename);
		List<String> tagged = new ArrayList<>();
		for(String line : lines)
		{
			TaggedSentence sentence = TaggedSentence.fromTaggedLine(line.split("@")[0]);
			if(sentence.length() == length)
			{
				tagged.add(line.split("@")[0]);
			}
		}
		return tagged;
	}
}
