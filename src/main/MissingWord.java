package main;

public class MissingWord 
{
	public static final int NUM_WORDS = 800570577;
	/*
	Alternative models to consider:
	Handling Questions
	Quotation Model
	Comma Removal?
	Removal of PRN (parenthetical clauses)
	Object Searcher model
	*/
	
	/*
	 * Notes on results of model 2/17
	 * Dependency parser could be extremely powerful
	 * 'a extra' should be flagged. (Fixed by fixing integer overflow in unigram counter?)
	 * fix pos tags that were thrown off by missing words
	 */
	
	/*
	 * Speech Errors
	 * ''_'' It_PRP is_VBZ clearly_RB more_RBR difficult_JJ for_IN us_PRP now_RB ,_, ''_'' the_DT Spanish_JJ coach_NN ._.
	 */
	
	/*
	 * Dependency Error
	 * Only_RB 56_CD of_IN this_DT group_NN took_VBD their_PRP$ rights_NNS and_CC responsibilities_NNS seriously_RB and_CC actually_RB voted_VBD ._.
	 * In_IN one_CD belonging_VBG to_TO an_DT army_NN general_JJ named_VBN Vir-Hugo_NNP Lherrison_NNP ,_, they_PRP buried_VBD 80_CD ._.
	 * It_PRP will_MD not_RB be_VB enough_JJ to_TO simply_RB steer_VB more_JJR federal_JJ grant_NN to_TO these_DT communities_NNS ._.
	 * Witnesses_NNS have_VBP reported_VBN seeing_VBG nine_CD mattresses_NNS being_VBG delivered_VBN ,_, although_IN accommodation_NN has_VBZ not_RB advertised_VBN ._.
	 * The_DT state_NN restricts_VBZ public_JJ smoking_NN and_CC a_DT cigarette_NN tax_NN of_IN $_$ 2_CD per_IN pack_NN ._.
	 */
	
	/*
	 * I should be getting these? Need more/stronger models?:
	 * THE_DT next_JJ time_NN anyone_NN tells_VBZ you_PRP racing_NN 's_POS bent_JJ ,_, ask_VB them_PRP prove_VB it_PRP ._.
	 * Her_PRP$ output_NN of_IN information_NN from_IN the_DT Healy_NNP eclipsed_VBD output_NN of_IN all_DT us_PRP professional_JJ journalists_NNS ._.
	 * It_PRP began_VBD with_IN notable_JJ cold_JJ ,_, and_CC remains_VBZ of_IN the_DT coolest_JJS summers_NNS on_IN record_NN ._.
	 * But_CC despite_IN appearing_VBG knowledgeable_JJ about_IN wine_NN ,_, the_DT majority_NN choose_VB to_TO ignore_VB its_PRP$ risks_NNS ._. (it guessed 9)
	 */
	
	/*
	 * Remove Parentheticals
	 * And_CC Keynes_NNP ,_, like_IN FDR_NNP ,_, he_PRP met_VBD and_CC admired_VBD ,_, had_VBD been_VBN right_RB ._.
	 * 
	 */
	
	/*
	 * Cut subordinate clauses (maybe non-intermediate subordinate clauses?)
	 * A_DT key_JJ House_NNP Democrat_NNP that_IN the_DT F-22_NN funding_NN increases_NNS will_MD survive_VB a_DT veto_NN threat_NN ._. <-- Not possible to cut with PCFG
	 * 
	 * 
	 */
	
	/*
	 * Correctable by simple run through a constituent parser looking for a complete sentence
	 * Physics_NN students_NNS even_RB more_RBR upset_JJ because_IN they_PRP need_VBP data_NNS from_IN experiments_NNS for_IN their_PRP$ theses_DT ._.
	 * 
	 */
	
	/*
	 * FML!!! They have bad grammar!
	 * ''_'' Sergeant_NNP Domingo_NNP already_RB established_VBN a_DT relationship_NN with_IN Sergeant_NNP Shannon_NNP ,_, ''_'' he_PRP said_VBD ._. (guessed 3, is 4)
	 * The_DT problem_NN was_VBD ,_, Orange_NNP County_NNP law_NN limited_VBD campaign_NN donations_NNS to_TO 1,000_CD per_IN person_NN ._.
	 */
	
	/*
	 * It gets these right?
	 * This_DT is_VBZ a_DT clear_JJ sign_NN that_IN banks_NNS are_VBP to_TO do_VB business_NN with_IN each_DT other_JJ ._.
	 * 
	 * 
	 */
}
