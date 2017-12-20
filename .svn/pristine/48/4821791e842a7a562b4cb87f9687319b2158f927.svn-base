package com.sina.book.control.download;

import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class NCXWriter
{
	private String mTitle;
	
	private ArrayList<OPFData> mOpfList;
	
	public NCXWriter()
	{
//		//TODO:
//		mTitle = "测试";
//		mOpfList = new ArrayList<OPFData>();
//		for(int i = 0; i < 10; ++i){
//			OPFData data = new OPFData();
//			data.text = "第" + (i+1) + "章";
//			data.href = "Text/contents.xhtml";
//			mOpfList.add(data);
//		}
	}
	
	public void setTitle(String title){
		mTitle = title;
	}
	
	public void setOpfList(ArrayList<OPFData> opfList){
		mOpfList = opfList;
	}
	

	// 写Xml数据
	public String writeXml()
	{
		StringWriter xmlWriter = new StringWriter();
		try {
			// 创建XmlSerializer,有两种方式
			// 方式一:使用工厂类XmlPullParserFactory的方式
			XmlPullParserFactory pullFactory = XmlPullParserFactory.newInstance();
			XmlSerializer xmlSerializer = pullFactory.newSerializer();

			// 方式二:使用Android提供的实用工具类android.util.Xml
			// XmlSerializer xmlSerializer = Xml.newSerializer();
			xmlSerializer.setOutput(xmlWriter);
			// 开始具体的写xml

			// <?xml version='1.0' encoding='UTF-8' standalone='no' ?>
			xmlSerializer.startDocument("UTF-8", false);

			// <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/"
			// version="2005-1">
			xmlSerializer.startTag("", "ncx");
			xmlSerializer.attribute("", "xmlns", "http://www.daisy.org/z3986/2005/ncx/");
			xmlSerializer.attribute("", "version", "2005-1");

			// <head>
			xmlSerializer.startTag("", "head");
			xmlSerializer.startTag("", "meta");
			xmlSerializer.attribute("", "name", "dtb:uid");
			xmlSerializer.attribute("", "content", "123456");
			xmlSerializer.endTag("", "meta");
			xmlSerializer.startTag("", "meta");
			xmlSerializer.attribute("", "name", "dtb:depth");
			xmlSerializer.attribute("", "content", "1");
			xmlSerializer.endTag("", "meta");
			xmlSerializer.startTag("", "meta");
			xmlSerializer.attribute("", "name", "dtb:totalPageCount");
			xmlSerializer.attribute("", "content", "0");
			xmlSerializer.endTag("", "meta");
			xmlSerializer.startTag("", "meta");
			xmlSerializer.attribute("", "name", "dtb:maxPageNumber");
			xmlSerializer.attribute("", "content", "0");
			xmlSerializer.endTag("", "meta");
			xmlSerializer.endTag("", "head");
			
			//<docTitle>
			xmlSerializer.startTag("", "docTitle");
			xmlSerializer.startTag("", "text");
			xmlSerializer.text(mTitle);
			xmlSerializer.endTag("", "text");
			xmlSerializer.endTag("", "docTitle");
			
			//<navMap>
			xmlSerializer.startTag("", "navMap");
			for(int i = 0; i < mOpfList.size(); ++i){
				OPFData data = mOpfList.get(i);
				
				xmlSerializer.startTag("", "navPoint");
				int id = i+1;
				xmlSerializer.attribute("", "id", "navPoint-"+ id);
				xmlSerializer.attribute("", "playOrder", String.valueOf(id));
				
				xmlSerializer.startTag("", "navLabel");
				xmlSerializer.startTag("", "text");
				xmlSerializer.text(data.text);
				xmlSerializer.endTag("", "text");
				xmlSerializer.endTag("", "navLabel");
				
				xmlSerializer.startTag("", "content");
				xmlSerializer.attribute("", "src", data.href);
				xmlSerializer.endTag("", "content");
				
				xmlSerializer.endTag("", "navPoint");
			}
			xmlSerializer.endTag("", "navMap");
			
			// </ncx>
			xmlSerializer.endTag("", "ncx");
			xmlSerializer.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlWriter.toString();
	}

}
