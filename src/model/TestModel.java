package model;

import global.PCFGSelectImprovementModel;

import java.util.Collections;
import java.util.List;

import partialTag.NPartialLexModel;
import lexbigram.BigramModel;
import lexbigram.TrigramModel;
import lexbigram.TrigramRarityModel;
import lexbigram.UnigramModel;
import mwutils.TaggedSentence;
import sideTags.BidirTagsModel;
import stanford.StanfordTagger;
import tag.NTagModel;
import tag.NTagReplacerModel;
import utils.ArrayUtils;

public class TestModel 
{
	public static void main(String[] args)
	{
		new TestModel().go();
	}
	private void go()
	{
		//Model model = new NPartialLexModel(5, 3, 5);
		//Model model = new BigramModel();
		//Model model = new NTagModel(4, 2);
		//Model model = new BidirTagsModel(1, 1, 1);
		//Model model = new NTagReplacerModel(5, 4);
		//Model model = new UnigramModel();
		Model model = new PCFGSelectImprovementModel();
		
		//String sentence = "The majority be of the standard 6X6 configuration for carrying personnel .";
		
		//Semantic: I_PRP read_VBP them_PRP every_DT time_NN I_PRP passed_VBD and_CC thought_VBD of_IN it_PRP until_IN that_DT evening_NN ._.
		//TaggedSentence tagged = new StanfordTagger().tagSentence(sentence);
		
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("They_PRP 've_VBP always_RB had_VBN the_DT ability_NN to_TO evolve_VB and_CC providing_VBG definitions_NNS on_IN their_PRP$ own_JJ ._.");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("Neither_CC will_MD the_DT fans_NNS who_WP Coors_NNP in_IN the_DT past_JJ month_NN ._.");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("Sales_NNS during_IN the_DT quarter_NN rose_VBD 10_CD percent_NN to_TO $_$ 1.31_CD billion_CD ,_, the_DT company_NN ._.");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("Bank_NNP of_IN America_NNP said_VBD does_VBZ not_RB comment_VB on_IN the_DT specifics_NNS of_IN any_DT particular_JJ customer_NN ._.");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("The_DT Speaker_NNP told_VBD MPs_NNS he_PRP was_VBD ''_'' disappointed_JJ ''_'' the_DT expense_NN details_NNS became_VBD public_JJ ._.");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("In_IN Egypt_NNP ,_, citizens_NNS are_VBP required_VBN to_TO their_PRP$ personal_JJ ID_NN cards_NNS at_IN all_DT times_NNS ._.");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("He_PRP has_VBZ seen_VBN only_RB rarely_RB since_IN ,_, in_IN rebel_NN videos_NNS filmed_VBN during_IN his_PRP$ captivity_NN ._.");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("His_PRP$ ERA_NN is_VBZ in_IN 17_CD games_NNS ,_, and_CC he_PRP has_VBZ allowed_VBN 11_CD earned_VBN runs_NNS ._.");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("Notes_NNS :_: Dotel_NNP had_VBD just_RB two_CD homers_NNS in_IN 33_CD 1-3_CD innings_NNS entering_VBG the_DT game_NN ..._:");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("Thousand-year-old_JJ ethnic_JJ and_CC religious_JJ grievances_NNS have_VBP occasionally_RB intruded_VBN into_IN politics_NNS of_IN business_NN association_NN meetings_NNS ._.");
		//TaggedSentence tagged = TaggedSentence.fromTaggedLine("Her_PRP$ parents_NNS found_VBN out_IN about_IN her_PRP$ arrest_NN in_IN a_DT 10_CD phone_NN call_NN from_IN her_PRP ._.");
		TaggedSentence tagged = TaggedSentence.fromTaggedLine("Others_NNS were_VBD asking_VBG about_IN postoperative_JJ recovery_NN and_CC ,_, while_IN four_CD patients_NNS were_VBD seeking_VBG reassurance_NN ._.");
		List<List<float[]>> data = model.process(Collections.singletonList(tagged));
		
		String[] tokens = tagged.getTokens();
		for(int a = 0; a < tokens.length; a++)
		{
			System.out.println(tokens[a]+"\t"+ArrayUtils.print(data.get(0).get(a)));
		}
	}
}
