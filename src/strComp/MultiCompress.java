package strComp;

import java.util.Arrays;

import utils.ArrayUtils;

public class MultiCompress 
{
	public static void main(String[] args)
	{
		Dictionary tags = Dictionary.fromCounterFile("C:/MissingWord/tags.txt");
		Dictionary words = Dictionary.fromCounterFile("C:/MissingWord/frequentWords.txt");
		MultiCompress mc = new MultiCompress(tags, tags, words);
		long encoded = mc.encode("NN", "DT", "Winfrey");
		System.out.println(encoded);
		System.out.println(ArrayUtils.print(mc.decode(encoded)));
	}
	private Dictionary[] dicts;
	private int[] numBits;
	public MultiCompress(Dictionary... dicts)
	{
		this.dicts = dicts;
		setBits();
	}
	public MultiCompress(Dictionary dict, int repetitions)
	{
		Dictionary[] dicts = new Dictionary[repetitions];
		Arrays.fill(dicts, dict);
		this.dicts = dicts;
		setBits();
	}
	private void setBits()
	{
		numBits = new int[dicts.length];
		for(int a = 0; a < dicts.length; a++)
		{
			numBits[a] = dicts[a].bitsRequired();
		}
	}
	public long encode(String... args)
	{
		if(args.length != dicts.length)
		{
			System.out.println("Compressing improper length!");
			System.exit(1);
		}
		long data = 0;
		for(int a = 0; a < args.length; a++)
		{
			data += dicts[a].encode(args[a]); 
			if(a < args.length - 1)
			{
				data = data << numBits[a + 1];
			}
		}
		return data;
	}
	public String[] decode(long data)
	{
		String[] toReturn = new String[dicts.length];
		int bitOffset = 0;
		for(int a = dicts.length; a --> 0; )
		{
			long isolated = data << (64 - numBits[a] - bitOffset) >>> (64 - numBits[a]);
			toReturn[a] = dicts[a].decode((int) isolated);
			bitOffset += numBits[a];
		}
		return toReturn;
	}
	public int bitsRequired()
	{
		return ArrayUtils.sum(numBits);
	}
}
