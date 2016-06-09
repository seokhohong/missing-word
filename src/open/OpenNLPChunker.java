package open;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

public class OpenNLPChunker 
{
	ChunkerME chunker;
	public OpenNLPChunker()
	{
		InputStream modelIn = null;
		ChunkerModel model = null;

		try {
		  modelIn = new FileInputStream("models/en-chunker.bin");
		  model = new ChunkerModel(modelIn);
		  chunker = new ChunkerME(model);
		} catch (IOException e) {
		  // Model loading failed, handle the error
		  e.printStackTrace();
		} finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    } catch (IOException e) {
		    }
		  }
		}
	}
	public String[] chunk(String[] tokens, String[] tags)
	{
		return chunker.chunk(tokens, tags);
	}
}
