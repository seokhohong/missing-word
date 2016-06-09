package vertical;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;

public class VerticalElem 
{
	String[] markov = new String[2];
	String head;
	Tree node;								public Tree getNode() { return node; }
	
	public VerticalElem(Tree root, Tree node)
	{
		this.node = node;
		Tree parent = node.parent(root);
		if(parent != null)
		{
			markov[0] = parent.label().value();
			Tree grandparent = parent.parent(root);
			if(grandparent != null)
			{
				markov[1] = grandparent.label().value();
			}
		}
		head = node.label().value();
	}
	public static List<VerticalElem> makeListFrom(Tree root)
	{
		List<VerticalElem> list = new ArrayList<>();
		for(Tree node : root.subTrees())
		{
			VerticalElem elem = new VerticalElem(root, node);
			list.add(elem);
		}
		return list;
	}
	private static final String DELIM = "/";
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = markov.length; a --> 0; )
		{
			if(markov[a] != null)
			{
				builder.append(markov[a]);
			}
			else
			{
				builder.append("null");
			}
			builder.append("@");
		}
		builder.append(head);
		return builder.toString();
	}
}
