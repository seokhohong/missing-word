package dep;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import utils.ArrayUtils;
import utils.ListUtils;
import utils.Multitasker;
import utils.Read;
import utils.Read.LineOperation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class DepAll 
{
	DepParser parser = new DepParser();
	private boolean mod = false;
	private Random rnd = new Random();
	public static void main(String[] args)
	{
		//new DepAll().go();
	}
	private void go()
	{
		Multitasker multitasker = new Multitasker();
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new ParseFile("D:/MissingWord/train/tokensPart"+a+".txt", "D:/MissingWord/train/depUncollapsedPart"+a+".txt", a));
		}
		multitasker.done();
	}
	class ParseFile implements Runnable
	{
		private String input;
		private String output;
		private int threadNum;
		public ParseFile(String input, String output, int threadNum)
		{
			this.input = input;
			this.output = output;
			this.threadNum = threadNum;
		}
		@Override
		public void run() 
		{
			Parse parse = new Parse(output, threadNum);
			Read.byLine(input, parse);
			try
			{
				parse.outputWriter.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	//private int fastForward = 5900000;
	private int fastForward = 0;
	class Parse implements LineOperation
	{
		
		int numLine = 0;
		BufferedWriter outputWriter; //TODO: CLOSE THIS GUY
		int threadNum;
		public Parse(String output, int threadNum)
		{
			this.threadNum = threadNum;
			try
			{
				outputWriter = new BufferedWriter(new FileWriter(output));
			}
			catch(IOException e) { e.printStackTrace(); }
		}
		public void read(String line)
		{
			numLine ++;
			if(fastForward > numLine)
			{
				return;
			}
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine);
				try
				{
					outputWriter.flush();
				}
				catch(IOException e) {}
			}
			if(mod)
			{
				String[] tokens = line.trim().split(" ");
				int removeIndex = rnd.nextInt(tokens.length);
				tokens = ArrayUtils.cutout(tokens, removeIndex);
				line = ListUtils.join(Arrays.asList(tokens), " ");
			}
			GrammaticalStructure gs = parser.parse(line);
			
			
			if(gs != null)
			{
				try
				{
					List<Dependency> deps = new ArrayList<>();
					for(TypedDependency td : gs.typedDependenciesCCprocessed())
					{
						deps.add(Dependency.getInstance(td));
					}
					outputWriter.write(ListUtils.join(deps, "@"));
					outputWriter.newLine();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			
		}
	}
}
