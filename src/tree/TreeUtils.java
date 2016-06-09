package tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import stanford.PCFGParser;
import edu.stanford.nlp.trees.Tree;

public class TreeUtils 
{
	public static void main(String[] args)
	{
		PCFGParser parser = new PCFGParser();
		Tree tree = parser.parse("Japan has suspended imports of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese .");
		System.out.println(TreeUtils.separateByS(tree));
	}
	/**
	 * Removes prepositions, RB, JJ 
	 * */
	public static Tree removeDetails(Tree tree)
	{
		for(Tree node : tree.preOrderNodeList())
		{
			List<Integer> toRemove = new ArrayList<>();
			for(int a = 0; a < node.numChildren(); a++)
			{
				Tree child = node.getChildrenAsList().get(a);
				if((child.label().value().equals("PP") && child.depth() <= 3) || child.label().value().equals("RB"))
				{
					toRemove.add(a);
				}
			}
			Collections.sort(toRemove);
			for(int a = toRemove.size(); a --> 0;)
			{
				node.removeChild(toRemove.get(a));
			}
		}
		return tree;
	}
	private static void cleanS(Tree tree)
	{
		for(int a = tree.children().length; a --> 0; )
		{
			if(tree.children()[a].label().toString().equals("S") || tree.children()[a].label().toString().equals("SBAR"))
			{
				tree.removeChild(a);
			}
			else
			{
				cleanS(tree.children()[a]);
			}
		}
	}
	public static List<Tree> separateByS(Tree tree)
	{
		Tree punctuation = tree.lastChild().deepCopy();
		List<Tree> sentences = new ArrayList<>();
		for(Tree child : tree.preOrderNodeList())
		{
			if(child.label().toString().equals("S"))
			{
				sentences.add(child);
			}
		}

		for(Tree sentence : sentences)
		{
			cleanS(sentence);
			if(!sentence.lastChild().equals(punctuation))
			{
				sentence.addChild(punctuation);
			}
		}
		return sentences;
	}
	public static int numNodes(Tree tree)
	{
		int numNodes = tree.numChildren();
		for(Tree child : tree.children())
		{
			numNodes += numNodes(child);
		}
		return numNodes;
	}
}
