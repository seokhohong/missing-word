package global;

import java.util.List;


public class ImprovementSet 
{
	private List<List<String>> sentences;					public List<List<String>> getSentences() { return sentences; }
	private int improvementIndex;							public int getImprovementIndex() { return improvementIndex; }
	
	public ImprovementSet(List<List<String>> sentences, int improvementIndex)
	{
		this.sentences = sentences;
		this.improvementIndex = improvementIndex;
	}
}
