package global;

import java.util.List;

public interface PairwiseModel 
{
	public List<List<Double>> getFeatures(List<ImprovementSet> pairs);
	public void load();
	public void unload();
}
