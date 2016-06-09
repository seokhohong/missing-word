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

public class ProcessChunks 
{
	public static void main(String[] args)
	{
		new ProcessChunks().go();
	}
	private void go()
	{
		Random rnd = new Random();
		
		ChunkCounter mod = ChunkCounter.load("C:/MissingWord/chunksModCounterPart1.txt");
		ChunkCounter orig = ChunkCounter.load("C:/MissingWord/chunksCounterPart1.txt");
		try
		{
			BufferedReader corpusReader = new BufferedReader(new FileReader("C:/MissingWord/train/corpusPart2.txt"));
			BufferedReader tagsReader = new BufferedReader(new FileReader("C:/MissingWord/train/tagsPart2.txt"));
			
			OpenNLPChunker chunker = new OpenNLPChunker();
			
			int lines = 0;
			while(true)
			{
				if(lines % 1000 == 0)
				{
					System.out.println(lines);
				}
				lines ++;
				String tokenLine = corpusReader.readLine();
				String tagLine = tagsReader.readLine();
				
				if(tokenLine == null) break;
				if(tokenLine.length() < 2) continue;
				
				String[] tokens = tokenLine.split(" ");
				String[] tags = tagLine.split("\\|");
				if(tokens.length != tags.length) continue;
				int removeIndex = rnd.nextInt(tokens.length);
				tokens = ArrayUtils.cutout(tokens, removeIndex);
				tags = ArrayUtils.cutout(tags, removeIndex);
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
				System.out.println(ArrayUtils.print(tokenLine.split(" ")));
				System.out.println(ArrayUtils.print(tokens));
				System.out.println(output);
				for(String chunk : output)
				{
					System.out.println(chunk + " " + mod.get(chunk) + " " + orig.get(chunk));
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
