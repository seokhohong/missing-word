package model;

import java.util.List;

import mwutils.TaggedSentence;

public interface ParseModel 
{
	public List<List<float[]>> process(List<String> sentences);
}
