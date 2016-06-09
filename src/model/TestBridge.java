package model;

import utils.ArrayUtils;
import mwutils.TaggedSentence;

public class TestBridge 
{
	public static void main(String[] args)
	{
		new TestBridge().go();
	}
	private void go()
	{
		//TaggedSentence sentence = TaggedSentence.fromTaggedLine("During_IN his_PRP$ party_NN conference_NN Mr_NN Clegg_NNP called_VBN ''_'' savage_JJ cuts_NNS ._. ''_''\7\for_IN");
		String sentence = "During_IN his_PRP$ party_NN conference_NN Mr_NN Clegg_NNP called_VBN ''_'' savage_JJ cuts_NNS ._. ''_''/7/for_IN";
		System.out.println(ArrayUtils.print(sentence.split("~")));
		System.out.println(TaggedSentence.fromTaggedLine(sentence.split("~")[0]));
		System.out.println(TaggedSentence.fromTaggedLine(sentence.split("~")[0]).length());
	}
}
