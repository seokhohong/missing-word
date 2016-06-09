package correct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import learnModelWeights.MakeFeatures;
import main.Gold;
import main.GoldStandard;
import dep.DependencyCache;
import dep.DependencyModel;
import stanford.StanfordTagger;
import utils.Pickle;
import word2vec.SmoothedBigramModel;
import word2vec.SmoothedTrigramModel;
import correct.PCFGModel;

public class SmallModelTest 
{
	public static void main(String[] args)
	{
		new SmallModelTest().go();
	}
	private void go()
	{
		FixSet one = new FixSet(new String[]
				{
				"Juergen Weckherlin , a German businessman in Hong Kong , said people were little money because they can not make trips that require face-to-face contact .",
				"Juergen Weckherlin , a German businessman in Hong Kong , said people were giving money because they can not make trips that require face-to-face contact .",
				"Juergen Weckherlin , a German businessman in Hong Kong , said people were losing money because they can not make trips that require face-to-face contact .",
				"Juergen Weckherlin , a German businessman in Hong Kong , said people were made money because they can not make trips that require face-to-face contact .",
				"Juergen Weckherlin , a German businessman in Hong Kong , said people were 15 money because they can not make trips that require face-to-face contact .",
				"Juergen Weckherlin , a German businessman in Hong Kong , said people were several money because they can not make trips that require face-to-face contact .",
				"Juergen Weckherlin , a German businessman in Hong Kong , said people were no money because they can not make trips that require face-to-face contact ."
				}, 13);
		FixSet two = new FixSet( new String[]
				{
				"Gordon will join Luol Deng on the GB team ; their respective NBA teams , the Detroit Pistons and the Chicago Bulls , play tonight .",
				"Gordon will join Luol Deng control the GB team ; their respective NBA teams , the Detroit Pistons and the Chicago Bulls , play tonight .",
				"Gordon will join Luol Deng in the GB team ; their respective NBA teams , the Detroit Pistons and the Chicago Bulls , play tonight .",
				"Gordon will join Luol Deng vs. the GB team ; their respective NBA teams , the Detroit Pistons and the Chicago Bulls , play tonight .",
				"Gordon will join Luol Deng with the GB team ; their respective NBA teams , the Detroit Pistons and the Chicago Bulls , play tonight .",
				"Gordon will join Luol Deng from the GB team ; their respective NBA teams , the Detroit Pistons and the Chicago Bulls , play tonight .",
				"Gordon will join Luol Deng many the GB team ; their respective NBA teams , the Detroit Pistons and the Chicago Bulls , play tonight .",
				}, 5);
		FixSet three = new FixSet( new String[]
				{
				"Buddy Piszel, Freddie Mac's chief financial officer, said the regulator 's move would allow it to buy morgage securities at attractive prices.",
				"Buddy Piszel, Freddie Mac's chief financial officer, said the regulator 5 move would allow it to buy morgage securities at attractive prices.",
				"Buddy Piszel, Freddie Mac's chief financial officer, said the regulator confidence move would allow it to buy morgage securities at attractive prices.",
				"Buddy Piszel, Freddie Mac's chief financial officer, said the regulator to move would allow it to buy morgage securities at attractive prices.",
				"Buddy Piszel, Freddie Mac's chief financial officer, said the regulator athletics move would allow it to buy morgage securities at attractive prices.",
				}, 13);
		
		FixSet four = new FixSet( new String[]
				{
				"-LRB- UPI -RRB- -- The Chicago Cubs have fired hitting coach Von Joshua ? months after he replaced Gerald Perry , team officials said .",
				"-LRB- UPI -RRB- -- The Chicago Cubs have fired hitting coach Von Joshua four months after he replaced Gerald Perry , team officials said .",
				"-LRB- UPI -RRB- -- The Chicago Cubs have fired hitting coach Von Joshua ! months after he replaced Gerald Perry , team officials said .",
				"-LRB- UPI -RRB- -- The Chicago Cubs have fired hitting coach Von Joshua . months after he replaced Gerald Perry , team officials said ."
				}, 13);
		
		FixSet five = new FixSet( new String[]
				{
				"PINZOLO , Italy -LRB- AP -RRB- -- David Trezeguet is quitting international football after the confirmation of the reappointment of Raymond Domenech as national coach",
				"PINZOLO , Italy -LRB- AP -RRB- -- IRS Trezeguet is quitting international football after the confirmation of the reappointment of Raymond Domenech as national coach",
				"PINZOLO , Italy -LRB- AP -RRB- -- Michael Trezeguet is quitting international football after the confirmation of the reappointment of Raymond Domenech as national coach",
				"PINZOLO , Italy -LRB- AP -RRB- -- the Trezeguet is quitting international football after the confirmation of the reappointment of Raymond Domenech as national coach",
				"PINZOLO , Italy -LRB- AP -RRB- -- Ilya Trezeguet is quitting international football after the confirmation of the reappointment of Raymond Domenech as national coach",
				"PINZOLO , Italy -LRB- AP -RRB- -- Pro Trezeguet is quitting international football after the confirmation of the reappointment of Raymond Domenech as national coach"
				}
				, 7);
		
		FixSet six = new FixSet( new String[]
				{
				"White House press secretary Robert Gibbs said Obama would urge lawmakers to forgo part of their August recess to continue working on health care legislation.",
				"White House press secretary Robert Gibbs said Obama would urge lawmakers to forgo part of their August recess to continue working on health care law.",
				"White House press secretary Robert Gibbs said Obama would urge lawmakers to forgo part of their August recess to continue working on health care system.",
				"White House press secretary Robert Gibbs said Obama would urge lawmakers to forgo part of their August recess to continue working on health care overhaul.",
				"White House press secretary Robert Gibbs said Obama would urge lawmakers to forgo part of their August recess to continue working on health care reform.",
				}
				, 24);
		List<Gold> golds = new GoldStandard(15, 300200).getGolds().subList(300000, 300200);
		FixOptions fixOptions = ((Map<Gold, FixOptions>) Pickle.load("C:/MissingWord/testFixOptions.ser")).get(golds.get(175));
		DependencyCache cache = new DependencyCache("15test", golds);
		List<AdvancedReplacer> models = new ArrayList<>();
		
		//models.add(new BidirTagsReplacer(1, 1, 2));
		//models.add(new NTagReplacer(5, 1));
		//models.add(new GlobalBigramModel());
		models.add(new PCFGModel(true));
		//models.add(new DependencyModel(1));
		//models.add(new DependencyModel(Arrays.asList(new String[] {"pobj"}), true, cache));
		//models.add(new TrigramModel());
		
		for(AdvancedReplacer model : models)
		{
			model.load();
			model.reweight(fixOptions, 1);
			fixOptions.recalibrate();
			List<WordScore> scores = new ArrayList<>();
			System.out.println(fixOptions.getGold());
			for(String word : fixOptions.getFixWords())
			{
				scores.add(new WordScore(word, fixOptions.getScore(word)));
			}
			Collections.sort(scores);
			for(WordScore score : scores)
			{
				System.out.println(score.word+" "+score.score);
			}
			System.out.println();
			model.unload();
		}
		System.out.println(MakeFeatures.getSmoothLabel(fixOptions));
	}
	static public class WordScore implements Comparable<WordScore>
	{
		private String word;			public String getWord() { return word; }
		private double score;			public double getScore() { return score; }
		WordScore(String word, double score)
		{
			this.word = word;
			this.score = score;
		}
		public int compareTo(WordScore other)
		{
			return Double.compare(this.score, other.score);
		}
	}
	StanfordTagger tagger = new StanfordTagger();
	class FixSet
	{
		List<FixEntry> fixEntries = new ArrayList<>();
		int fixIndex;
		FixSet(String[] arr, int fixIndex)
		{
			for(String val : arr)
			{
				fixEntries.add(new FixEntry(tagger.tagSentence(val), fixIndex));
			}
			this.fixIndex = fixIndex;
		}
	}
}

