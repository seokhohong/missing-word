package dep;

import java.util.HashMap;
import java.util.Map;

import mwutils.Counter;
import utils.Read;
import utils.Read.LineOperation;

public class ExamineSVO implements LineOperation
{
	//DepParser depParser = new DepParser();
	public static void main(String[] args)
	{
		new ExamineSVO().go();
	}
	private void go()
	{
		Read.byLine("C:/MissingWord/train/depPart2.txt", this);
		for(String key : subjects.keySet())
		{
			System.out.println(key+" "+subjects.get(key).getNormSS());
		}
	}
	private Map<String, Counter<String>> subjects = new HashMap<>();
	int numLine = 0;
	@Override
	public void read(String line) 
	{
		numLine ++;
		if(numLine % 10000 == 0)
		{
			System.out.println(numLine+" "+subjects.keySet().size());
		}
		String[] split = line.split("@");
		for(String elem : split)
		{
			try
			{
				Dependency dep = Dependency.parse(elem);
				if(dep.getRelation().contains("nsubj"))
				{
					if(!subjects.containsKey(dep.getDepToken()))
					{
						subjects.put(dep.getDepToken(), new Counter<>());
					}
					subjects.get(dep.getDepToken()).add(dep.getHeadToken());
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
}
