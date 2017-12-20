package com.sina.book.htmlspanner;

import java.util.HashMap;

/**
 * HTML特殊字符处理类.
 *
 * @author MarkMjw
 */
public class HtmlDecoder {
    
    /** The Constant gCharTable. */
    public static final HashMap<String, Character> gCharTable;

    /**
     * 解析字符串，处理特殊字符.
     *
     * @param html the html
     * @return 处理后的结果
     */
    public static String decode(String html) {
        String result;
        int tmpPos, i;

        int maxPos = html.length();
        StringBuffer sb = new StringBuffer(maxPos);
        int curPos = 0;
        while (curPos < maxPos) {
            char ch = html.charAt(curPos++);
            if (ch == '&') {
                tmpPos = curPos;
                if (tmpPos < maxPos) {
                    char d = html.charAt(tmpPos++);
                    if (d == '#') {
                        if (tmpPos < maxPos) {
                            d = html.charAt(tmpPos++);
                            if ((d == 'x') || (d == 'X')) {
                                if (tmpPos < maxPos) {
                                    d = html.charAt(tmpPos++);
                                    if (isHexDigit(d)) {
                                        while (tmpPos < maxPos) {
                                            d = html.charAt(tmpPos++);
                                            if (!isHexDigit(d)) {
                                                if (d == ';') {
                                                    result = html.substring(curPos + 2, tmpPos - 1);
                                                    try {
                                                        i = Integer.parseInt(result, 16);
                                                        if ((i >= 0) && (i < 65536)) {
                                                            ch = (char) i;
                                                            sb.append(ch);
                                                        }
                                                        curPos = tmpPos;
                                                    } catch (NumberFormatException e) {
                                                      //无需处理
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else if (isDigit(d)) {
                                while (tmpPos < maxPos) {
                                    d = html.charAt(tmpPos++);
                                    if (!isDigit(d)) {
                                        if (d == ';') {
                                            result = html.substring(curPos + 1, tmpPos - 1);
                                            try {
                                                i = Integer.parseInt(result);
                                                if ((i >= 0) && (i < 65536)) {
                                                    ch = (char) i;
                                                    sb.append(ch);
                                                }
                                                curPos = tmpPos;
                                            } catch (NumberFormatException e) {
                                                //无需处理
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (isLetter(d)) {
                        while (tmpPos < maxPos) {
                            d = html.charAt(tmpPos++);
                            if (!isLetterOrDigit(d)) {
                                if (d == ';') {
                                    result = html.substring(curPos, tmpPos - 1);
                                    Character temp = (Character) gCharTable.get(result);
                                    if (temp != null) {
                                        ch = temp.charValue();
                                        sb.append(ch);
                                    } 
                                    curPos = tmpPos; 
                                }
                                break;
                            }
                        }
                    }
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Checks if is letter or digit.
     *
     * @param c the c
     * @return true, if is letter or digit
     */
    private static boolean isLetterOrDigit(char c) {
        return isLetter(c) || isDigit(c);
    }

    /**
     * Checks if is hex digit.
     *
     * @param c the c
     * @return true, if is hex digit
     */
    private static boolean isHexDigit(char c) {
        return isHexLetter(c) || isDigit(c);
    }

    /**
     * Checks if is letter.
     *
     * @param c the c
     * @return true, if is letter
     */
    private static boolean isLetter(char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
    }

    /**
     * Checks if is hex letter.
     *
     * @param c the c
     * @return true, if is hex letter
     */
    private static boolean isHexLetter(char c) {
        return ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F'));
    }

    /**
     * Checks if is digit.
     *
     * @param c the c
     * @return true, if is digit
     */
    private static boolean isDigit(char c) {
        return (c >= '0') && (c <= '9');
    }

    /**
     * Compact.
     *
     * @param s the s
     * @return the string
     */
    public static String compact(String s) {
        int maxPos = s.length();
        StringBuffer sb = new StringBuffer(maxPos);
        int curPos = 0;
        while (curPos < maxPos) {
            char c = s.charAt(curPos++);
            if (isWhitespace(c)) {
                while ((curPos < maxPos) && isWhitespace(s.charAt(curPos))) {
                    curPos++;
                }
                c = '\u0020';
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Checks if is whitespace. HTML is very particular about what 
     * constitutes white space.
     *
     * @param ch the ch
     * @return true, if is whitespace
     */
    public static boolean isWhitespace(char ch) {
        return (ch == '\u0020') || (ch == '\r') || (ch == '\n') || (ch == '\u0009')
                || (ch == '\u000c') || (ch == '\u200b');
    }

    static {
        //我们总会把文件转成GBK，所以有些GBK不支持的字符写为null
        gCharTable = new HashMap<String, Character>();
        gCharTable.put("quot", Character.valueOf((char)34));
        gCharTable.put("amp", Character.valueOf((char) 38));
        gCharTable.put("apos", Character.valueOf((char) 39));
        gCharTable.put("lt", Character.valueOf((char) 60));
        gCharTable.put("gt", Character.valueOf((char) 62));
        gCharTable.put("nbsp", null);
        gCharTable.put("iexcl", Character.valueOf((char) 161));
        gCharTable.put("cent", Character.valueOf((char) 162));
        gCharTable.put("pound", Character.valueOf((char) 163));
        gCharTable.put("curren", Character.valueOf((char) 164));
        gCharTable.put("yen", Character.valueOf((char) 165));
        gCharTable.put("brvbar", Character.valueOf((char) 166));
        gCharTable.put("sect", Character.valueOf((char) 167));
        gCharTable.put("uml", Character.valueOf((char) 168));
        gCharTable.put("copy", Character.valueOf((char) 169));
        gCharTable.put("ordf", Character.valueOf((char) 170));
        gCharTable.put("laquo", Character.valueOf((char) 171));
        gCharTable.put("not", Character.valueOf((char) 172));
        gCharTable.put("shy", Character.valueOf((char) 173));
        gCharTable.put("reg", Character.valueOf((char) 174));
        gCharTable.put("macr", Character.valueOf((char) 175));
        gCharTable.put("deg", Character.valueOf((char) 176));
        gCharTable.put("plusmn", Character.valueOf((char) 177));
        gCharTable.put("sup2", Character.valueOf((char) 178));
        gCharTable.put("sup3", Character.valueOf((char) 179));
        gCharTable.put("acute", Character.valueOf((char) 180));
        gCharTable.put("micro", Character.valueOf((char) 181));
        gCharTable.put("para", Character.valueOf((char) 182));
        gCharTable.put("middot", Character.valueOf((char) 183));
        gCharTable.put("cedil", Character.valueOf((char) 184));
        gCharTable.put("sup1", Character.valueOf((char) 185));
        gCharTable.put("ordm", Character.valueOf((char) 186));
        gCharTable.put("raquo", Character.valueOf((char) 187));
        gCharTable.put("frac14", Character.valueOf((char) 188));
        gCharTable.put("frac12", Character.valueOf((char) 189));
        gCharTable.put("frac34", Character.valueOf((char) 190));
        gCharTable.put("iquest", Character.valueOf((char) 191));
        gCharTable.put("Agrave", Character.valueOf((char) 192));
        gCharTable.put("Aacute", Character.valueOf((char) 193));
        gCharTable.put("Acirc", Character.valueOf((char) 194));
        gCharTable.put("Atilde", Character.valueOf((char) 195));
        gCharTable.put("Auml", Character.valueOf((char) 196));
        gCharTable.put("Aring", Character.valueOf((char) 197));
        gCharTable.put("AElig", Character.valueOf((char) 198));
        gCharTable.put("Ccedil", Character.valueOf((char) 199));
        gCharTable.put("Egrave", Character.valueOf((char) 200));
        gCharTable.put("Eacute", Character.valueOf((char) 201));
        gCharTable.put("Ecirc", Character.valueOf((char) 202));
        gCharTable.put("Euml", Character.valueOf((char) 203));
        gCharTable.put("Igrave", Character.valueOf((char) 204));
        gCharTable.put("Iacute", Character.valueOf((char) 205));
        gCharTable.put("Icirc", Character.valueOf((char) 206));
        gCharTable.put("Iuml", Character.valueOf((char) 207));
        gCharTable.put("ETH", Character.valueOf((char) 208));
        gCharTable.put("Ntilde", Character.valueOf((char) 209));
        gCharTable.put("Ograve", Character.valueOf((char) 210));
        gCharTable.put("Oacute", Character.valueOf((char) 211));
        gCharTable.put("Ocirc", Character.valueOf((char) 212));
        gCharTable.put("Otilde", Character.valueOf((char) 213));
        gCharTable.put("Ouml", Character.valueOf((char) 214));
        gCharTable.put("times", Character.valueOf((char) 215));
        gCharTable.put("Oslash", Character.valueOf((char) 216));
        gCharTable.put("Ugrave", Character.valueOf((char) 217));
        gCharTable.put("Uacute", Character.valueOf((char) 218));
        gCharTable.put("Ucirc", Character.valueOf((char) 219));
        gCharTable.put("Uuml", Character.valueOf((char) 220));
        gCharTable.put("Yacute", Character.valueOf((char) 221));
        gCharTable.put("THORN", Character.valueOf((char) 222));
        gCharTable.put("szlig", Character.valueOf((char) 223));
        gCharTable.put("agrave", Character.valueOf((char) 224));
        gCharTable.put("aacute", Character.valueOf((char) 225));
        gCharTable.put("acirc", Character.valueOf((char) 226));
        gCharTable.put("atilde", Character.valueOf((char) 227));
        gCharTable.put("auml", Character.valueOf((char) 228));
        gCharTable.put("aring", Character.valueOf((char) 229));
        gCharTable.put("aelig", Character.valueOf((char) 230));
        gCharTable.put("ccedil", Character.valueOf((char) 231));
        gCharTable.put("egrave", Character.valueOf((char) 232));
        gCharTable.put("eacute", Character.valueOf((char) 233));
        gCharTable.put("ecirc", Character.valueOf((char) 234));
        gCharTable.put("euml", Character.valueOf((char) 235));
        gCharTable.put("igrave", Character.valueOf((char) 236));
        gCharTable.put("iacute", Character.valueOf((char) 237));
        gCharTable.put("icirc", Character.valueOf((char) 238));
        gCharTable.put("iuml", Character.valueOf((char) 239));
        gCharTable.put("eth", Character.valueOf((char) 240));
        gCharTable.put("ntilde", Character.valueOf((char) 241));
        gCharTable.put("ograve", Character.valueOf((char) 242));
        gCharTable.put("oacute", Character.valueOf((char) 243));
        gCharTable.put("ocirc", Character.valueOf((char) 244));
        gCharTable.put("otilde", Character.valueOf((char) 245));
        gCharTable.put("ouml", Character.valueOf((char) 246));
        gCharTable.put("divide", Character.valueOf((char) 247));
        gCharTable.put("oslash", Character.valueOf((char) 248));
        gCharTable.put("ugrave", Character.valueOf((char) 249));
        gCharTable.put("uacute", Character.valueOf((char) 250));
        gCharTable.put("ucirc", Character.valueOf((char) 251));
        gCharTable.put("uuml", Character.valueOf((char) 252));
        gCharTable.put("yacute", Character.valueOf((char) 253));
        gCharTable.put("thorn", Character.valueOf((char) 254));
        gCharTable.put("yuml", Character.valueOf((char) 255));
        gCharTable.put("OElig", Character.valueOf((char) 338));
        gCharTable.put("oelig", Character.valueOf((char) 339));
        gCharTable.put("Scaron", Character.valueOf((char) 352));
        gCharTable.put("scaron", Character.valueOf((char) 353));
        gCharTable.put("fnof", Character.valueOf((char) 402));
        gCharTable.put("circ", Character.valueOf((char) 710));
        gCharTable.put("tilde", Character.valueOf((char) 732));
        gCharTable.put("Alpha", Character.valueOf((char) 913));
        gCharTable.put("Beta", Character.valueOf((char) 914));
        gCharTable.put("Gamma", Character.valueOf((char) 915));
        gCharTable.put("Delta", Character.valueOf((char) 916));
        gCharTable.put("Epsilon", Character.valueOf((char) 917));
        gCharTable.put("Zeta", Character.valueOf((char) 918));
        gCharTable.put("Eta", Character.valueOf((char) 919));
        gCharTable.put("Theta", Character.valueOf((char) 920));
        gCharTable.put("Iota", Character.valueOf((char) 921));
        gCharTable.put("Kappa", Character.valueOf((char) 922));
        gCharTable.put("Lambda", Character.valueOf((char) 923));
        gCharTable.put("Mu", Character.valueOf((char) 924));
        gCharTable.put("Nu", Character.valueOf((char) 925));
        gCharTable.put("Xi", Character.valueOf((char) 926));
        gCharTable.put("Omicron", Character.valueOf((char) 927));
        gCharTable.put("Pi", Character.valueOf((char) 928));
        gCharTable.put("Rho", Character.valueOf((char) 929));
        gCharTable.put("Sigma", Character.valueOf((char) 931));
        gCharTable.put("Tau", Character.valueOf((char) 932));
        gCharTable.put("Upsilon", Character.valueOf((char) 933));
        gCharTable.put("Phi", Character.valueOf((char) 934));
        gCharTable.put("Chi", Character.valueOf((char) 935));
        gCharTable.put("Psi", Character.valueOf((char) 936));
        gCharTable.put("Omega", Character.valueOf((char) 937));
        gCharTable.put("alpha", Character.valueOf((char) 945));
        gCharTable.put("beta", Character.valueOf((char) 946));
        gCharTable.put("gamma", Character.valueOf((char) 947));
        gCharTable.put("delta", Character.valueOf((char) 948));
        gCharTable.put("epsilon", Character.valueOf((char) 949));
        gCharTable.put("zeta", Character.valueOf((char) 950));
        gCharTable.put("eta", Character.valueOf((char) 951));
        gCharTable.put("theta", Character.valueOf((char) 952));
        gCharTable.put("iota", Character.valueOf((char) 953));
        gCharTable.put("kappa", Character.valueOf((char) 954));
        gCharTable.put("lambda", Character.valueOf((char) 955));
        gCharTable.put("mu", Character.valueOf((char) 956));
        gCharTable.put("nu", Character.valueOf((char) 957));
        gCharTable.put("xi", Character.valueOf((char) 958));
        gCharTable.put("omicron", Character.valueOf((char) 959));
        gCharTable.put("pi", Character.valueOf((char) 960));
        gCharTable.put("rho", Character.valueOf((char) 961));
        gCharTable.put("sigmaf", Character.valueOf((char) 962));
        gCharTable.put("sigma", Character.valueOf((char) 963));
        gCharTable.put("tau", Character.valueOf((char) 964));
        gCharTable.put("upsilon", Character.valueOf((char) 965));
        gCharTable.put("phi", Character.valueOf((char) 966));
        gCharTable.put("chi", Character.valueOf((char) 967));
        gCharTable.put("psi", Character.valueOf((char) 968));
        gCharTable.put("omega", Character.valueOf((char) 969));
        gCharTable.put("thetasym", Character.valueOf((char) 977));
        gCharTable.put("upsih", Character.valueOf((char) 978));
        gCharTable.put("piv", Character.valueOf((char) 982));
        gCharTable.put("ensp", Character.valueOf((char) 8194));
        gCharTable.put("emsp", Character.valueOf((char) 8195));
        gCharTable.put("thinsp", Character.valueOf((char) 8201));
        gCharTable.put("zwnj", Character.valueOf((char) 8204));
        gCharTable.put("zwj", Character.valueOf((char) 8205));
        gCharTable.put("lrm", Character.valueOf((char) 8206));
        gCharTable.put("rlm", Character.valueOf((char) 8207));
        gCharTable.put("ndash", Character.valueOf((char) 8211));
        gCharTable.put("mdash", Character.valueOf((char) 8212));
        gCharTable.put("lsquo", Character.valueOf((char) 8216));
        gCharTable.put("rsquo", Character.valueOf((char) 8217));
        gCharTable.put("sbquo", Character.valueOf((char) 8218));
        gCharTable.put("ldquo", Character.valueOf((char) 8220));
        gCharTable.put("rdquo", Character.valueOf((char) 8221));
        gCharTable.put("bdquo", Character.valueOf((char) 8222));
        gCharTable.put("dagger", Character.valueOf((char) 8224));
        gCharTable.put("Dagger", Character.valueOf((char) 8225));
        gCharTable.put("bull", Character.valueOf((char) 8226));
        gCharTable.put("hellip", Character.valueOf((char) 8230));
        gCharTable.put("permil", Character.valueOf((char) 8240));
        gCharTable.put("prime", Character.valueOf((char) 8242));
        gCharTable.put("Prime", Character.valueOf((char) 8243));
        gCharTable.put("lsaquo", Character.valueOf((char) 8249));
        gCharTable.put("rsaquo", Character.valueOf((char) 8250));
        gCharTable.put("oline", Character.valueOf((char) 8254));
        gCharTable.put("frasl", Character.valueOf((char) 8260));
        gCharTable.put("euro", Character.valueOf((char) 8364));
        gCharTable.put("image", Character.valueOf((char) 8465));
        gCharTable.put("weierp", Character.valueOf((char) 8472));
        gCharTable.put("real", Character.valueOf((char) 8476));
        gCharTable.put("trade", Character.valueOf((char) 8482));
        gCharTable.put("alefsym", Character.valueOf((char) 8501));
        gCharTable.put("larr", Character.valueOf((char) 8592));
        gCharTable.put("uarr", Character.valueOf((char) 8593));
        gCharTable.put("rarr", Character.valueOf((char) 8594));
        gCharTable.put("darr", Character.valueOf((char) 8595));
        gCharTable.put("harr", Character.valueOf((char) 8596));
        gCharTable.put("crarr", Character.valueOf((char) 8629));
        gCharTable.put("lArr", Character.valueOf((char) 8656));
        gCharTable.put("uArr", Character.valueOf((char) 8657));
        gCharTable.put("rArr", Character.valueOf((char) 8658));
        gCharTable.put("dArr", Character.valueOf((char) 8659));
        gCharTable.put("hArr", Character.valueOf((char) 8660));
        gCharTable.put("forall", Character.valueOf((char) 8704));
        gCharTable.put("part", Character.valueOf((char) 8706));
        gCharTable.put("exist", Character.valueOf((char) 8707));
        gCharTable.put("empty", Character.valueOf((char) 8709));
        gCharTable.put("nabla", Character.valueOf((char) 8711));
        gCharTable.put("isin", Character.valueOf((char) 8712));
        gCharTable.put("notin", Character.valueOf((char) 8713));
        gCharTable.put("ni", Character.valueOf((char) 8715));
        gCharTable.put("prod", Character.valueOf((char) 8719));
        gCharTable.put("sum", Character.valueOf((char) 8721));
        gCharTable.put("minus", Character.valueOf((char) 8722));
        gCharTable.put("lowast", Character.valueOf((char) 8727));
        gCharTable.put("radic", Character.valueOf((char) 8730));
        gCharTable.put("prop", Character.valueOf((char) 8733));
        gCharTable.put("infin", Character.valueOf((char) 8734));
        gCharTable.put("ang", Character.valueOf((char) 8736));
        gCharTable.put("and", Character.valueOf((char) 8743));
        gCharTable.put("or", Character.valueOf((char) 8744));
        gCharTable.put("cap", Character.valueOf((char) 8745));
        gCharTable.put("cup", Character.valueOf((char) 8746));
        gCharTable.put("int", Character.valueOf((char) 8747));
        gCharTable.put("there4", Character.valueOf((char) 8756));
        gCharTable.put("sim", Character.valueOf((char) 8764));
        gCharTable.put("cong", Character.valueOf((char) 8773));
        gCharTable.put("asymp", Character.valueOf((char) 8776));
        gCharTable.put("ne", Character.valueOf((char) 8800));
        gCharTable.put("equiv", Character.valueOf((char) 8801));
        gCharTable.put("le", Character.valueOf((char) 8804));
        gCharTable.put("ge", Character.valueOf((char) 8805));
        gCharTable.put("sub", Character.valueOf((char) 8834));
        gCharTable.put("sup", Character.valueOf((char) 8835));
        gCharTable.put("nsub", Character.valueOf((char) 8836));
        gCharTable.put("sube", Character.valueOf((char) 8838));
        gCharTable.put("supe", Character.valueOf((char) 8839));
        gCharTable.put("oplus", Character.valueOf((char) 8853));
        gCharTable.put("otimes", Character.valueOf((char) 8855));
        gCharTable.put("perp", Character.valueOf((char) 8869));
        gCharTable.put("sdot", Character.valueOf((char) 8901));
        gCharTable.put("lceil", Character.valueOf((char) 8968));
        gCharTable.put("rceil", Character.valueOf((char) 8969));
        gCharTable.put("lfloor", Character.valueOf((char) 8970));
        gCharTable.put("rfloor", Character.valueOf((char) 8971));
        gCharTable.put("lang", Character.valueOf((char) 9001));
        gCharTable.put("rang", Character.valueOf((char) 9002));
        gCharTable.put("loz", Character.valueOf((char) 9674));
        gCharTable.put("spades", Character.valueOf((char) 9824));
        gCharTable.put("clubs", Character.valueOf((char) 9827));
        gCharTable.put("hearts", Character.valueOf((char) 9829));
        gCharTable.put("diams", Character.valueOf((char) 9830));
    }
}
