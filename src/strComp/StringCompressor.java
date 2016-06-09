package strComp;

public class StringCompressor 
{
	public static void main(String[] args)
	{
		StringCompressor strcmp = new StringCompressor();
		long compressed = strcmp.encode(new char[] { 'a', 'c' });
		System.out.println(strcmp.decode(compressed));
	}
	public long encode(String str)
	{
		return encode(str.toCharArray());
	}
	public long encode(char[] chars, int length)
	{
		long result = 0;
		for(int a = 0; a < length; a++)
		{
			result += chars[a] << ((a + 1) * 8);
		}
		result += length;
		return result;
	}
	public long encode(char[] chars)
	{
		return encode(chars, chars.length);
	}
	public char[] decode(long val)
	{
		char[] array = new char[(int) (val << 56 >>> 56)];
		for(int a = 0; a < array.length; a++)
		{
			char isolated = (char) (val << (6 - a) * 8 >>> 56);
			array[a] = isolated;
		}
		return array;
	}
	public int length(long encoded)
	{
		return (int) (encoded << 56 >>> 56);
	}
}
