package tools;

import java.io.File;

import mwutils.MapCompressor;

public class RunThreshold 
{
	public static void main(String[] args)
	{
		/*
		for(int a = 1; a < 5; a++)
		{
			for(String mod : new String[] { "Mod", "" })
			{
				MapCompressor.threshold("D:/MissingWord/partialLex/5partialLex"+a+"offset"+mod+".txt", "D:/MissingWord/partialLex/5partialLex"+a+"offset"+mod+"5.txt", 5);
			}
		}
		*/
		/*
		for(int a = 1; a < 3 ; a++)
		{
			for(int b = 1; b < 3; b++)
			{
				for(int c = 1; c < 4; c ++)
				{
					for(String mod : new String[] { "Mod", "" })
					{
						File file = new File("D:/MissingWord/sideTags/"+a+"l"+b+"r"+c+"offset"+mod+".txt");
						if(file.exists())
						{
							MapCompressor.threshold(file.getAbsolutePath(), "D:/MissingWord/sideTags/"+a+"l"+b+"r"+c+"offset"+mod+"5.txt", 5);
						}
					}
				}
			}
		}
		*/
		for(int a = 1; a < 6; a++)
		{
			for(String mod : new String[] { "Mod", "" })
			{
				MapCompressor.threshold("D:/MissingWord/tag/6tag"+a+"offset"+mod+".txt", "D:/MissingWord/tag/6tag"+a+"offset"+mod+"5.txt", 5);
			}
		}
	}
}
