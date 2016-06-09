package dep;

import java.io.IOException;

import mwutils.Counter;
import utils.Read;
import utils.Read.LineOperation;

public class DependencyUnigram 
{
	public static void main(String[] args)
	{
		DependencyUnigram.make();
	}
	public DependencyUnigram()
	{
		
	}
	public static void make()
	{
		DepCounterOperation depCounter = new DepCounterOperation();
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("C:/MissingWord/train/depPart"+a+".txt", depCounter);
		}
		depCounter.depTerms.threshold(50);
		depCounter.depTerms.export("C:/MissingWord/dep/commonUnigram50.txt");
	}
	static class DepCounterOperation implements LineOperation
	{
		Counter<String> depTerms = new Counter<>();
		int numLine = 0;
		@Override
		public void read(String line) 
		{
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine);
			}
			numLine ++;
			String[] depParts = line.split("@");
			for(String depPart : depParts)
			{	
				try
				{
					Dependency dep = Dependency.parse(depPart);
					depTerms.add(dep.getHeadChunk());
					depTerms.add(dep.getDepChunk());
				}
				catch(Exception e) {}
			}
		}
		
	}
}
