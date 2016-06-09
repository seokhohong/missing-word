package dep;

import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;

public class DepParser 
{
	public static void main(String[] args)
	{
		DepParser parser = new DepParser();
		parser.parse("text");
		System.out.println(parser.parse("Japan has suspended imports of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese ."));
	}
	MaxentTagger tagger;
	DependencyParser parser;
	String modelPath = DependencyParser.DEFAULT_MODEL;
    String taggerPath = "models/english-left3words-distsim.tagger";
	public DepParser()
	{
		tagger = new MaxentTagger(taggerPath);
		parser = DependencyParser.loadFromModelFile(modelPath);
	}
	public void reload()
	{
		parser = null;
		System.gc();
		parser = DependencyParser.loadFromModelFile(modelPath);
	}
	public GrammaticalStructure parse(String text)
	{
	    DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
	    for (List<HasWord> sentence : tokenizer) {
	      List<TaggedWord> tagged = tagger.tagSentence(sentence);
	      return parser.predict(tagged);
	    }
	    return null;
	}
}
