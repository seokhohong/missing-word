package tools;

import java.io.File;

public class MakeDirs 
{
	public static void main(String[] args)
	{
		File dir = new File("C:/MissingWord/pos");
		for(int a = 0; a < 50; a++)
		{
			File newDir = new File("C:/MissingWord/pos/"+a);
			if(!newDir.exists())
			{
				newDir.mkdir();
			}
		}
	}
}
