package pcfg;

import java.util.ArrayList;
import java.util.List;

import stanford.PCFGParser;
import utils.ListUtils;
import edu.stanford.nlp.trees.Tree;
import mwutils.Counter;

public class PCFGModel 
{
	Counter<String> lex = Counter.load("C:/MissingWord/PCFGRulesLexNoMarkov.txt");
	Counter<String> modLex = Counter.load("C:/MissingWord/PCFGModRulesLexNoMarkov.txt");
	public static void main(String[] args)
	{
		new PCFGModel().go();
	}
	private void go()
	{
		//String sentence = "Japan has suspended imports of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese .";
		String sentence = "Japan has suspended imports , after reports that high levels have been found.";
		//String mod = "Japan has suspended of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese .";
		String mod = "Japan has suspended , after reports that high levels have been found.";
		//String sentence = "If no candidate wins an absolute majority , there will be a runoff between the top two contenders , most likely in mid-October .";
		PCFGParser parser = new PCFGParser();
		Tree tree = parser.parse(sentence);
		Tree modTree = parser.parse(mod);
		parser = null;
		System.gc();
		computeLikelihood(tree);
		computeLikelihood(modTree);
	}
	private void computeLikelihood(Tree tree)
	{
		List<PCFGGrammarRule> rules = PCFGGrammarRule.from(tree);
		List<Integer> counts = new ArrayList<>();
		List<Double> probs = new ArrayList<>();
		List<Integer> modCounts = new ArrayList<>();
		List<Double> modProbs = new ArrayList<>();
		for(PCFGGrammarRule rule : rules)
		{
			counts.add(lex.getCount(rule.lexNoMarkov()));
			probs.add(lex.getProb(rule.lexNoMarkov()));
		}
		
		for(PCFGGrammarRule rule : rules)
		{
			modCounts.add(modLex.getCount(rule.lexNoMarkov()));
			modProbs.add(modLex.getProb(rule.lexNoMarkov()));
		}
		for(int a = 0; a < rules.size(); a++)
		{
			System.out.println(rules.get(a).lexNoMarkov());
			System.out.println(counts.get(a)+" "+modCounts.get(a));
			System.out.println(probs.get(a)+" "+modProbs.get(a));
			System.out.println();
		}
		System.out.println(ListUtils.sum(probs)+" "+ListUtils.sum(modProbs));
	}
}
