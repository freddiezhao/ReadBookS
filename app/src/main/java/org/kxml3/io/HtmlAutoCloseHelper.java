package org.kxml3.io;

public final class HtmlAutoCloseHelper
{
	public static final String[]	htmlNoCloseTag					= { "img", "input", "br", "hr" };

	public static final String[]	htmlStartClose					= {
																	"meta", "meta", "link", "title", "style", "script", null,
																	"form", "form", "p", "hr", "h1", "h2", "h3", "h4", "h5", "h6",
																	"dl", "ul", "ol", "menu", "dir", "address", "pre",
																	"listing", "xmp", "head", "br", "input", null,
																	"head", "meta", "link", "title", "style", "script", "p", "br", "input", null,
																	"title", "p", "br", "input", null,
																	"body", "head", "style", "link", "title", "p", "br", "input", null,
																	"frameset", "head", "style", "link", "title", "p", "br", "input", null,
																	"li", "p", "h1", "h2", "h3", "h4", "h5", "h6", "dl", "address",
																	"pre", "listing", "xmp", "head", "li", "br", "input", null,
																	"hr", "p", "head", "br", "input", null,
																	"h1", "p", "head", "br", "input", null,
																	"h2", "p", "head", "br", "input", null,
																	"h3", "p", "head", "br", "input", null,
																	"h4", "p", "head", "br", "input", null,
																	"h5", "p", "head", "br", "input", null,
																	"h6", "p", "head", "br", "input", null,
																	"dir", "p", "head", "br", "input", null,
																	"address", "p", "head", "ul", "br", "input", null,
																	"pre", "p", "head", "ul", "br", "input", null,
																	"listing", "p", "head", "br", "input", null,
																	"xmp", "p", "head", "br", "input", null,
																	"blockquote", "p", "head", "br", "input", null,
																	"dl", "p", "dt", "menu", "dir", "address", "pre", "listing",
																	"xmp", "head", "br", "input", null,
																	"dt", "p", "menu", "dir", "address", "pre", "listing", "xmp",
																	"head", "dd", "br", "input", null,
																	"dd", "p", "menu", "dir", "address", "pre", "listing", "xmp",
																	"head", "dt", "br", "input", null,
																	"ul", "p", "head", "ol", "menu", "dir", "address", "pre",
																	"listing", "xmp", "br", "input", null,
																	"ol", "p", "head", "ul", "br", "input", null,
																	"menu", "p", "head", "ul", "br", "input", null,
																	"p", "p", "head", "h1", "h2", "h3", "h4", "h5", "h6", "tt", "i", "b", "u", "s", "strike",
			"big", "small"
			, "br", "input", null,
			"div", "p", "head", "br", "input", null,
			"noscript", "p", "head", "br", "input", null,
			"center", "font", "b", "i", "p", "head", "br", "input", null,
			"a", "a", "br", "input", null,
			"caption", "p", "br", "input", null,
			"colgroup", "caption", "colgroup", "col", "p", "br", "input", null,
			"col", "caption", "col", "p", "br", "input", null,
			"table", "p", "head", "h1", "h2", "h3", "h4", "h5", "h6", "pre",
			"listing", "xmp", "a", "br", "input", null,
			"th", "th", "td", "p", "span", "font", "a", "b", "i", "u", "br", "input", null,
			"td", "th", "td", "p", "span", "font", "a", "b", "i", "u", "br", "input", null,
			"tr", "th", "td", "tr", "caption", "col", "colgroup", "p", "br", "input", null,
			"thead", "caption", "col", "colgroup", "br", "input", null,
			"tfoot", "th", "td", "tr", "caption", "col", "colgroup", "thead",
			"tbody", "p", "br", "input", null,
			"tbody", "th", "td", "tr", "caption", "col", "colgroup", "thead",
			"tfoot", "tbody", "p", "br", "input", null,
			"optgroup", "option", "br", "input", null,
			"option", "option", "br", "input", null,
			"fieldset", "legend", "p", "head", "h1", "h2", "h3", "h4", "h5", "h6",
			"pre", "listing", "xmp", "a", "br", "input", null,
			"br", "br", "input", null,
			"input", "input", null,
			null
																	};

	private static int[]			htmlNoCloseTagHash;
	private static int[]			htmlStartCloseHash;
	private static boolean			htmlStartCloseIndexinitialized	= false;
	private final static int		htmlStartCloseIndex[]			= new int[100];

	static {
		htmlInitAutoClose();
		htmlInitNoCloseTag();
	}

	private final static void htmlInitAutoClose()
	{
		int indx, i = 0;

		if (htmlStartCloseIndexinitialized)
			return;

		for (indx = 0; indx < 100; indx++)
			htmlStartCloseIndex[indx] = -1;

		indx = 0;

		while ((htmlStartClose[i] != null) && (indx < 100 - 1)) {
			htmlStartCloseIndex[indx++] = i;
			while (htmlStartClose[i] != null)
				i++;
			i++;
		}

		htmlStartCloseHash = new int[htmlStartClose.length];
		for (int n = 0; n < htmlStartClose.length; ++n)
			htmlStartCloseHash[n] = StringHelper.hashcodeIgnoreCase(htmlStartClose[n]);

		htmlStartCloseIndexinitialized = true;
	}

	private final static void htmlInitNoCloseTag()
	{
		htmlNoCloseTagHash = new int[htmlNoCloseTag.length];
		for (int n = 0; n < htmlNoCloseTag.length; ++n)
			htmlNoCloseTagHash[n] = StringHelper.hashcodeIgnoreCase(htmlNoCloseTag[n]);
	}

	public final static boolean htmlCheckTagClosed(String newTag, String oldTag)
	{
		return htmlCheckTagClosed(StringHelper.hashcodeIgnoreCase(newTag),
				StringHelper.hashcodeIgnoreCase(oldTag));
	}

	public final static boolean htmlCheckTagClosed(int newTag, int oldTag)
	{
		for (int i = 0; htmlStartCloseIndex[i] != -1
				&& i < htmlStartCloseIndex.length; ++i) {

			if (htmlStartCloseHash[htmlStartCloseIndex[i]] == newTag) {
				for (int j = htmlStartCloseIndex[i] + 1; htmlStartCloseHash[j] != 0; ++j) {
					if (htmlStartCloseHash[j] == oldTag)
						return true;
				}
			}
		}

		return false;
	}

	public final static boolean htmlCheckTagNoClosePart(String tag)
	{
		return htmlCheckTagNoClosePart(StringHelper.hashcodeIgnoreCase(tag));
	}

	public final static boolean htmlCheckTagNoClosePart(int tagHash)
	{
		for (int n = 0; n < htmlNoCloseTagHash.length; ++n) {
			if (tagHash == htmlNoCloseTagHash[n])
				return true;
		}

		return false;
	}
}
