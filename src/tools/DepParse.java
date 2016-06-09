package tools;

import mwutils.TaggedSentence;
import dep.DepParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import opennlp.tools.parser.chunking.ParserEventStream;

public class DepParse 
{
	DepParser parser = new DepParser();
	public static void main(String[] args)
	{
		new DepParse();
	}
	public DepParse()
	{
		String[] toTag = new String[]{
				"Information_NNP regarding_VBG these_DT factors_NNS included_VBN in_IN our_PRP$ filings_NNS with_IN the_DT Securities_NNPS and_CC Exchange_NNP Commission_NNP ._.",
				"Information_NNP regarding_VBG these_DT factors_NNS are_VB included_VBN in_IN our_PRP$ filings_NNS with_IN the_DT Securities_NNPS and_CC Exchange_NNP Commission_NNP ._."
		};
		for(String str : toTag)
		{
			regDep(str);
			System.out.println();
		}
	}
	private void regDep(String taggedString)
	{
		TaggedSentence tagged = TaggedSentence.fromTaggedLine(taggedString);
		GrammaticalStructure gs = parser.parse(tagged.toString());
		for(TypedDependency dep : gs.allTypedDependencies())
		{
			System.out.println(dep);
		}
	}
}
