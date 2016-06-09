package tag;

import mwutils.Counter;
import mwutils.TaggedSentence;
import utils.Read;
import utils.Read.LineOperation;

public class MakeTagDict 
{
	private Counter<String> tagCounter = new Counter<>();
	public static void main(String[] args)
	{
		new MakeTagDict().go();
	}
	private void go()
	{
		Read.byLine("C:/MissingWord/train/taggedPart0.txt", new CountTags());
		tagCounter.threshold(100);
		tagCounter.export("C:/MissingWord/tags.txt");
	}
	class CountTags implements LineOperation
	{
		int numLines = 0;
		@Override
		public void read(String line) 
		{
			if(numLines % 10000 == 0)
			{
				System.out.println(numLines+" "+tagCounter.keySet().size());
			}
			TaggedSentence taggedSentence = TaggedSentence.fromTaggedLine(line);
			numLines ++;
			String[] tags = taggedSentence.getTags();
			for(String tag : tags)
			{
				tagCounter.add(tag);
			}
		}
	}
}
