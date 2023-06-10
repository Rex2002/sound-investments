package util;

public class General {
	public static String strReplace(String s, char searched, char replacement) {
		char[] chars = s.toCharArray();
		StringBuffer out = new StringBuffer(chars.length);
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			out.append(c == searched ? replacement : c);
		}
		return out.toString();
	}
}
