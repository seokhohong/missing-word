package tools;

import java.util.concurrent.atomic.AtomicInteger;

import utils.Read;
import utils.Read.LineOperation;

public class NumQuestions 
{
	//600k
	private AtomicInteger count = new AtomicInteger(0);
	public static void main(String[] args)
	{
		new NumQuestions().go();
	}
	private void go()
	{
		for(int a = 0; a < 5; a++)
		{
			Read.byLine("C:/MissingWord/train/taggedPart"+a+".txt", new CountQuestions());
		}
		System.out.println(count);
	}
	class CountQuestions implements LineOperation
	{
		@Override
		public void read(String line) 
		{
			String[] tokens = line.split(" ");
			if(tokens[tokens.length - 1].equals("?_."))
			{
				count.addAndGet(1);
			}
		}
	}
}