/*
Wtf like really?
products_NNS	-5.238952124294717
package_NN	-6.39662026426975
centers_NNS	-6.319603533776105
coverage_NN	-4.1415034166922275
fraud_NN	-6.950606871130431
services_NNS	-3.9274213648918437
crisis_NN	-6.728755736132191
facilities_NNS	-5.1048762499851765
decisions_NNS	-6.832430247861036
insurance_NN	-5.626262439243298
law_NN	-5.153383244154896
plan_NN	-3.0151757341205396
debate_NN	-4.107024837939125
reform_NN	0.0
systems_NNS	-6.039852738796777
bills_NNS	-4.578695474031826
providers_NNS	-3.7117525383087724
workers_NNS	-4.750761597235532
overhaul_NN	-2.3448779566922333
organizations_NNS	-6.3295960841686
center_NN	-6.926528404421093
unit_NN	-5.834186552961267
program_NN	-5.090511017871952
programs_NNS	-5.004273500419569
rationing_NN	-6.638237554454075
agenda_NN	-6.290125883623177
industry_NN	-4.91432539527011
needs_NNS	-6.489363772665101
bill_NN	-1.582631441019556
plans_NNS	-5.11833332091931
costs_NNS	-2.226810879895572
disparities_NNS	-6.912912560458127
reforms_NNS	-5.434303422054143
expenses_NNS	-6.511747820079152
trusts_NNS	-6.657322249421785
issues_NNS	-5.689831593890789
professionals_NNS	-5.226407925587561
benefits_NNS	-4.344047661382087
system_NN	-1.5005673382486489
proposals_NNS	-5.6327510745492795
policy_NN	-5.965712003906337
legislation_NN	-2.0145280474925134
spending_NN	-6.154219925777829

Comparing!
legislation rerm 1 White_NNP House_NNP press_NN secretary_NN Robert_NNP Gibbs_NNP said_VBD Obama_NNP would_MD urge_VB lawmakers_NNS to_TO forgo_VB part_NN of_IN their_PRP$ August_NNP recess_NN to_TO continue_VB working_VBG on_IN health_NN care_NN ._.
1.0
*/