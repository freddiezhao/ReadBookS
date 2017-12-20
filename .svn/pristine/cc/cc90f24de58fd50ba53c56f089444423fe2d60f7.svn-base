package org.kxml3.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;

import com.sina.book.SinaBookApplication;

public class HTMLParser
{
	private StringBuffer	textBuf;
	private Stack<String>	elemStack;
	private StringBuffer	mOutputBuf;
	private boolean			isFirstTextElem				= true;
	private boolean			isLastTextElemContainsSpace	= false;

	public String parse(String orgHtml, String charsetName) throws UnsupportedEncodingException
	{
		orgHtml = "<body>" + orgHtml + "</body>";

		String[] reCharset = { charsetName, "" };
		String result = parseCore(orgHtml, charsetName, reCharset);

		if (reCharset[1] != null && reCharset[1].length() > 0
				&& reCharset[0] != null && reCharset[0].length() > 0
				&& !StringHelper.endWithIgnoreCase(reCharset[0], reCharset[1])) {

			charsetName = reCharset[1];
			result = parseCore(orgHtml, charsetName, reCharset);
		}

		return result;
	}

	public String parseCore(String orgHtml, String charsetName, String[] reCharset) throws UnsupportedEncodingException
	{
		elemStack = new Stack<String>();
		textBuf = new StringBuffer();
		mOutputBuf = new StringBuffer();
		isFirstTextElem = true;
		isLastTextElemContainsSpace = false;

		ByteArrayInputStream is = new ByteArrayInputStream(orgHtml.getBytes(charsetName));

		KXmlParser parser = new KXmlParser();
		try {
			parser.setInput(is, charsetName);
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Error ex) {
			ex.printStackTrace();
		}

		// prepare attributes hash
		Hashtable<String, String> attrs = new Hashtable<String, String>();

		// 开始文档
		onStartDocument();

		// 循环遍历直至读到文档末尾
		for (;;) {
			int eventType = -1;

			try {
				eventType = parser.nextToken();
			} catch (Exception ex) {
				ex.printStackTrace();
			} catch (Error ex) {
				ex.printStackTrace();
			}

			// 结束标记
			if (eventType == XmlPullParser.END_DOCUMENT) {
				checkNotifyTextContent();

				// System.out.println(System.currentTimeMillis() + " " +
				// "END_DOCUMENT");

				break;
			}

			// 判断是否错误类型
			if (eventType != -1) {
				try {
					// 判断文档节点类型
					switch (eventType) {
					case XmlPullParser.START_TAG: {
						// System.out.println(System.currentTimeMillis() + " " +
						// "START_TAG " + parser.getName());

						// 准备属性表
						attrs.clear();
						for (int n = 0; n < parser.getAttributeCount(); ++n) {
							String attrName = parser.getAttributeName(n);
							String attrValue = parser.getAttributeValue(n);

							attrs.put(attrName, attrValue);

							attrName = null;
							attrValue = null;
						}

						checkCloseNoClosePartTag();
						checkNotifyTextContent();

						onStartElement(parser.getName(), reCharset, attrs);

						// 判断charset
						if (reCharset[1] != null && reCharset[1].length() > 0
								&& reCharset[0] != null && reCharset[0].length() > 0
								&& !StringHelper.endWithIgnoreCase(reCharset[0], reCharset[1])) {

							return null;
						}
					}
						break;

					case XmlPullParser.END_TAG: {
						// System.out.println(System.currentTimeMillis() + " " +
						// "END_TAG " + parser.getName());

						// 激发解析进度事件
						if (parser.readPos == -1) {
						} else {
						}

						checkNotifyTextContent();
						onEndElement(parser.getName());
					}
						break;

					case XmlPullParser.TEXT:
						// System.out.println(System.currentTimeMillis() + " " +
						// "TEXT " + parser.getText());

						// 添加到文本缓冲中
						appendBufferText(parser.getText());
						break;

					case XmlPullParser.IGNORABLE_WHITESPACE:
						break;

					case XmlPullParser.CDSECT:
						// System.out.println(System.currentTimeMillis() + " " +
						// "CDSECT " + parser.getText());

						appendTrimBufferText(parser.getText());
						break;

					case XmlPullParser.PROCESSING_INSTRUCTION:
						// System.out.println(System.currentTimeMillis() + " " +
						// "PROCESSING_INSTRUCTION " + parser.getText());

						checkNotifyTextContent();
						break;

					case XmlPullParser.COMMENT:
						// System.out.println(System.currentTimeMillis() + " " +
						// "COMMENT " + parser.getText());

						checkNotifyTextContent();
						break;

					case XmlPullParser.ENTITY_REF: {
						String entityRef = makeEntityRef(parser.getName());

						// System.out.println(System.currentTimeMillis() + " " +
						// "ENTITY_REF " + entityRef);

						checkNotifyTextContent();

						if (entityRef != null && entityRef.length() > 0)
							onContent(entityRef, false);

						entityRef = null;
					}
						break;

					case XmlPullParser.DOCDECL:
						// System.out.println(System.currentTimeMillis() + " " +
						// "DOCDECL " + parser.getText());

						checkNotifyTextContent();
						break;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} catch (Error ex) {
					ex.printStackTrace();
				}
			}
		}

		// destruct attributes table
		attrs.clear();
		attrs = null;

		// System.out.println(System.currentTimeMillis() + " " + "Parse End");

		// 文档结尾
		onEndDocument();

		// 销毁解析器
		parser.dispose();
		parser = null;

		return mOutputBuf.toString();
	}

	public final static String makeEntityRef(String tag)
	{
		if (tag == null)
			return null;
		else if (tag.equals("copy"))
			return "(C)";
		else if (tag.equals("reg"))
			return "(R)";
		else if (tag.equals("amp"))
			return "&";
		else if (tag.equals("lt"))
			return "<";
		else if (tag.equals("gt"))
			return ">";
		else if (tag.equals("nbsp"))
			return " ";
		else if (tag.equals("apos"))
			return "'";
		else if (tag.equals("quot"))
			return "\"";
		else if (tag.equals("middot"))
			return " - ";
		else if (tag.startsWith("#")) {
			// unicode
			int c = (tag.charAt(1) == 'x' ? Integer.parseInt(tag.substring(2),
					16) : Integer.parseInt(tag.substring(1)));

			return String.valueOf((char) c);
		} else if (tag.equals("raquo"))
			// >>
			return String.valueOf((char) 187);
		else if (tag.equals("laquo"))
			// <<
			return String.valueOf((char) 171);
		else if (tag.equals("rsaquo"))
			// >
			return String.valueOf((char) 155);
		else if (tag.equals("lsaquo"))
			// <
			return String.valueOf((char) 139);

		return "";
	}

	private final void checkCloseNoClosePartTag()
	{
		String parentTag = stackTopElem();
		int parentTagType = StringHelper.hashcodeIgnoreCase(parentTag);

		if (HtmlAutoCloseHelper.htmlCheckTagNoClosePart(parentTagType)) {
			onEndElement(parentTag);
		}
	}

	/**
	 * 从栈中获取父亲节点
	 */
	private final String stackTopElem()
	{
		if (elemStack.empty())
			return null;
		else {
			Object elemObj = elemStack.peek();
			if (elemObj instanceof String) {
				return (String) elemObj;
			} else {
				return null;
			}
		}
	}

	private final void checkNotifyTextContent()
	{
		// 判断文字缓存长度
		if (textBuf.length() != 0) {
			checkCloseNoClosePartTag();

			onContent(textBuf.toString());
			textBuf.setLength(0);
		}
	}

	public void onStartDocument()
	{
		mOutputBuf.append("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">");
		
		InputStream is;
		try {
			is = SinaBookApplication.gContext.getResources().getAssets().open("client_style.css");
			byte[] data = StreamHelper.readAll(is);
			String head = new String(data);
			
			mOutputBuf.append("<style type=\"text/css\" >\n");
			mOutputBuf.append(head);
			mOutputBuf.append("\n</style>");
			
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	public void onEndDocument()
	{
		mOutputBuf.append("</html>");
	}

	private final void onStartElement(String tagName, String[] reCharset, Hashtable<String, String> attrs)
	{
		// 节点类型
		int tagType = StringHelper.hashcodeIgnoreCase(tagName);

		// Log.d("Start tag: " + tagName);

		// 获取父亲节点
		String parent = stackTopElem();
		int parentTagType = StringHelper.hashcodeIgnoreCase(parent);

		// 判断html关闭
		if (parent != null
				&& HtmlAutoCloseHelper.htmlCheckTagClosed(tagType, parentTagType)) {

			onEndElement(parent);

			parent = null;
			parent = stackTopElem();
		}

		mOutputBuf.append('<');
		mOutputBuf.append(tagName);

		if (attrs.size() > 0) {
			for (Iterator<String> itr = attrs.keySet().iterator(); itr.hasNext();) {
				String key = itr.next();
				String value = attrs.get(key);

				mOutputBuf.append(' ');
				mOutputBuf.append(key);
				mOutputBuf.append('=');
				mOutputBuf.append('\"');
				mOutputBuf.append(value);
				mOutputBuf.append('\"');
			}
		}

		if ("img".equalsIgnoreCase(tagName) || "br".equalsIgnoreCase(tagName) || "hr".equalsIgnoreCase(tagName)) {
			mOutputBuf.append(" /");
		}

		mOutputBuf.append('>');

		parseElementBegin(parent, tagName);

		parent = null;
		tagName = null;
		attrs = null;
	}

	/**
	 * 分析节点结束
	 * 
	 * @param page
	 * @param parseInfo
	 * @param tagType
	 */
	private final void onEndElement(String tagName)
	{
		// 判断节点结束，是否是特殊的不用入栈的节点
		String parent = stackTopElem();
		int tagType = StringHelper.hashcodeIgnoreCase(tagName);
		int parentTagType = parent == null ? -1 : StringHelper.hashcodeIgnoreCase(parent);

		// 容错处理：检查并关闭之前的节点
		int popCount = 0;
		Vector<String> popTypes = null;
		if (tagType != parentTagType) {
			boolean isSameTagTypeFound = false;

			// 查找tagType相等
			for (int n = elemStack.size() - 1; n >= 0; --n, ++popCount) {
				String elem = (String) elemStack.elementAt(n);
				int elemTagType = StringHelper.hashcodeIgnoreCase(elem);

				if (elemTagType == tagType) {
					isSameTagTypeFound = true;
					break;
				}

				if (popTypes == null) {
					popTypes = new Vector<String>();
				}

				popTypes.add(elem);
			}

			if (!isSameTagTypeFound) {
				popCount = 0;

				if (popTypes != null) {
					popTypes.removeAllElements();
					popTypes = null;
				}
			}
		}

		if (popCount > 0 && popTypes != null) {
			checkNotifyTextContent();

			for (int n = 0; n < popTypes.size(); ++n) {
				String popTag = (String) popTypes.elementAt(n);
				onEndElement(popTag);
			}

			popTypes.removeAllElements();
			popTypes = null;

			// 重新设置parent和parentTagType
			parent = stackTopElem();
			parentTagType = tagType;
		}

		// 元素出栈
		if (tagType == parentTagType) {
			if ("img".equalsIgnoreCase(tagName) || "br".equalsIgnoreCase(tagName) || "hr".equalsIgnoreCase(tagName)) {
			} else {
				mOutputBuf.append('<');
				mOutputBuf.append('/');
				mOutputBuf.append(tagName);
				mOutputBuf.append('>');
			}

			parseElementEnd(parent);
		}

		parent = null;
	}

	private final void parseElementBegin(String parent, String elem)
	{
		if (elem != null) {
			// 新元素入栈
			elemStack.push(elem);
		}

		parent = null;
		elem = null;
	}

	/**
	 * 解析节点完毕
	 * 
	 * @param page
	 * @param parseInfo
	 * @param elem
	 */
	private final void parseElementEnd(String elem)
	{
		if (!elemStack.isEmpty()) {
			elemStack.pop();
		}
	}

	private final void onContent(String text)
	{
		onContent(text, true);
	}

	private final void onContent(String parmText, boolean isCheckWhiteSpace)
	{
		if (parmText == null || parmText.length() == 0) {
			return;
		}

		// 查找并替换静态变量
		String newText = parmText;

		if (newText.length() > 0) {
			String parent = stackTopElem();
			if (parent != null) {
				if (newText.length() > 0) {
					String text = "";

					if (isCheckWhiteSpace) {
						// 判断空格
						char firstChar = newText.charAt(0);
						char lastChar = newText.charAt(newText.length() - 1);

						boolean isLeftSideSpace = firstChar == ' ';
						boolean isRightSideSpace = lastChar == ' ';

						// 第一个文本节点
						if (isFirstTextElem) {
							if (!isLeftSideSpace && !isRightSideSpace)
								text = newText;
							else {
								int[] parms = { 0, 0 };

								text = StringHelper.trim(newText, parms);
								int st = parms[0];
								int len = parms[1];

								// 如果第一个文本节点为全空格，忽略
								if (text.length() == 0) {
									text = "";
								} else if (isRightSideSpace) {
									text = newText.substring(st, len + 1);
								}

								parms = null;
							}

							isFirstTextElem = false;
						} else {
							if (!isLeftSideSpace && !isRightSideSpace)
								text = newText;
							else {
								int[] parms = { 0, 0 };

								text = StringHelper.trim(newText, parms);
								int st = parms[0];
								int len = parms[1];

								if (text.length() == 0) {
									// text完全为空格
									// 那么判断上一个节点最后一个不为空格，
									// 则设置改节点为空格
									if (!isLastTextElemContainsSpace)
										text = " ";
								} else {
									// 如果上一个节点尾部是空格，且这个节点左边是空格
									// 则忽略此节点左边的空格

									// 或者左边无空格且右边有空格
									if ((isLastTextElemContainsSpace && isLeftSideSpace)
											|| (!isLeftSideSpace && isRightSideSpace)) {

										// 如果此节点右边有空格
										if (isRightSideSpace)
											++len;
									} else {
										if (isLeftSideSpace)
											--st;

										if (isRightSideSpace)
											++len;
									}

									text = newText.substring(st, len);
								}

								parms = null;
							}
						}

						// 上一个节点包含空格
						isLastTextElemContainsSpace = isRightSideSpace;
					} else
						text = newText;

					if (text.length() > 0) {
						// 添加文本节点元素
						mOutputBuf.append(text);
					}

					text = null;
				}
			}

			parent = null;
		}

		newText = null;
	}

	private final void appendTrimBufferText(String text)
	{
		// 将文本中的所有空格替换成一个空格
		boolean isLastCharSpace = false;
		for (int n = 0; n < text.length(); ++n) {
			char ch = text.charAt(n);

			// FIXME: 换行符
			if (ch != '\r' && ch != '\n') {
				// 发现空格和tab
				if (/* ch == ' ' || */ch == '\t' || ch == '\r' || ch == '\n') {
					if (isLastCharSpace) {
						continue;
					}

					ch = ' ';
					isLastCharSpace = true;
				} else {
					isLastCharSpace = false;
				}

				textBuf.append(ch);
			}
		}

		// // 将文本中的所有空格替换成一个空格
		// boolean isLastCharSpace = false;
		// for (int n = 0; n < text.length(); ++n) {
		// char ch = text.charAt(n);
		//
		// // 发现空格和tab
		// if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
		// if (text.length() == 1)
		// return;
		//
		// if (isLastCharSpace)
		// continue;
		//
		// ch = ' ';
		// isLastCharSpace = true;
		// } else {
		// isLastCharSpace = false;
		//
		// if (ch != ' ' || textBuf.length() == 0 ||
		// textBuf.charAt(textBuf.length() - 1) != ' ')
		// textBuf.append(ch);
		// }
		// }
		// boolean isLastCharSpace = false;
		// for (int n = 0; n < text.length(); ++n) {
		// char ch = text.charAt(n);
		//
		// // 发现空格和tab
		// if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
		// if (isLastCharSpace)
		// continue;
		//
		// ch = ' ';
		// isLastCharSpace = true;
		// } else
		// isLastCharSpace = false;
		//
		// textBuf.append(ch);
		//
		// if (ch != ' ')
		// textBufWithoutSpace.append(ch);
		// }
	}

	/**
	 * 添加文字到文本缓冲中
	 * 
	 * @param text
	 */
	private final void appendBufferText(String text)
	{
		// System.out.println("||||" + text + "||||");
		for (int n = 0; n < text.length(); ++n) {
			char ch = text.charAt(n);

			if (ch == '\r' || ch == '\n') {
				ch = ' ';
			} else {
				if (ch == '\t')
					ch = ' ';

				// FIXME: 这里\r\n\t不管
				textBuf.append(ch);
			}
		}

		// textBuf.append(text);
	}

}
