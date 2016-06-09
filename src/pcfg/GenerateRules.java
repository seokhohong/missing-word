package pcfg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.stanford.nlp.trees.Tree;
import tree.ReconstructTree;
import utils.Read;
import utils.Read.LineOperation;

public class GenerateRules 
{
	public static void main(String[] args)
	{
		new GenerateRules().go();
	}
	private void go()
	{
		RuleGenerator rg = new RuleGenerator("C:/MissingWord/pcfgRules.txt");
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("C:/MissingWord/train/parsePart"+a+".txt", rg);
		}
		RuleGenerator rgm = new RuleGenerator("C:/MissingWord/pcfgModRules.txt");
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("C:/MissingWord/train/parseModPart"+a+".txt", rgm);
		}
	}
	class RuleGenerator implements LineOperation
	{
		private BufferedWriter writer;
		public RuleGenerator(String output)
		{
			try
			{
				writer = new BufferedWriter(new FileWriter(output));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		@Override
		public void read(String line) 
		{
			try
			{
				Tree tree = ReconstructTree.reconstruct(line);
				List<PCFGGrammarRule> rules = PCFGGrammarRule.from(tree);
				for(PCFGGrammarRule rule : rules)
				{
					writer.write(rule.toString());
					writer.newLine();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
}
