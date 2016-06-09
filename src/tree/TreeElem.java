package tree;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;

public class TreeElem 
{
	String[] markov = new String[2];
	String head;
	String[] children;
	Tree node;								public Tree getNode() { return node; }
	
	public TreeElem(Tree root, Tree node)
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
		children = new String[node.numChildren()];
		for(int a = 0; a < children.length; a++)
		{
			children[a] = node.getChild(a).label().value();
		}
	}
	public static List<TreeElem> makeListFrom(Tree root)
	{
		List<TreeElem> list = new ArrayList<>();
		for(Tree node : root.subTrees())
		{
			TreeElem elem = new TreeElem(root, node);
			list.add(elem);
		}
		return list;
	}
	private static final String DELIM = "@";
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < markov.length; a++)
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
		builder.append("@");
		for(int a = 0; a < children.length - 1; a++)
		{
			builder.append(children[a]);
			builder.append("@");
		}
		if(children.length > 0)
		{
			builder.append(children[children.length - 1]);
		}
		return builder.toString();
	}
}
