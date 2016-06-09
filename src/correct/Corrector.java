package correct;

import gnu.trove.map.TLongDoubleMap;

import java.util.Map;

import main.Gold;

public interface Corrector 
{
	public void reweight(Map<Gold, TLongDoubleMap> weights);
}
