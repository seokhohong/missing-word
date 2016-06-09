package stanford;

import edu.stanford.nlp.trees.Tree;

public interface ConstituencyParser 
{
	public Tree parse(String text);
}
