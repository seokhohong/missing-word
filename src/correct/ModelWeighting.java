package correct;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.Read;

public class ModelWeighting 
{
	public static void main(String[] args)
	{
		for(List<Double> list : getWeights())
		{
			System.out.println(list);
		}
	}
	/**
	 * 
	 * Models IN ORDER
	 * 
	 */
	public static List<List<Double>> getWeights()
	{
		List<List<Double>> weights = new ArrayList<>();
		String labelFile = "C:/MissingWord/corrScoring/labels.txt";
		new File(labelFile).delete();
		while(new File(labelFile).exists())
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		runScript();
		

		while(!new File(labelFile).exists())
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		List<String> lines = Read.from(labelFile);
		List<List<Double>> toTranspose = new ArrayList<>();
		
		for(String line : lines)
		{
			List<Double> vector = new ArrayList<>();
			for(String num : line.split(" "))
			{
				vector.add(Double.parseDouble(num));
			}
			toTranspose.add(vector);
		}
		for(int a = 0; a < toTranspose.get(0).size(); a++)
		{
			List<Double> list = new ArrayList<>();
			for(int b = 0; b < toTranspose.size(); b++)
			{
				list.add(0d);
			}
			weights.add(list);
		}
		for(int a = 0; a < toTranspose.size(); a++)
		{
			for(int b = 0; b < toTranspose.get(a).size(); b ++)
			{
				weights.get(b).set(a, toTranspose.get(a).get(b));
			}
		}
		return weights;
	}
	private static void runScript()
	{
		try
		{
			Runtime.getRuntime().exec("python C:/Users/SEOKHO/Dropbox/Workspace/MissingWord/corrModel/QueryMe.py");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
