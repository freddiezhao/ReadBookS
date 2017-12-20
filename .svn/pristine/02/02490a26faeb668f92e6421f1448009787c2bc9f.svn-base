package com.sina.book.control.download;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class OPFWriter
{
	private String mTitle;
	
	private String mAuthor;
	
	public ArrayList<OPFData> mCpList;
	
//	public ArrayList<OPFData> mOPFResList;
	public HashMap<String, OPFData> mOPFResList;
	
	public OPFWriter()
	{
		//TODO:
//		mTitle = "测试";
//		mAuthor = "作者";
//		
//		
//		mOPFResList = new ArrayList<OPFData>();
//		for(int i = 0; i < 10; ++i){
//			OPFData data = new OPFData();
//			data.text = "第" + (i+1) + "章";
//			data.href = "Images/pic161.jpg";
//			data.id="pic161.jpg";
//			data.media_type = OPFData.MEDIA_TYPE_JPEG;
//			mOPFResList.add(data);
//		}
//		
//		
//		mCpList = new ArrayList<OPFData>();
//		for(int i = 0; i < 5; ++i){
//			OPFData data = new OPFData();
//			data.text = "第" + (i+1) + "章";
//			data.id = "pic161.jpg";
//			data.href = "Images/pic161.jpg";
//			mCpList.add(data);
//		}
	}
	
	public void setTitle(String title){
		mTitle = title;
	}
	
	public void setAuthor(String author){
		mAuthor = author;
		if(mAuthor == null){
			mAuthor = "";
		}
	}
	
	public void setOpfList(ArrayList<OPFData> opfList){
		mCpList = opfList;
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
			xmlSerializer.startDocument("UTF-8", true);

			xmlSerializer.startTag("", "package");
			xmlSerializer.attribute("", "xmlns", "http://www.idpf.org/2007/opf");
			xmlSerializer.attribute("", "unique-identifier", "BookId");
			xmlSerializer.attribute("", "version", "2.0");
			
			xmlSerializer.startTag("", "metadata");
			xmlSerializer.attribute("", "xmlns:dc", "http://purl.org/dc/elements/1.1/");
			
			xmlSerializer.startTag("", "dc:identifier");
			xmlSerializer.attribute("", "id", "BookId");
			xmlSerializer.text("123456");
			xmlSerializer.endTag("", "dc:identifier");
			
			xmlSerializer.startTag("", "dc:title");
			xmlSerializer.text(mTitle);
			xmlSerializer.endTag("", "dc:title");
			
			if(mAuthor != null){
				xmlSerializer.startTag("", "dc:creator");
				xmlSerializer.attribute("", "opf:role", "aut");
				xmlSerializer.text(mAuthor);
				xmlSerializer.endTag("", "dc:creator");
			}
			
			xmlSerializer.startTag("", "dc:date");
			xmlSerializer.text("2015-01-01");
			xmlSerializer.endTag("", "dc:date");
			
			xmlSerializer.startTag("", "dc:publisher");
			xmlSerializer.text("sa");
			xmlSerializer.endTag("", "dc:publisher");
			
			xmlSerializer.startTag("", "dc:language");
			xmlSerializer.text("zh-CN");
			xmlSerializer.endTag("", "dc:language");
			
			xmlSerializer.endTag("", "metadata");
			
			// manifest
			xmlSerializer.startTag("", "manifest");
			xmlSerializer.startTag("", "item");
			xmlSerializer.attribute("", "href", "toc.ncx");
			xmlSerializer.attribute("", "id", "ncx");
			xmlSerializer.attribute("", "media-type", OPFData.MEDIA_TYPE_NCX);
			xmlSerializer.endTag("", "item");
			
			for(int i = 0; i < mCpList.size(); ++i){
				OPFData data = mCpList.get(i);
				xmlSerializer.startTag("", "item");
				xmlSerializer.attribute("", "href", data.href);
				xmlSerializer.attribute("", "id", data.id);
				xmlSerializer.attribute("", "media-type", data.media_type);
				xmlSerializer.endTag("", "item");
			}
			
			Collection<OPFData> tlist = mOPFResList.values();
			Iterator<OPFData> iterator =  tlist.iterator();
			while(iterator.hasNext()){
				OPFData data  = iterator.next();
				xmlSerializer.startTag("", "item");
				xmlSerializer.attribute("", "href", data.href);
				xmlSerializer.attribute("", "id", data.id);
				xmlSerializer.attribute("", "media-type", data.media_type);
				xmlSerializer.endTag("", "item");
			}
//			for(int i = 0; i < mOPFResList.size(); ++i){
//				OPFData data = mOPFResList.get(i);
//				xmlSerializer.startTag("", "item");
//				xmlSerializer.attribute("", "href", data.href);
//				xmlSerializer.attribute("", "id", data.id);
//				xmlSerializer.attribute("", "media-type", data.media_type);
//				xmlSerializer.endTag("", "item");
//			}
			
			xmlSerializer.endTag("", "manifest");
			
			// spine
			xmlSerializer.startTag("", "spine");
			xmlSerializer.attribute("", "toc", "ncx");
			
			for(int i = 0; i < mCpList.size(); ++i){
				OPFData data = mCpList.get(i);
				xmlSerializer.startTag("", "itemref");
				xmlSerializer.attribute("", "idref", data.id);
				xmlSerializer.endTag("", "itemref");
			}
			xmlSerializer.endTag("", "spine");
			
			xmlSerializer.endTag("", "package");
			xmlSerializer.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlWriter.toString();
	}

}
