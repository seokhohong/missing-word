package stanford;

import mwutils.TaggedSentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class StanfordTagger 
{
	MaxentTagger tagger;
	public StanfordTagger()
	{
		//String taggerPath = "models/english-bidirectional-distsim.tagger";
		String taggerPath = "C:/MissingWord/models/english-left3words-distsim.tagger";
		tagger = new MaxentTagger(taggerPath);
	}
	public String tag(String sentence)
	{
		return tagger.tagString(sentence);
	}
	public TaggedSentence tagSentence(String sentence)
	{
		String taggedString = tag(sentence);
		String[] split = taggedString.split(" ");
		String[] tags = new String[split.length];
		String[] tokens = new String[split.length];
		for(int a = 0; a < split.length; a++)
		{
			tokens[a] = split[a].split("_")[0];
			tags[a] = split[a].split("_")[1];
		}
		return new TaggedSentence(tokens, tags);
	}
	public String[] getTags(String sentence)
	{
		return getTagElement(sentence, 1);
	}
	public String[] getTokens(String sentence)
	{
		return getTagElement(sentence, 0);
	}
	private String[] getTagElement(String sentence, int elem)
	{
		String taggedString = tag(sentence);
		String[] split = taggedString.split(" ");
		String[] tags = new String[split.length];
		for(int a = 0; a < split.length; a++)
		{
			tags[a] = split[a].split("_")[elem];
		}
		return tags;
	}
}
