package com.sina.book.control.download;

import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class ContainerWriter
{
	
	
	
	public ContainerWriter()
	{
	}

	// 写Xml数据
	public String writeXml()
	{
		StringWriter xmlWriter = new StringWriter();
		try {
			XmlPullParserFactory pullFactory = XmlPullParserFactory.newInstance();
			XmlSerializer xmlSerializer = pullFactory.newSerializer();
			xmlSerializer.setOutput(xmlWriter);

			xmlSerializer.startDocument("UTF-8", false);

			xmlSerializer.startTag("", "container");
			xmlSerializer.attribute("", "xmlns", "urn:oasis:names:tc:opendocument:xmlns:container");
			xmlSerializer.attribute("", "version", "1.0");
			
			xmlSerializer.startTag("", "rootfiles");
			
			xmlSerializer.startTag("", "rootfile");
			xmlSerializer.attribute("", "full-path", "OEBPS/content.opf");
			xmlSerializer.attribute("", "media-type", "application/oebps-package+xml");
			xmlSerializer.endTag("", "rootfile");
			
			xmlSerializer.endTag("", "rootfiles");
			xmlSerializer.endTag("", "container");
			
			xmlSerializer.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlWriter.toString();
	}

}
