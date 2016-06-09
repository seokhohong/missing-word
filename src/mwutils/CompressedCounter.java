package mwutils;

import gnu.trove.set.TLongSet;

public interface CompressedCounter
{
	public void add(String... elems);
	public int getCount(String... elems);
	public void export(String filename);
	public int keySize();
	public TLongSet keySet();
}
