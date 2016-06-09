package stanford;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import utils.ListUtils;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;

public class PCFGParser implements ConstituencyParser 
{
	public static void main(String[] args)
	{
		PCFGParser parser = new PCFGParser();
		Tree tree = parser.parse("Japan has suspended imports of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese .");
	}
	MaxentTagger tagger;
	LexicalizedParser lp;
	public PCFGParser()
	{
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		String taggerPath = "C:/MissingWord/models/english-left3words-distsim.tagger";

		tagger = new MaxentTagger(taggerPath);
		lp = LexicalizedParser.loadModel(parserModel);
	}
	public Tree parse(String text)
	{
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
		for (List<HasWord> sentence : tokenizer) 
		{
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			return lp.apply(tagged);
		}
		return null;
	}
	public List<String> lex(String text)
	{
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
		for (List<HasWord> sentence : tokenizer) 
		{
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			Tree tree = lp.apply(tagged);
			return makeNGrams(tree, 5);
		}
		return null;
	}
	private List<String> makeNGrams(Tree tree, int n)
	{
		List<String> allNGrams = new ArrayList<>();
		for(int a = 1; a < tree.size(); a++)
		{
			ArrayList<String> curr = new ArrayList<>();
			if(!tree.getNodeNumber(a).isLeaf())
			{
				buildTrees(allNGrams, tree, a, curr, n, true);
			}
		}
		return allNGrams;
	}
	private void buildTrees(List<String> allNGrams, Tree root, int index, List<String> curr, int n, boolean add)
	{
		Tree node = root.getNodeNumber(index);
		if(node.isPreTerminal() && !add)
		{
			return;
		}
		if(node.isPreTerminal() || add)
		{
			curr.add(nodeInfo(node, root));
		}
		if(curr.size() == n)
		{
			allNGrams.add(assembleList(curr));
			return;
		}
		else
		{
			List<String> currCopy = ListUtils.copy(curr);
			if(!root.getNodeNumber(index).isPreTerminal())
			{
				notFolded(allNGrams, root, index, curr, n);
			}
			fold(allNGrams, root, index, currCopy, n);
		}
	}
	private void notFolded(List<String> allNGrams, Tree root, int index, List<String> curr, int n)
	{

		int nextNode = index + 1;
		if(nextNode < root.size())
		{
			List<String> currCopy = ListUtils.copy(curr);
			buildTrees(allNGrams, root, nextNode, curr, n, true);
			buildTrees(allNGrams, root, nextNode, currCopy, n, false);
		}
		else
		{
			allNGrams.add(assembleList(curr));
		}
	}
	private void fold(List<String> allNGrams, Tree root, int index, List<String> curr, int n)
	{
		int nextNode = deepestLastChild(root.getNodeNumber(index)).nodeNumber(root) + 1;
		if(nextNode < root.size())
		{
			List<String> currCopy = ListUtils.copy(curr);
			buildTrees(allNGrams, root, nextNode, curr, n, true);
			buildTrees(allNGrams, root, nextNode, currCopy, n, false);
		}
		else
		{
			allNGrams.add(assembleList(curr));
		}
	}
	private Tree deepestLastChild(Tree tree)
	{
		while(tree.children().length > 0)
		{
			tree = tree.lastChild();
		}
		return tree;
	}
	private String nodeInfo(Tree node, Tree root)
	{
		StringBuilder builder = new StringBuilder();
		if(node.parent(root) != null)
		{
			if(node.parent(root).parent(root) != null)
			{
				builder.append(node.parent(root).parent(root).label()+"|");
			}
			builder.append(node.parent(root).label()+"|");
		}

		if(node.isPreTerminal())
		{
			builder.append(node.children()[0].label());
		}
		else
		{
			builder.append(node.label());
		}
		return builder.toString();
	}
	private String assembleList(List<String> elems)
	{
		return ListUtils.join(elems, "~");
	}

	/*
	private int assignWords(Tree tree, List<HasWord> sentence, int index, Map<Tree, String> words)
	{
		if(tree.isLeaf())
		{
			words.put(tree, sentence.get(index).word());
			index ++;
		}
		else
		{
			for(Tree child : tree.children())
			{
				index = assignWords(child, sentence, index, words);
			}
		}
		return index;
	}
	 */
}
