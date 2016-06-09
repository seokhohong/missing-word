package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ReadSimul 
{
	public static void byLine(String file1, String file2, SimulLineOperation op)
	{
		try
		{
			BufferedReader buff1 = new BufferedReader(new FileReader(new File(file1)));
			BufferedReader buff2 = new BufferedReader(new FileReader(new File(file2)));
			String line;
			while((line = buff1.readLine())!=null)
			{
				op.read(line, buff2.readLine());
			}
			buff1.close();
			buff2.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void byLine(String file1, String file2, SimulLineOperation op, int numLines)
	{
		try
		{
			BufferedReader buff1 = new BufferedReader(new FileReader(new File(file1)));
			BufferedReader buff2 = new BufferedReader(new FileReader(new File(file2)));
			String line;
			int numLine = 0;
			while((line = buff1.readLine())!=null)
			{
				op.read(line, buff2.readLine());
				numLine ++;
				if(numLine == numLines)
				{
					break;
				}
			}
			buff1.close();
			buff2.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public interface SimulLineOperation
	{
		public void read(String line1, String line2);
	}
}
