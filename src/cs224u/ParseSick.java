package cs224u;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dep.DepParser;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import stanford.PCFGParser;
import utils.ListUtils;
import utils.Read;
import utils.Write;

public class ParseSick 
{
	public static void main(String[] args)
	{
		new ParseSick().dep("sicktrain.txt", "relationsTrainTyped.txt");
		new ParseSick().dep("sicktest.txt", "relationsTestTyped.txt");
		//new ParseSick().constituency("sicktrain.txt", "trainTrees.txt");
		//new ParseSick().constituency("sicktest.txt", "testTrees.txt");
	}
	private void constituency(String input, String output)
	{
		PCFGParser parser = new PCFGParser();
		List<String> lines = Read.from(input);
		List<String> outputLines = new ArrayList<>();
		for(String line : lines)
		{
			String[] components = line.split("`");
			String tree1 = parser.parse(components[0]).toString().replaceAll("\t", "");
			String tree2 = parser.parse(components[1]).toString().replaceAll("\t", "");
			//System.out.println(tree1);
			outputLines.add(tree1 + "|" + tree2);
		}
		Write.to(output, outputLines);
	}
	private void dep(String input, String output)
	{
		DepParser depParser = new DepParser();
		List<String> lines = Read.from(input);
		List<String> relationSet = new ArrayList<>();
		for(String line : lines)
		{
			String[] components = line.split("`");
			relationSet.add(exportRelnSet(relnExtraction(depParser.parse(components[0]))));
			relationSet.add(exportRelnSet(relnExtraction(depParser.parse(components[1]))));
		}
		Write.to(output, relationSet);
	}
	private String exportRelnSet(Map<Integer, List<DepRelation>> relation)
	{
		List<String> allElements = new ArrayList<>();
		for(Integer wordIndex : relation.keySet())
		{
			List<String> subrelations = new ArrayList<>();
			subrelations.add(Integer.toString(wordIndex));
			for(DepRelation dep : relation.get(wordIndex))
			{
				subrelations.add(dep.export());
			}
			allElements.add(ListUtils.join(subrelations, "`"));
		}
		return ListUtils.join(allElements, ";");
	}
	private Map<Integer, List<DepRelation>> relnExtraction(GrammaticalStructure gs)
	{
		Map<Integer, List<DepRelation>> depRelations = new HashMap<>();
		for(TypedDependency td : gs.allTypedDependencies())
		{
			String govWord = td.gov().word();
			String depWord = td.dep().word();
			if(!depRelations.containsKey(td.gov().index()))
			{
				depRelations.put(td.gov().index(), new ArrayList<>());
			}
			if(!depRelations.containsKey(depWord))
			{
				depRelations.put(td.dep().index(), new ArrayList<>());
			}
			depRelations.get(td.gov().index()).add(new DepRelation(td.dep(), td.reln().toString(), td.dep().index()));
			depRelations.get(td.dep().index()).add(new DepRelation(td.gov(), td.reln().toString(), td.gov().index()));
		}
		return depRelations;
	}
	private Map<String, String> wordToMarkovization(Tree tree)
	{
		Map<String, String> map = new HashMap<>();
		for(Tree leaf : tree.getLeaves())
		{
			map.put(leaf.label().value(), markov(tree, leaf, 3));
		}
		return map;
	}
	private String markov(Tree root, Tree leaf, int depth)
	{
		List<String> tags = new ArrayList<>();
		Tree currNode = leaf;
		for(int a = 0; a < depth; a++)
		{
			tags.add(currNode.parent(root).label().value());
			currNode = currNode.parent(root);
			if(currNode == null)
			{
				break;
			}
		}
		Collections.reverse(tags);
		return ListUtils.join(tags, "/");
	}
	class DepRelation
	{
		DepRelation(IndexedWord word, String reln, int index)
		{
			this.word = word;
			this.reln = reln;
			this.index = index;
		}
		IndexedWord word;
		String reln;
		int index = 0;
		public String toString()
		{
			return reln + " " + word.toString();
		}
		public String export()
		{
			return index+"|"+word.value()+"|"+reln.toString();
		}
	}
}
