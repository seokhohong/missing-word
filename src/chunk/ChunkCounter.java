package chunk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChunkCounter 
{
	private Map<String, Integer> counter = new HashMap<>();
	public ChunkCounter(String file)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			int index = 0;
			while((line = reader.readLine()) != null)
			{
				index ++;
				if(index % 100000 == 0)
				{
					System.out.println(index);
				}
				String[] chunks = line.split("\\\\");
				for(String chunk : chunks)
				{
					if(!counter.containsKey(chunk))
					{
						counter.put(chunk, 0);
					}
					counter.put(chunk, counter.get(chunk) + 1);
				}
			}
			reader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	private ChunkCounter(Map<String, Integer> counter)
	{
		this.counter = counter;
	}
	public int get(String chunk)
	{
		if(!counter.containsKey(chunk))
		{
			return 0;
		}
		return counter.get(chunk);
	}
	public static ChunkCounter load(String file)
	{
		Map<String, Integer> tempCounter = new HashMap<>();
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line = reader.readLine()) != null)
			{
				String[] split = line.split(" ");
				tempCounter.put(split[0], Integer.parseInt(split[1]));
			}
			reader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return new ChunkCounter(tempCounter);
	}
	public void export(String file)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for(String key : counter.keySet())
			{
				writer.write(key +" "+counter.get(key));
				writer.newLine();
			}
			writer.close();
		}
		catch(IOException e)
		{
			
		}
	}
}

