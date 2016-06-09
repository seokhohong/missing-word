package word2vec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Read;

public class SimilarityMatrix 
{
	public static void main(String[] args)
	{
		SimilarityMatrix matrix = SimilarityMatrix.load("C:/MissingWord/mostRelated.txt");
		System.out.println(matrix.similarityData("people"));
	}
	private Map<String, Map<String, Double>> matrix;
	private SimilarityMatrix(Map<String, Map<String, Double>> matrix)
	{
		this.matrix = matrix;
	}
	public static SimilarityMatrix load(String filename)
	{
		Map<String, Map<String, Double>> matrix = new HashMap<>();
		List<String> lines = Read.from(filename);
		for(String line : lines)
		{
			String[] split1 = line.split("@");
			String[] similars = split1[1].split("\\\\");
			Map<String, Double> similarities = new HashMap<>();
			for(int a = 0; a < similars.length / 2; a++)
			{
				try
				{
					similarities.put(similars[a * 2], Double.parseDouble(similars[a * 2 + 1]));
				}
				catch(NumberFormatException e)
				{
					
				}
			}
			matrix.put(split1[0], similarities);
		}
		return new SimilarityMatrix(matrix);
	}
	public static SimilarityMatrix load(String filename, int num)
	{
		Map<String, Map<String, Double>> matrix = new HashMap<>();
		List<String> lines = Read.from(filename);
		for(String line : lines)
		{
			String[] split1 = line.split("@");
			String[] similars = split1[1].split("\\\\");
			Map<String, Double> similarities = new HashMap<>();
			for(int a = 0; a < Math.min(similars.length / 2, num * 2); a++)
			{
				try
				{
					similarities.put(similars[a * 2], Double.parseDouble(similars[a * 2 + 1]));
				}
				catch(NumberFormatException e)
				{
					
				}
			}
			matrix.put(split1[0], similarities);
		}
		return new SimilarityMatrix(matrix);
	}
	public boolean containsKey(String word)
	{
		return matrix.containsKey(word);
	}
	public Map<String, Double> similarityData(String word)
	{
		return matrix.get(word);
	}
	public Map<String, Double> similarityDataAndSelf(String word)
	{
		Map<String, Double> simMap = new HashMap<>();
		simMap.put(word, 1d);
		if(containsKey(word))
		{
			simMap.putAll(similarityData(word));
		}
		return simMap;
	}
}
