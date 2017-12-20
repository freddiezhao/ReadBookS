package org.kxml3.io;


/**
 * 字符串辅助类
 * 
 * @author 杨芹勍
 */
public final class StringHelper
{
	private String	src;
	private int		pos	= 0;

	public StringHelper(String src)
	{
		if (src == null)
			throw new IllegalStateException("src");

		this.src = src;
		this.pos = 0;
	}

	public final String nextToken(char delim)
	{
		int len = src.length();

		if (pos == len) {
			return null;
		}

		StringBuffer buf = new StringBuffer();
		for (; pos < len; ++pos) {
			char c = src.charAt(pos);
			if (c == delim) {
				do { // skip all consequent delims
					++pos;
					
					if (pos == len)
						break;
				} while (pos < len && src.charAt(pos) == delim);
				break;
			}
			buf.append(c);
		}

		String result = buf.toString(); // return the token.
		buf = null;
		return result;
	}

	public final String nextToken(String delims)
	{
		int len = src.length();

		if (pos == len) {
			return null;
		}

		StringBuffer buf = new StringBuffer();
		for (; pos < len; ++pos) {
			char c = src.charAt(pos);
			if (delims.indexOf(c) >= 0) {
				do { // skip all consequent delims
					++pos;
				} while (pos < len && delims.indexOf(src.charAt(pos)) >= 0);
				break;
			}
			buf.append(c);
		}

		String result = buf.toString(); // return the token.
		buf = null;
		return result;
	}

	public final void skipWhiteChars()
	{
		char c;
		for (; pos < src.length(); ++pos) // skip white characters at start
		{
			c = src.charAt(pos);
			if (c != ' ' && c != '\t' && c != '\n')
				break;
		}
	}

	public final char peakNextChar()
	{
		if (pos < src.length())
			return src.charAt(pos);
		else
			return '\0';
	}

	public final Integer nextInteger()
	{
		skipWhiteChars();
		char c = peakNextChar();

		if (c < '0' || c > '9')
			return null; // not a digit.

		int start = pos;
		++pos;
		for (; pos < src.length(); ++pos) // find the last digit of the number
		{
			c = src.charAt(pos);
			if (c < '0' || c > '9')
				break;// not a digit.
		}

		return new Integer(Integer.parseInt(src.substring(start, pos)));
	}

	public final String nextWord()
	{
		skipWhiteChars();
		char c = peakNextChar();

		if (c == '\0') // no more words
			return null;

		int start = pos;
		++pos;
		for (; pos < src.length(); ++pos) // a word is a series of characters
		// that are not white characters
		{
			c = src.charAt(pos);
			if (c == ' ' || c == '\t' || c == '\n')
				break;// end of word.
		}
		return src.substring(start, pos);
	}

	public final String lastToken()
	{
		if (pos < src.length())
			return src.substring(pos);
		return null;
	}

	public static String trimStart(String s)
	{
		if (s == null) {
			return null;
		}

		int pos = 0;
		for (; pos < s.length(); ++pos) {
			char c = s.charAt(pos);
			if (c != ' ' && c != '\t' && c != '\n')
				break;
		}

		return s.substring(pos);
	}

	public final static String trimQuotes(String val)
	{
		if (val == null || val.length() == 0)
			return null;

		int len = val.length();
		int st = 0;
		int off = 0; /* avoid getfield opcode */

		while ((st < len)
				&& ((val.charAt(off + st) <= '\'') || (val.charAt(off + st) <= '\"')))
			st++;

		while ((st < len)
				&& ((val.charAt(off + len - 1) <= '\'') || (val.charAt(off
						+ len - 1) <= '\"')))
			len--;

		return ((st > 0) || (len < val.length())) ? val.substring(st, len)
				: val;
	}

	public static final boolean isNullOrEmpty(String str)
	{
		return str == null || str.length() == 0;
	}

	public static final boolean contains(char c, char[] array)
	{
		for (int i = 0; i < array.length; ++i) {
			if (c == array[i])
				return true;
		}
		return false;
	}

	public final static int indexOfIgnoreCase(String org, String str)
	{
		return indexOfIgnoreCase(org, str, 0);
	}

