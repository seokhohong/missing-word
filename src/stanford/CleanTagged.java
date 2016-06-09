package stanford;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import utils.ListUtils;
import utils.Read;
import utils.Read.LineOperation;

public class CleanTagged 
{
	public static void main(String[] args)
	{
		new CleanTagged().go();
	}
	private void go()
	{
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("C:/MissingWord/train/taggedPart"+a+".txt", new Clean("C:/MissingWord/train/taggedPartClean"+a+".txt"));
		}
	}
	private class Clean implements LineOperation
	{
		int numLine = 0;
		BufferedWriter writer;
		Clean(String output)
		{
			try
			{
				writer = new BufferedWriter(new FileWriter(output));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		@Override
		public void read(String line) 
		{
			numLine += 1;
			if(numLine % 10000 == 0)
			{
				System.out.println(numLine);
			}
			boolean goodLine = true;
			String[] split = line.split(" ");
			String[] newTokens = new String[split.length];
			for(int a = 0; a < split.length; a++)
			{
				String token = split[a];
				int lastIndex = token.lastIndexOf("_");
				String fixedToken = null;
				if(token.indexOf("_") != lastIndex)
				{
					fixedToken = token.substring(0, lastIndex) + "~" + token.substring(lastIndex + 1);
					line = line.replace(token, fixedToken);
				}
				else
				{
					fixedToken = split[a].replace("_", "~");
				}
				newTokens[a] = fixedToken;
			}
			line = ListUtils.join(Arrays.asList(newTokens), " ");
			if(goodLine)
			{
				try
				{
					writer.write(line);
					writer.newLine();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}
}
