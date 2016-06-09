package pcfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import stanford.PCFGParser;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;

public class PCFGGrammarRule 
{
	public static void main(String[] args)
	{
		PCFGParser parser = new PCFGParser();
		Tree tree = parser.parse("Japan has suspended imports of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese .");
		System.out.println(from(tree));
	}
	private String head;
	private String[] children;
	private String headLex;
	private String[] childLex;
	private String[] parents;
	public PCFGGrammarRule(String head, String headLex, String[] children, String[] childLex, String[] parents)
	{
		this.head = head;
		this.headLex = headLex;
		this.children = children;
		this.childLex = childLex;
		this.parents = parents;
	}
	public static List<PCFGGrammarRule> from(Tree tree)
	{
		List<PCFGGrammarRule> rules = new ArrayList<>();
		HeadFinder headFinder = new CollinsHeadFinder();
		tree.percolateHeads(headFinder);
		for(Tree nonTerm : tree.preOrderNodeList())
		{
			if(!nonTerm.isPreTerminal() && !nonTerm.isLeaf())
			{
				String[] children = new String[nonTerm.numChildren()];
				String[] childLex = new String[nonTerm.numChildren()];
				for(int a = 0; a < nonTerm.children().length; a++)
				{
					children[a] = nonTerm.children()[a].label().value();
					childLex[a] = getHead(nonTerm.children()[a]);
				}
				String[] parents = new String[2];
				if(nonTerm.parent(tree) != null)
				{
					parents[0] = nonTerm.parent(tree).label().value();
					if(nonTerm.parent(tree).parent(tree) != null)
					{
						parents[1] = nonTerm.parent(tree).parent(tree).label().value();
					}
				}
				rules.add(new PCFGGrammarRule(nonTerm.label().value(), getHead(nonTerm), children, childLex, parents));
			}
		}
		return rules;
	}
	public static PCFGGrammarRule parse(String line) throws Exception
	{
		String[] split = line.split("\\\\");
		String[] parents = new String[2];
		parents[0] = split[0];
		parents[1] = split[1];
		String[] pairs = split[2].split("\\)");
		String head = "";
		String headLex = "";
		String[] children = new String[pairs.length - 1];
		String[] childLex = new String[pairs.length - 1];
		for(int a = 0; a < pairs.length; a++)
		{
			String[] tuple = pairs[a].split("\\(");
			if(a == 0)
			{
				head = tuple[0];
				headLex = tuple[1];
			}
			else
			{
				children[a - 1] = tuple[0];
				childLex[a - 1] = tuple[1];
			}
		}
		return new PCFGGrammarRule(head, headLex, children, childLex, parents);
	}
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < children.length; a++)
		{
			builder.append(children[a] +"("+childLex[a]+")");
		}
		return parents[0]+"\\"+parents[1]+"\\"+head+"("+headLex+")"+builder.toString();
	}
	public String headLexicalization()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < children.length; a++)
		{
			builder.append(children[a]);
		}
		return parents[0]+"\\"+parents[1]+"\\"+head+"("+headLex+")"+builder.toString();
	}
	public String noLex2Markov()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < children.length; a++)
		{
			builder.append(children[a]+"~");
		}
		return parents[0]+"\\"+parents[1]+"\\"+head+"()"+builder.toString();
	}
	public String noLex1Markov()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < children.length; a++)
		{
			builder.append(children[a]);
		}
		return parents[1]+"\\"+head+"()"+builder.toString();
	}
	public String lexNoMarkov()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < children.length; a++)
		{
			builder.append(children[a]);
		}
		return head+"("+headLex+")"+builder.toString();
	}
	private static String getHead(Tree node)
	{
		if(node.label() instanceof TaggedWord)
		{
			return ((TaggedWord) node.label()).word();
		}
		return ((CoreLabel) node.label()).getString(edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation.class);
	}
	public int hashCode()
	{
		return head.hashCode() * (headLex.hashCode() + 13) * (parents[0].hashCode() + 7);
	}
	public boolean equals(Object o)
	{
		if(o instanceof PCFGGrammarRule)
		{
			PCFGGrammarRule other = (PCFGGrammarRule) o;
			return other.head.equals(head) && other.headLex.equals(headLex) && Arrays.deepEquals(other.parents, parents) && Arrays.deepEquals(other.children, children) && Arrays.deepEquals(other.childLex, childLex);
		}
		return false;
	}
}