	public final static int indexOfIgnoreCase(String org, String str,
			int fromIndex)
	{
		int max = 0 + (org.length() - str.length());
		if (fromIndex >= org.length()) {
			if (org.length() == 0 && fromIndex == 0 && str.length() == 0) {
				return 0;
			}
			return -1;
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (str.length() == 0) {
			return fromIndex;
		}

		int strOffset = 0;
		char first = str.charAt(strOffset);
		int i = fromIndex;

		startSearchForFirstChar: while (true) {

			/* Look for first character. */
			while (i <= max
					&& Character.toLowerCase(org.charAt(i)) != Character
							.toLowerCase(first)) {
				i++;
			}
			if (i > max) {
				return -1;
			}

			/* Found first character, now look at the rest of v2 */
			int j = i + 1;
			int end = j + str.length() - 1;
			int k = strOffset + 1;
			while (j < end) {
				if (Character.toLowerCase(org.charAt(j++)) != Character
						.toLowerCase(str.charAt(k++))) {
					i++;
					/* Look for str's first char again. */
					continue startSearchForFirstChar;
				}
			}
			return i; /* Found whole string. */
		}
	}

	public final static int indexOfIgnoreCase(String str, char ch)
	{
		return indexOfIgnoreCase(str, ch, 0);
	}

	public final static int indexOfIgnoreCase(String str, char ch, int fromIndex)
	{
		int max = str.length();

		if (fromIndex < 0) {
			fromIndex = 0;
		} else if (fromIndex >= max) {
			return -1;
		}
		for (int i = fromIndex; i < max; i++) {
			char vi = str.charAt(i);
			if (vi == ch) {
				return i;
			} else if (Character.toLowerCase(vi) == Character.toLowerCase(ch)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 移除css注释
	 * 
	 * @param value
	 * @return
	 */
	public final static String removeCssComment(String value)
	{
		return removeComment(value, false, true);
	}

	/**
	 * 移除C风格注释
	 * 
	 * @param value
	 * @return
	 */
	public final static String removeCStyleComment(String value)
	{
		return removeComment(value, true, true);
	}

	/**
	 * 移除注释
	 * 
	 * @param value
	 * @param isRemoveInlineComment
	 *            是否移除行内注释
	 * @param isRemoveBlockComment
	 *            是否移除块注释
	 * @return
	 */
	public final static String removeComment(String value,
			boolean isRemoveInlineComment, boolean isRemoveBlockComment)
	{

		String result = value;

		// 移除注释
		// “//”型注释
		if (isRemoveInlineComment) {
			for (;;) {
				int inlineCommentPos = result.indexOf("//");
				if (inlineCommentPos >= 0) {
					// 查找\n
					int newlinePos = result.indexOf('\n', inlineCommentPos + 2);
					if (newlinePos >= 0)
						result = new StringBuffer(result).delete(
								inlineCommentPos, newlinePos + 1).toString();
					else {
						result = result.substring(0, inlineCommentPos);
						break;
					}
				} else
					break;
			}
		}

		if (isRemoveBlockComment) {
			// “/**/” 型注释
			for (;;) {
				int blockCommentStartPos = result.indexOf("/*");
				if (blockCommentStartPos >= 0) {
					int blockCommentEndPos = result.indexOf("*/",
							blockCommentStartPos + 2);

					result = new StringBuffer(result).delete(
							blockCommentStartPos, blockCommentEndPos + 2)
							.toString();
				} else
					break;
			}
		}

		return result;
	}

	/**
	 * 获取合并后字符串
	 * 
	 * @param strs
	 * @return
	 */
	public final static String mergeStrings(String[] strs)
	{
		StringBuffer sb = new StringBuffer();

		for (int n = 0; n < strs.length; ++n) {
			sb.append(strs[n]);
		}

		String result = sb.toString();
		sb = null;

		return result;
	}

	/**
	 * 去除换行符和空格
	 * 
	 * @param val
	 * @return
	 */
	public final static String trimLine(String val)
	{
		if (val == null)
			return null;

		int len = val.length();
		int st = 0;

		while ((st < len)
				&& (val.charAt(st) == ' ' || val.charAt(st) == '\t'
						|| val.charAt(st) == '\r' || val.charAt(st) == '\n'))
			st++;

		while ((st < len)
				&& (val.charAt(len - 1) == ' ' || val.charAt(len - 1) == '\t'
						|| val.charAt(len - 1) == '\r' || val.charAt(len - 1) == '\n'))
			len--;

		return ((st > 0) || (len < val.length())) ? val.substring(st, len)
				: val;
	}

	/**
	 * 获取哈希值
	 * 
	 * @param s
	 * @return
	 */
	public final static int hashcode(String s)
	{
		if (s == null) {
			return 0;
		}

		return substrHashcode(s, 0, s.length());
	}

	public final static int substrHashcode(String s, int pos)
	{
		if (s == null) {
			return 0;
		}

		return substrHashcode(s, pos, s.length() - pos);
	}

	public final static int substrHashcode(String s, int pos, int len)
	{
		if (s == null) {
			return 0;
		}

		int h = 0;

		for (int i = 0; i < len; ++i) {
			h = 31 * h + s.charAt(i + pos);
		}

		return h;
	}

	/**
	 * 获取哈希值，忽略大小写
	 * 
	 * @param s
	 * @return
	 */
	public final static int hashcodeIgnoreCase(String s)
	{
		if (s == null) {
			return 0;
		}

		return substrHashcodeIgnoreCase(s, 0, s.length());
	}

	public final static int substrHashcodeIgnoreCase(String s, int pos)
	{
		if (s == null) {
			return 0;
		}

		return substrHashcodeIgnoreCase(s, pos, s.length() - pos);
	}

	public final static int substrHashcodeIgnoreCase(String s, int pos,
			int len, boolean isTrim)
	{

		if (s == null) {
			return 0;
		}

		if (isTrim) {
			int startPos = pos;
			while (s.charAt(pos) <= ' ') {
				pos++;
			}

			while (s.charAt(startPos + len - 1) <= ' ') {
				len--;
			}

			len -= pos;
		}

		return substrHashcodeIgnoreCase(s, pos, len);
	}

	public final static int substrHashcodeIgnoreCase(String s, int pos, int len)
	{

		if (s == null) {
			return 0;
		}

		int h = 0;

		for (int i = 0; i < len; i++) {
			h = 31 * h + Character.toLowerCase(s.charAt(i + pos));
		}

		return h;
	}

	/**
	 * 字符串到十六进制字符串
	 * 
	 * @param s
	 * @return
	 */
	public final static String hexString(String s)
	{
		if (s == null) {
			return null;
		}

		try {
			return hexString(s.getBytes("UTF-8"));
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		} catch (Error ex) {
			ex.printStackTrace();
			return "";
		}
	}

	/**
	 * 两字符串不考虑大小写是否相等
	 * 
	 * @param thisString
	 * @param anotherString
	 * @return
	 */
	public final static boolean equalsIgnoreCase(String thisString,
			String anotherString)
	{

		if (thisString != null && thisString == anotherString) {
			return true;
		} else {
			if (thisString != null && anotherString != null) {
				int len1 = thisString.length();
				int len2 = anotherString.length();

				if (len1 == len2) {
					return thisString.regionMatches(true, 0, anotherString, 0,
							len1);
				}
			}

			return false;
		}
	}

	/**
	 * 字节集合到十六进制字符串
	 * 
	 * @param b
	 * @return
	 */
	public final static String hexString(byte[] b)
	{
		if (b == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer(b.length * 3);
		for (int i = 0; i < b.length; ++i) {
			sb.append("%");
			sb.append(Integer.toHexString(b[i] & 0xff));
		}

		String result = sb.toString();
		sb = null;

		return result;
	}

	/**
	 * 获取escape串
	 * 
	 * @param s
	 * @param quot
	 * @param isUnicode
	 * @param isDeleteEscape
	 * @return
	 */
	public final static String escapedString(String s, int quot,
			boolean isUnicode, boolean isDeleteEscape)
	{

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\n':
			case '\r':
			case '\t':
				if (!isDeleteEscape) {
					if (quot == -1) {
						sb.append(c);
					} else {
						sb.append("&#" + ((int) c) + ';');
					}
				}
				break;

			case '&':
				if (!isDeleteEscape) {
					sb.append("&amp;");
				}
				break;

			case '>':
				if (!isDeleteEscape) {
					sb.append("&gt;");
				}
				break;

			case '<':
				if (!isDeleteEscape) {
					sb.append("&lt;");
				}
				break;

			case '"':
			case '\'':
				if (c == quot) {
					if (!isDeleteEscape) {
						sb.append(c == '"' ? "&quot;" : "&apos;");
						break;
					}
				}

			default:
				// if(c < ' ')
				// throw new
				// IllegalArgumentException("Illegal control code:"+((int) c));

				if (c >= ' ' && c != '@' && (c < 127 || isUnicode)) {
					sb.append(c);
				} else {
					if (!isDeleteEscape) {
						sb.append("&#" + ((int) c) + ";");
					}
				}
			}
		}

		String result = sb.toString();
		sb = null;

		return result;
	}

	/**
	 * 格式化字符串 使用方法: srcStr传入"a,{0},b,{1},c,{2}"，parms传入:{"aa", "bb",
	 * "cc"}，则返回"a,aa,b,bb,c,cc" 其中srcStr中的{0},{1},{2}为与parms参数一一对应的替换位置
	 * 注:索引从{0}开始 常用: 在Common中定义:MAP_NAME = "/maps/{1}.bin"，通过
	 * formatString(MAP_NAME, new String[]{"2"});来获取地图文件名
	 * 
	 * @param srcStr
	 *            来源字符串
	 * @param parms
	 *            格式化参数
	 * @return 格式化后的字符串
	 */
	public final static String formatString(String srcStr, String[] parms)
	{
		if (srcStr == null || parms == null) {
			return null;
		} else {
			String result = new String(srcStr);
			for (int n = 0; n < parms.length; ++n) {
				String srcParmName = "{" + n + "}";
				result = replaceString(result, srcParmName, parms[n]);
			}

			return result;
		}
	}

	/**
	 * 格式化字符串 参数为数值型重载
	 * 
	 * @param srcStr
	 *            来源字符串
	 * @param parm
	 *            长整形参数
	 * @return 格式化后的字符串
	 */
	public final static String formatString(String srcStr, long parm)
	{
		return formatString(srcStr, String.valueOf(parm));
	}

	/**
	 * 格式化字符串 当参数只有一个，使用此方法
	 * 
	 * @param srcStr
	 *            来源字符串
	 * @param parm
	 *            单一参数
	 * @return 格式化后的字符串
	 */
	public final static String formatString(String srcStr, String parm)
	{
		return formatString(srcStr, new String[] { parm });
	}

	public final static String trim(final String val, final int[] parms)
	{
		int len = val.length();
		int st = 0;
		int off = 0;

		while ((st < len)
				&& (val.charAt(off + st) <= ' ' || val.charAt(off + st) == '\t'))
			st++;

		while ((st < len)
				&& (val.charAt(off + len - 1) <= ' ' || val.charAt(off + len
						- 1) == '\t'))
			len--;

		if (parms != null) {
			parms[0] = st;
			parms[1] = len;
		}

		return ((st > 0) || (len < val.length())) ? val.substring(st, len) : val;
	}

	/**
	 * 去掉字符串左右两边的制表符
	 * 
	 * @param str
	 *            原始字符串
	 * @return 转换后的字符串
	 */
	public final static String trimTab(String str)
	{
		for (; str.startsWith("\t") || str.endsWith("\t");) {
			if (str.startsWith("\t")) {
				str = str.substring(1);
			}

			if (str.endsWith("\t")) {
				str = str.substring(0, str.length() - 1);
			}
		}

		return str;
	}

	/*
	 * 分割字符串
	 * 
	 * @param original 待分割字符串
	 * 
	 * @param regex 分割符
	 * 
	 * 调用示例: import tool.strDeal;//插入包 String[] strLine=
	 * strDeal.split("你 好!"," ");//用空格分割
	 */
	public final static String[] split(String original, char regex)
	{
		return split(original, String.valueOf(regex), false);
	}

	/*
	 * 分割字符串
	 * 
	 * @param original 待分割字符串
	 * 
	 * @param regex 分割符
	 * 
	 * 调用示例: import tool.strDeal;//插入包 String[] strLine=
	 * strDeal.split("你 好!"," ");//用空格分割
	 */
	public final static String[] split(String original, String regex)
	{
		return split(original, regex, false);
	}

	/*
	 * 分割字符串
	 * 
	 * @param original 待分割字符串
	 * 
	 * @param regex 分割符
	 * 
	 * 调用示例: import tool.strDeal;//插入包 String[] strLine=
	 * strDeal.split("你 好!"," ");//用空格分割
	 */
	public final static String[] split(String original, char regex,
			boolean isIgnoreCase)
	{

		return split(original, String.valueOf(regex), isIgnoreCase);
	}

	/*
	 * 分割字符串
	 * 
	 * @param original 待分割字符串
	 * 
	 * @param regex 分割符
	 * 
	 * 调用示例: import tool.strDeal;//插入包 String[] strLine=
	 * strDeal.split("你 好!"," ");//用空格分割
	 */
	public final static String[] split(String src, String regex,
			boolean isIgnoreCase)
	{

		// 取子串的起始位置
		int si = 0;

		// 将结果数据先放入Vector中 注意应当引入import java.util.Vector;
		String[] strs = new String[200];
		int strIdx = 0;

		// 存储取子串时起始位置
		int idx = 0;

		// 获得匹配子串的位置
		if (isIgnoreCase)
			si = StringHelper.indexOfIgnoreCase(src, regex);
		else
			si = src.indexOf(regex);

		// 如果没有找到
		if (si == -1)
			return new String[] { src };

		// 如果起始字符串的位置小于字符串的长度，则证明没有取到字符串末尾。
		// -1代表取到了末尾
		// 判断的条件，循环查找依据
		for (; si < src.length() && si != -1;) {
			// 取子串
			strs[strIdx++] = src.substring(idx, si);

			// 设置取子串的起始位置
			idx = si + regex.length();

			// 获得匹配子串的位置
			if (isIgnoreCase)
				si = StringHelper.indexOfIgnoreCase(src, regex,
						si + regex.length());
			else
				si = src.indexOf(regex, si + regex.length());
		}

		if (idx < src.length())
			// 取结束的子串
			strs[strIdx++] = src.substring(idx);

		// 返回的结果字符串数组
		String[] str = new String[strIdx];
		System.arraycopy(strs, 0, str, 0, strIdx);

		strs = null;

		// 返回生成的数组
		return str;
	}

	/**
	 * 从原字符串中找到指定的字符串,替换成另外的字符串
	 * 
	 * @param s
	 * @param s1
	 * @param s2
	 * @param ignoreCase
	 *            是否忽略大小写
	 * @return
	 */
	public final static String replaceString(String s, String s1, String s2, boolean ignoreCase)
	{
		boolean isProcessed = false;

		if (s == null || s1 == null || s2 == null)
			return s;

		int j;
		StringBuffer sb;
		if (ignoreCase) {
			if (StringHelper.indexOfIgnoreCase(s, s1) == -1)
				return s;
				
			s1 = s1.toLowerCase();
			int i;
			
			
			sb = new StringBuffer();
			for (; (i = StringHelper.indexOfIgnoreCase(s, s1)) != -1;) {
				String s3 = s.substring(0, i);
				String s5 = s.substring(i + s1.length());
				sb.append(s3).append(s2);
				s = s5;
				isProcessed = true;
				s5 = null;
				s3 = null;
			}
		} else {
			if (s.indexOf(s1) == -1)
				return s;
			
			sb = new StringBuffer();
			for (; (j = s.indexOf(s1)) != -1;) {
				String s4 = s.substring(0, j);
				String s6 = s.substring(j + s1.length());
				sb.append(s4).append(s2);
				s = s6;
				isProcessed = true;
				s6 = null;
				s4 = null;
			}
		}

		sb.append(s);

		String result = sb.toString();
		sb = null;

		if (isProcessed)
			return result;
		else
			return s;
	}

	/**
	 * 从原字符串中找到指定的字符串,替换成另外的字符串,不忽略大小写
	 * 
	 * @param s
	 * @param s1
	 * @param s2
	 * @return
	 */
	public final static String replaceString(String s, String s1, String s2)
	{
		return replaceString(s, s1, s2, false);
	}

	// /**
	// * 判断字符区域是否匹配
	// *
	// * @param ignoreCase
	// * @param left
	// * @param leftPos
	// * @param right
	// * @param rightPos
	// * @param len
	// * @return
	// */
	// public final static boolean regionMatches(boolean ignoreCase, String
	// left,
	// int leftPos, String right, int rightPos, int len) {
	//
	// // 判断是否越界
	// if (left == null || right == null || leftPos < 0 || rightPos < 0
	// || leftPos + len > left.length()
	// || rightPos + len > right.length()) {
	//
	// return false;
	// } else {
	// for (int n = 0; n < len; ++n) {
	// char ch1 = left.charAt(leftPos++);
	// char ch2 = right.charAt(rightPos++);
	//
	// if (ch1 == ch2) {
	// continue;
	// } else {
	// if (Character.toLowerCase(ch1) == Character
	// .toLowerCase(ch2)) {
	// continue;
	// } else {
	// return false;
	// }
	// }
	// }
	//
	// return true;
	// }
	// }

	/**
	 * 判断开始字符忽略大小写
	 * 
	 * @param s
	 * @param sub
	 * @return
	 */
	public final static boolean startWithIgnoreCase(String s, String sub)
	{
		if (s == null) {
			return false;
		}

		return s.regionMatches(true, 0, sub, 0, sub.length());
	}

	/**
	 * 判断开始字符忽略大小写
	 * 
	 * @param s
	 * @param sub
	 * @param startIdx
	 * @return
	 */
	public final static boolean startWithIgnoreCase(String s, String sub,
			int startIdx)
	{

		if (s == null) {
			return false;
		}

		return s.regionMatches(true, startIdx, sub, 0, sub.length());
	}

	/**
	 * 判断结束字符忽略大小写
	 * 
	 * @param s
	 * @param sub
	 * @return
	 */
	public final static boolean endWithIgnoreCase(String s, String sub)
	{
		// return src.toLowerCase().endsWith(sub.toLowerCase());

		if (s == null) {
			return false;
		}

		return s.regionMatches(true, s.length() - sub.length(), sub, 0,
				sub.length());
	}

	private static final String	SPLIT_FIX_TOKENS	= "\"\"\'\'[](){}";

	/**
	 * 分割字符串，跳过"", [], (), {}, '' 等，速度较慢
	 * 
	 * @param original
	 * @param regex
	 * @param ignoreCase
	 * @return
	 */
	public final static String[] splitAfterToken(String original, String regex,
			boolean ignoreCase)
	{
		int pos = 0;
		int lastPos = 0;
		int len = original.length();
		int regexLen = regex.length();

		String[] strs = new String[200];
		int count = 0;

		for (; pos < len;) {
			// 以下代码过滤掉Token
			char ch = original.charAt(pos);
			int chPos = SPLIT_FIX_TOKENS.indexOf(ch);

			if (chPos >= 0) {
				char nextCh = SPLIT_FIX_TOKENS.charAt(chPos + 1);

				for (; pos < len;) {
					pos = original.indexOf(nextCh, pos + 1);
					if (original.charAt(pos - 1) != '\\') {
						break;
					}
				}
			} else {
				// 如果找到了字符串
				if (len - pos >= regexLen
						&& (original.indexOf(regex, pos) == pos || ignoreCase
								&& StringHelper.indexOfIgnoreCase(original,
										regex, pos) == pos)) {
					strs[count] = original.substring(lastPos, pos);
					count++;
					pos += regexLen - 1;
					lastPos = pos + 1;
				}
			}

			pos++;
		}

		strs[count] = original.substring(lastPos, len);
		count++;

		// 返回的结果字符串数组
		String[] result = new String[count];
		System.arraycopy(strs, 0, result, 0, count);

		strs = null;

		// 返回生成的数组
		return result;
	}

	/**
	 * 获取URL中的主机名
	 * 
	 * @param s
	 * @return
	 */
	public final static String urlHost(String s)
	{
		if (s == null) {
			return "";
		}

		if (startWithIgnoreCase(s, "http://")) {
			s = s.substring(7);
		}

		int idx = s.indexOf("/");
		if (idx < 0) {
			return s;
		} else {
			return s.substring(0, idx);
		}
	}

	/**
	 * 替换C语言转义符
	 * 
	 * @param src
	 * @return
	 */
	public final static String replaceTransSign(String src)
	{
		String result = src;

		result = replaceString(result, "\\\"", "\"", false); // "
		result = replaceString(result, "\\\'", "\'", false); // '
		result = replaceString(result, "\\\\", "\\", false); // \
		result = replaceString(result, "\\t", "\t", false); // \t
		result = replaceString(result, "\\r", "\r", false); // \r
		result = replaceString(result, "\\n", "\n", false); // \n

		return result;
	}

	/**
	 * 取得除了主机外的Url
	 * 
	 * @param s
	 * @return
	 */
	public final static String urlWithoutHost(String s)
	{
		if (startWithIgnoreCase(s, "http://")) {
			s = s.substring(7);
		}

		int idx = s.indexOf('/');

		// 如果idx < 0则证明本来就是host
		// 如果 "/"在最后一个则本来就是host
		if (idx < 0 || idx == s.length() - 1) {
			return "";
		}

		return s.substring(idx + 1);
	}
}
