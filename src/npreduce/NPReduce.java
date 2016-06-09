package npreduce;

import java.util.ArrayList;
import java.util.List;

import utils.ListUtils;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Tag;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;

public class NPReduce 
{
	public static List<String> foldNP(Tree tree, int length)
	{
		List<String> tagSequences = new ArrayList<>();
		List<Tree> folded = new ArrayList<>();
		for(Tree node : tree.preOrderNodeList())
		{
			if(node.label().toString().equals("NP"))
			{
				folded.add(fold(tree, node));
			}
		}
		for(Tree fold : folded)
		{
			tagSequences.addAll(makeTagSequences(fold, length));
		}
		return tagSequences;
	}
	private static List<String> makeTagSequences(Tree tree, int windowSize)
	{
		List<String> sequences = new ArrayList<>();
		List<Label> preterms = tree.preTerminalYield();
		
		int center = 0;
		for(int a = 0; a < preterms.size(); a++)
		{
			if(preterms.get(a).value().equals("NP"))
			{
				center = a;
			}
		}
		
		for(int a = center - windowSize + 1; a < center + 1; a++)
		{
			List<String> tokens = new ArrayList<>();
			for(int b = 0; b < windowSize; b ++)
			{
				if(a + b < 0 || a + b >= preterms.size())
				{
					tokens.add(" ");
				}
				else
				{
					tokens.add(preterms.get(a + b).value());
				}
			}
			sequences.add(ListUtils.join(tokens, ","));
		}
		return sequences;
	}
	private static Tree fold(Tree root, Tree toFold)
	{
		Tree newTree = root.deepCopy();
		int foldIndex = toFold.nodeNumber(root);
		Tree targetFold = newTree.getNodeNumber(foldIndex);
		targetFold.setLabel(new Tag("NP"));
		int numChildren = targetFold.numChildren();
		for(int a = 0; a < numChildren; a++)
		{
			targetFold.removeChild(0);
		}
		targetFold.addChild(new LabeledScoredTreeNode(new Word("NPR-Word")));
		//targetFold.addChild(new LabeledScoredTreeNode(new Tag("NP Reduced")));
		return newTree;
	}
}
