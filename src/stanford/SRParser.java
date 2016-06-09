package stanford;

import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;

public class SRParser implements ConstituencyParser 
{
	public static void main(String[] args)
	{
		SRParser parser = new SRParser();
		Tree tree = parser.parse("Japan has suspended imports of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese .");
	}
	MaxentTagger tagger;
	ShiftReduceParser model;
	public SRParser()
	{
		String parserModel = "edu/stanford/nlp/models/srparser/englishSR.beam.ser.gz";
		String taggerPath = "C:/MissingWord/models/english-left3words-distsim.tagger";

		tagger = new MaxentTagger(taggerPath);
		model = ShiftReduceParser.loadModel(parserModel);
	}
	public Tree parse(String text)
	{
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
		for (List<HasWord> sentence : tokenizer) 
		{
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			return model.apply(tagged);
		}
		return null;
	}
}
