package tree;

import java.util.Stack;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Tag;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;

public class ReconstructTree 
{
	public static void main(String[] args)
	{
		new ReconstructTree().go();
	}
	private void go()
	{
		try {
			System.out.println(reconstruct("(ROOT (S (NP (NP (NNP Brent) (NNP North) (NNP Sea) (NN crude)) (PP (IN for) (NP (NNP November) (NN delivery)))) (VP (VBD rose) (NP (CD 84) (NNS cents)) (PP (TO to) (NP (NP (CD 68.88) (NNS dollars)) (NP (DT a) (NN barrel))))) (. .)))"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Tree reconstruct(String line) throws Exception
	{
		Stack<Tree> curr = new Stack<>();
		Tree root = null;
		for(int a = 0; a < line.length(); a++)
		{
			char c = line.charAt(a);
			if(c == '(')
			{
				CoreLabel core = new CoreLabel();
				String tag = line.substring(a + 1, line.indexOf(" ", a + 1));
				core.setTag(tag);
				core.set(edu.stanford.nlp.ling.CoreAnnotations.CategoryAnnotation.class, tag);
				core.set(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class, tag);
				if(root == null)
				{
					root = new LabeledScoredTreeNode(core);
					curr.push(root);
				}
				else
				{
					Tree child = new LabeledScoredTreeNode(core);
					curr.peek().addChild(child);
					curr.push(child);
				}
			}
			if(c == ')')
			{
				if(line.charAt(a - 1) != ')')
				{
					Word word = new Word(line.substring(line.lastIndexOf(" ", a - 1) + 1, a));
					Tree child = new LabeledScoredTreeNode(word);
					curr.peek().addChild(child);
				}
				curr.pop();
			}
		}	
		return root;
	}
}
