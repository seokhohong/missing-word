package pcfg;

import java.util.Arrays;
import java.util.List;

import mwutils.Counter;
import utils.Read;
import utils.Read.LineOperation;

public class TallyPCFG 
{
	public static void main(String[] args)
	{
		new TallyPCFG().go();
	}
	private void go()
	{
		tally(Arrays.asList(new String[] {"C:/MissingWord/PCFGRules.txt"})).export("C:/MissingWord/PCFGRulesLexNoMarkov.txt");
		tally(Arrays.asList(new String[] {"C:/MissingWord/PCFGModRules.txt"})).export("C:/MissingWord/PCFGModRulesLexNoMarkov.txt");
	}
	public static Counter<String> tally(List<String> filenames)
	{
		Counter<String> counter = new Counter<>();
		LineCounter lineCounter = new LineCounter(counter);
		for(String filename : filenames)
		{
			Read.byLine(filename, lineCounter);
		}
		return counter;
	}
	static class LineCounter implements LineOperation
	{
		private Counter<String> counter;
		public LineCounter(Counter<String> counter)
		{
			this.counter = counter;
		}
		int numLine = 0;
		public void read(String line)
		{
			if(numLine % 100000 == 0)
			{
				System.out.println(numLine+" "+counter.keySet().size());
			}
			numLine ++;
			try
			{
				counter.add(PCFGGrammarRule.parse(line).lexNoMarkov());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
