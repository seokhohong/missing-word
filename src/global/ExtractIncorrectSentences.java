package global;

import java.util.ArrayList;
import java.util.List;

import utils.Read;
import utils.Write;

public class ExtractIncorrectSentences 
{
	public static void main(String[] args)
	{
		new ExtractIncorrectSentences().from("C:/MissingWord/export.txt");
	}
	private void from(String file)
	{
		List<String> lines = Read.from(file);
		List<String> incorrect = new ArrayList<>();
		for(String line : lines)
		{
			if(line.startsWith("0"))
			{
				incorrect.add(line.substring(line.indexOf(" ") + 1));
			}
		}
		Write.to("C:/MissingWord/incorrect.txt", incorrect);
	}
}
