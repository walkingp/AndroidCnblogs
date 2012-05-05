package com.cnblogs.android.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import com.cnblogs.android.entity.*;
import com.cnblogs.android.parser.RssItemsXmlParser;
import com.cnblogs.android.utility.NetHelper;

/**
 * Rss操作类
 * 
 * @author walkingp
 * 
 */
public class RssItemHelper extends DefaultHandler {
	/**
	 * 根据网址得到订阅的文章列表
	 * 
	 * @return
	 */
	public static List<RssItem> GetRssList(String url) {
		String dataString = NetHelper.getData(url, "UTF-8"); // NetHelper.GetContentFromUrl(url);
		if (dataString.equals("")) {
			return null;
		}
		ArrayList<RssItem> list = ParseString(dataString);

		return list;
	}
	/**
	 * 将字符串转换为Rss集合
	 * 
	 * @return
	 */
	private static ArrayList<RssItem> ParseString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		ArrayList<RssItem> listRss = new ArrayList<RssItem>();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			RssItemsXmlParser handler = new RssItemsXmlParser(listRss);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			listRss = handler.GetRssItemList();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listRss;
	}
}
