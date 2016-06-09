package correct;

import java.util.*;

import mwutils.Counter;
import strComp.Dictionary;
import utils.Multitasker;
import utils.Pickle;
import utils.Read;
import utils.Read.LineOperation;

public class TfidfWeights 
{
	Counter<String> words = Counter.load("C:/MissingWord/frequentWords.txt");
	public static void main(String[] args)
	{
		new TfidfWeights().make();
	}
	private void make()
	{
		Counter<String> document = new Counter<>();
		Multitasker multitasker = new Multitasker(5);
		for(int a = 0; a < 5; a++)
		{
			multitasker.load(new IDF("D:/MissingWord/train/tokensPart"+a+".txt", document));
		}
		multitasker.done();
		Map<String, Double> tfidf = new HashMap<>(); 
		for(String key : document.keySet())
		{
			if(words.contains(key))
			{
				tfidf.put(key, (double) words.getCount(key) / document.getCount(key));
			}
		}
		Pickle.dump(tfidf, "C:/MissingWord/tfidf.ser");
	}
	class IDF implements Runnable, LineOperation
	{
		private String filename;
		private Counter<String> document;
		IDF(String filename, Counter<String> document)
		{
			this.filename = filename;
			this.document = document;
		}
		@Override
		public void read(String line) 
		{
			Set<String> tokenSet = new HashSet<>();
			for(String token : line.split(" "))
			{
				tokenSet.add(token);
			}
			for(String token : tokenSet)
			{
				document.add(token);
			}
		}

		@Override
		public void run() 
		{
			Read.byLine(filename, this);
		}
	}
}
