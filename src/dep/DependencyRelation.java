package dep;

import java.util.List;

import mwutils.Counter;
import utils.Read;
import utils.Read.LineOperation;

public class DependencyRelation 
{
	public static void main(String[] args)
	{
		DependencyRelation.make();
	}
	public DependencyRelation()
	{
		
	}
	public static void make()
	{
		DepCounterOperation depCounter = new DepCounterOperation();
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("C:/MissingWord/train/depPart"+a+".txt", depCounter);
		}
		depCounter.depTerms.export("C:/MissingWord/dep/depRelationCounter.txt");
	}
	static class DepCounterOperation implements LineOperation
	{
		Counter<String> depTerms = new Counter<>();
		int numLine = 0;
		@Override
		public void read(String line) 
		{
			String[] depParts = line.split("@");
			for(String depPart : depParts)
			{	
				try
				{
					Dependency dep = Dependency.parse(depPart);
					depTerms.add(dep.getRelation());
				}
				catch(Exception e) {}
			}
		}
	}
}
