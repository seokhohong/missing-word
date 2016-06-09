package pcfg;

import stanford.PCFGParser;

public class TestPCFG 
{
	public static void main(String[] args)
	{
		new TestPCFG().go();
	}
	private void go()
	{
		PCFGParser parser = new PCFGParser();
		System.out.println(parser.parse("Arsenal are shocked when third largest shareholder, Lady Nina Bracewell-Smith, the board.").score());
		System.out.println(parser.parse("The Arsenal are shocked when third largest shareholder, Lady Nina Bracewell-Smith, the board.").score());
		System.out.println(parser.parse("Arsenal are very shocked when third largest shareholder, Lady Nina Bracewell-Smith, the board.").score());
		System.out.println(parser.parse("Arsenal are shocked when the third largest shareholder, Lady Nina Bracewell-Smith, the board.").score());
		System.out.println(parser.parse("Arsenal are shocked when third largest shareholder title, Lady Nina Bracewell-Smith, the board.").score());
		System.out.println(parser.parse("Arsenal are shocked when third largest shareholder, Lady Maxwell Nina Bracewell-Smith, the board.").score());
		System.out.println(parser.parse("Arsenal are shocked when third largest shareholder, Lady Nina Bracewell-Smith, left the board.").score());
	}
}
