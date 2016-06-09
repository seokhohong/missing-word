package strComp;

import java.util.Arrays;

public class MinimalString 
{
	private char[] chars;
	public MinimalString(String str)
	{
		chars = str.toCharArray();
	}
	public String toString()
	{
		return new String(chars);
	}
	public int hashCode()
	{
		return new String(chars).hashCode();
	}
	public boolean equals(Object o)
	{
		if(o instanceof MinimalString)
		{
			MinimalString other = (MinimalString) o;
			return Arrays.equals(chars, other.chars);
		}
		return false;
	}
}
