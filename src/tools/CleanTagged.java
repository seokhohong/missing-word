package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CleanTagged 
{
	public static void main(String[] args)
	{
		for(int a = 0; a < 5; a++)
		{
			try
			{
				BufferedWriter writer = new BufferedWriter(new FileWriter("C:/MissingWord/train/cleanTaggedPart"+a+".txt"));
				BufferedReader reader = new BufferedReader(new FileReader("C:/MissingWord/train/taggedPart"+a+".txt"));
				String line;
				while((line = reader.readLine()) != null)
				{
					writer.write(line.replaceAll("@", "at")+"\n");
				}
				reader.close();
				writer.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
