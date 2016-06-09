package chunk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import open.OpenNLPChunker;
import utils.ArrayUtils;
import utils.ListUtils;

public class RunChunker 
{
	public static void main(String[] args)
	{
		new RunChunker().go();
	}
	private boolean remove = false;
	private Random rnd = new Random();
	public void go()
	{
		
		try
		{
			BufferedReader corpusReader = new BufferedReader(new FileReader("C:/MissingWord/train/cleanTokensPart1.txt"));
			BufferedReader tagsReader = new BufferedReader(new FileReader("C:/MissingWord/train/cleanTagsPart1.txt"));
			BufferedWriter chunkWriter = new BufferedWriter(new FileWriter("C:/MissingWord/chunksPart1.txt"));

			OpenNLPChunker chunker = new OpenNLPChunker();
			
			int lines = 0;
			while(true)
			{
				if(lines % 1000 == 0)
				{
					System.out.println(lines);
				}
				String tokenLine = corpusReader.readLine();
				String tagLine = tagsReader.readLine();
				
				if(tokenLine == null) break;
				if(tokenLine.length() < 2) continue;
				
				String[] tokens = tokenLine.split(" ");
				String[] tags = tagLine.split("\\|");
				
				if(tokens.length != tags.length) 
				{
					continue;
				}
				if(remove)
				{
					int removeIndex = rnd.nextInt(tokens.length);
					tokens = ArrayUtils.cutout(tokens, removeIndex);
					tags = ArrayUtils.cutout(tags, removeIndex);
				}
				String[] chunks = chunker.chunk(tokens, tags);
				
				List<String> output = new ArrayList<>();
				List<String> currentTags = new ArrayList<>();
				for(int a = 0; a < chunks.length; a++)
				{
					currentTags.add(tags[a]);
					if(a + 1 >= chunks.length || chunks[a+1].startsWith("B-") || chunks[a+1].equals("O"))
					{
						String chunkLabel = chunks[a].substring(Math.min(2, chunks[a].length() - 1));
						if(chunkLabel.equals("O"))
						{
							chunkLabel = tags[a];
						}
						output.add(chunkLabel+"|"+ListUtils.join(currentTags, "|"));
						currentTags.clear();
					}
				}
				chunkWriter.write(ListUtils.join(output, "\\"));
				chunkWriter.newLine();
				lines ++;
			}


			corpusReader.close();
			tagsReader.close();
			chunkWriter.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
