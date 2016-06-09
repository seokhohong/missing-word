package vertical;

import edu.stanford.nlp.trees.Tree;
import mwutils.Counter;
import tree.ReconstructTree;
import tree.TreeElem;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class MakeVerticalDict 
{
	public static void main(String[] args)
	{
		new MakeVerticalDict().make();
	}
	private void make()
	{
		Multitasker multitasker = new Multitasker(5);
		Counter<String> verticalDict = new Counter<>();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new VerticalCounter("D:/MissingWord/train/parsePart"+a+".txt", verticalDict));
		}
		multitasker.done();
		verticalDict.export("C:/MissingWord/vertical/verticalDict.txt");
	}
	class VerticalCounter implements Runnable, LineOperation
	{
		private String parseFile;
		private Counter<String> verticalDict;
		private int numLine = 0;
		public VerticalCounter(String parseFile, Counter<String> verticalDict)
		{
			this.parseFile = parseFile;
			this.verticalDict = verticalDict;
		}
		@Override
		public void read(String line) 
		{
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+verticalDict.keySet().size());
			}
			try
			{
				Tree root = ReconstructTree.reconstruct(line);
				for(Tree node : root.subTrees())
				{
					if(node.isPreTerminal())
					{
						VerticalElem elem = new VerticalElem(root, node);
						verticalDict.add(elem.toString());
					}
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			numLine ++;
		}
		@Override
		public void run() 
		{
			Read.byLine(parseFile, this);
		}
	}
}
