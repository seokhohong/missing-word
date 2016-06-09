package dep;

import mwutils.MapCompressor;

public class ThresholdAllDep 
{
	public static void main(String[] args)
	{
		new ThresholdAllDep().go();
	}
	private void go()
	{
		MapCompressor.threshold("C:/MissingWord/dep/wholeDep.txt", "C:/MissingWord/dep/wholeDep5.txt", 5);
	}
}
