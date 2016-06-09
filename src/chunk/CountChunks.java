package chunk;


public class CountChunks 
{
	public static void main(String[] args)
	{
		new CountChunks().go();
	}
	private void go()
	{
		new ChunkCounter("C:/MissingWord/chunksPart1.txt").export("C:/MissingWord/chunksCounterPart1.txt");
		new ChunkCounter("C:/MissingWord/chunksModPart1.txt").export("C:/MissingWord/chunksModCounterPart1.txt");
	}
}
