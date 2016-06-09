package stanford;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CleanNewlines 
{
	public static void main(String[] args)
	{
		new CleanNewlines().go();
	}
	private void go()
	{
		for(int a = 0; a < 5 ; a++)
		{
			clean("C:/MissingWord/train/taggedPart"+a+".txt", "C:/MissingWord/train/cleanTaggedPart"+a+".txt");
		}
	}
	private void clean(String input, String output)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
			BufferedReader reader = new BufferedReader(new FileReader(input));
			String line = "";
			while((line = reader.readLine()) != null)
			{
				if(line.length() > 0)
				{
					writer.write(line);
					writer.newLine();
				}
			}
			writer.close();
			reader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
