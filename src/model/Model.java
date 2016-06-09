package model;

import java.util.List;
import mwutils.TaggedSentence;

public interface Model 
{
	public List<List<float[]>> process(List<TaggedSentence> sentences);
}
