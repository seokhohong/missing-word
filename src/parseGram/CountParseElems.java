package parseGram;

import parseGram.ParseEverything.ParseFile;
import mwutils.Counter;
import edu.stanford.nlp.trees.Tree;
import tree.ReconstructTree;
import tree.TreeElem;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;

public class CountParseElems 
{
	public static void main(String[] args)
	{
		new CountParseElems().test();
	}
	private void test()
	{
		Counter<String> treeElems = new Counter<String>();
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new ParseElemCounter("D:/MissingWord/train/parsePart"+a+".txt", treeElems));
		}
		multitasker.done();
		treeElems.export("C:/MissingWord/parseElems.txt");
	}
	class ParseElemCounter implements Runnable, LineOperation
	{
		private String filename;
		private Counter<String> counter;
		private int numLine = 0;
		ParseElemCounter(String filename, Counter<String> counter)
		{
			this.filename = filename;
			this.counter = counter;
		}
		@Override
		public void read(String line) 
		{
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine+" "+counter.keySet().size());
			}
			try
			{
				Tree root = ReconstructTree.reconstruct(line);
				for(Tree node : root.subTrees())
				{
					TreeElem elem = new TreeElem(root, node);
					counter.add(elem.toString());
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
			Read.byLine(filename, this);
		}
		
	}
}
